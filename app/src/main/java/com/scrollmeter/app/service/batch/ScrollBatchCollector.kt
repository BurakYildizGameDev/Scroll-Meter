package com.scrollmeter.app.service.batch

import com.scrollmeter.app.core.dispatchers.IoDispatcher
import com.scrollmeter.app.domain.repository.IScrollRepository
import com.scrollmeter.app.service.filter.ScrollGestureFilter
import com.scrollmeter.app.service.model.RawScrollEvent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Batches incoming in-memory scroll events and persists them to the repository
 * using debounced intervals (every 5 seconds) or volume thresholds (20,000 pixels).
 */
@Singleton
class ScrollBatchCollector @Inject constructor(
    private val scrollRepository: IScrollRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {

    companion object {
        const val DEBOUNCE_INTERVAL_MS = 5000L
        const val VOLUME_THRESHOLD_PX = 20_000.0
    }

    private val mutex = Mutex()
    private val inMemoryBatch = mutableMapOf<String, Double>()
    private var accumulatedPixels = 0.0

    private var collectorJob: Job? = null
    private var timerJob: Job? = null

    // Open for test overriding
    var epochDaySupplier: () -> Long = { LocalDate.now().toEpochDay() }

    /**
     * Starts listening to incoming raw scroll events and schedules
     * the periodic debounce flush timer.
     */
    fun startCollecting(scope: CoroutineScope, eventFlow: Flow<RawScrollEvent>): Job {
        stopCollecting()

        collectorJob = scope.launch(ioDispatcher) {
            try {
                eventFlow.collect { event ->
                    addEvent(event)
                }
            } catch (e: Exception) {
                Timber.e(e, "Error collecting scroll events")
            }
        }

        timerJob = scope.launch(ioDispatcher) {
            try {
                while (isActive) {
                    delay(DEBOUNCE_INTERVAL_MS)
                    flush()
                }
            } catch (e: Exception) {
                Timber.e(e, "Error in scroll batch debounce timer")
            }
        }

        return collectorJob!!
    }

    // Adaptive 120Hz/144Hz high-refresh rate mode (allows 1.0px sub-pixel displacement)
    var isHighRefreshRate: Boolean = false

    suspend fun addEvent(event: RawScrollEvent) = withContext(ioDispatcher) {
        val distance = ScrollGestureFilter.processScrollGesture(
            packageName = event.packageName,
            deltaX = event.deltaX,
            deltaY = event.deltaY,
            isHighRefreshRate = isHighRefreshRate
        ) ?: return@withContext

        var batchToFlush: Map<String, Double>? = null
        mutex.withLock {
            inMemoryBatch[event.packageName] = (inMemoryBatch[event.packageName] ?: 0.0) + distance
            accumulatedPixels += distance

            if (accumulatedPixels >= VOLUME_THRESHOLD_PX) {
                batchToFlush = inMemoryBatch.toMap()
                inMemoryBatch.clear()
                accumulatedPixels = 0.0
            }
        }

        if (batchToFlush != null) {
            val epoch = epochDaySupplier()
            try {
                scrollRepository.recordScrollBatch(epoch, batchToFlush!!)
            } catch (e: Exception) {
                Timber.e(e, "Failed to persist scroll batch to repository in addEvent")
            }
        }
    }

    /**
     * Flushes any pending accumulated batch to the repository.
     */
    suspend fun flush() = withContext(ioDispatcher) {
        var batchToFlush: Map<String, Double>? = null
        mutex.withLock {
            if (inMemoryBatch.isNotEmpty()) {
                batchToFlush = inMemoryBatch.toMap()
                inMemoryBatch.clear()
                accumulatedPixels = 0.0
            }
        }

        if (batchToFlush != null) {
            val epoch = epochDaySupplier()
            try {
                scrollRepository.recordScrollBatch(epoch, batchToFlush!!)
            } catch (e: Exception) {
                Timber.e(e, "Failed to persist scroll batch to repository in flush")
            }
        }
    }

    /**
     * Pauses the collection jobs and flushes any remaining items immediately.
     */
    suspend fun pause() = withContext(ioDispatcher) {
        stopCollecting()
        flush()
    }

    /**
     * Resumes collection if not currently running.
     */
    fun resume(scope: CoroutineScope, eventFlow: Flow<RawScrollEvent>) {
        if (collectorJob?.isActive == true) return
        startCollecting(scope, eventFlow)
    }

    /**
     * Stops and cancels all active collection and timer jobs.
     */
    fun stopCollecting() {
        collectorJob?.cancel()
        collectorJob = null
        timerJob?.cancel()
        timerJob = null
    }

    /**
     * Helper to query current pending batch count for testing.
     */
    internal suspend fun getPendingCount(): Int = mutex.withLock {
        inMemoryBatch.size
    }
}
