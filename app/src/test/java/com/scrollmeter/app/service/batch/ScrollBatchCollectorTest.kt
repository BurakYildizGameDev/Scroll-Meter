package com.scrollmeter.app.service.batch

import com.scrollmeter.app.domain.repository.IScrollRepository
import com.scrollmeter.app.service.model.RawScrollEvent
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ScrollBatchCollectorTest {

    private lateinit var scrollRepository: IScrollRepository
    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)
    private lateinit var collector: ScrollBatchCollector

    @BeforeEach
    fun setUp() {
        scrollRepository = mockk(relaxed = true)
        collector = ScrollBatchCollector(
            scrollRepository = scrollRepository,
            ioDispatcher = testDispatcher
        ).apply {
            epochDaySupplier = { 20000L }
        }
    }

    @Test
    fun `addEvent filters out micro jitter and anomaly spike`() = testScope.runTest {
        // Micro jitter (< 3px)
        collector.addEvent(RawScrollEvent("com.instagram", 1f, 1f))
        assertEquals(0, collector.getPendingCount())

        // Anomaly spike (> 5000px)
        collector.addEvent(RawScrollEvent("com.instagram", 0f, 6000f))
        assertEquals(0, collector.getPendingCount())

        // Blacklisted package
        collector.addEvent(RawScrollEvent("com.android.systemui", 0f, 50f))
        assertEquals(0, collector.getPendingCount())

        // Valid event
        collector.addEvent(RawScrollEvent("com.instagram", 0f, 100f))
        assertEquals(1, collector.getPendingCount())
    }

    @Test
    fun `flush sends pending batch to repository`() = testScope.runTest {
        collector.addEvent(RawScrollEvent("com.instagram", 0f, 100f))
        collector.addEvent(RawScrollEvent("com.twitter", 0f, 200f))

        collector.flush()

        coVerify(exactly = 1) {
            scrollRepository.recordScrollBatch(
                epochDays = 20000L,
                batch = match {
                    it["com.instagram"] == 100.0 && it["com.twitter"] == 200.0
                }
            )
        }
        assertEquals(0, collector.getPendingCount())
    }

    @Test
    fun `volume threshold triggers automatic flush`() = testScope.runTest {
        // Threshold is 20,000px. Add 5 events of 4500px each = 22,500px.
        repeat(5) {
            collector.addEvent(RawScrollEvent("com.tiktok", 0f, 4500f))
        }

        coVerify(atLeast = 1) {
            scrollRepository.recordScrollBatch(
                epochDays = 20000L,
                batch = match { it.containsKey("com.tiktok") }
            )
        }
    }

    @Test
    fun `debounce interval flushes batches automatically over time`() = testScope.runTest {
        val eventFlow = MutableSharedFlow<RawScrollEvent>(extraBufferCapacity = 10)
        collector.startCollecting(this, eventFlow)
        runCurrent() // Ensure collector and timer coroutines start

        // Emit an event
        eventFlow.emit(RawScrollEvent("com.reddit", 0f, 50f))
        runCurrent() // Process the event into batch

        assertEquals(1, collector.getPendingCount())

        // Before 5 seconds, no flush has occurred yet
        advanceTimeBy(2000)
        runCurrent()
        coVerify(exactly = 0) {
            scrollRepository.recordScrollBatch(any(), any())
        }

        // Advance past 5000ms debounce threshold
        advanceTimeBy(3500)
        runCurrent()

        coVerify(exactly = 1) {
            scrollRepository.recordScrollBatch(
                epochDays = 20000L,
                batch = match { it["com.reddit"] == 50.0 }
            )
        }
        assertEquals(0, collector.getPendingCount())

        collector.stopCollecting()
    }

    @Test
    fun `pause stops collection and flushes any pending items`() = testScope.runTest {
        collector.addEvent(RawScrollEvent("com.threads", 0f, 80f))
        assertEquals(1, collector.getPendingCount())

        collector.pause()

        coVerify(exactly = 1) {
            scrollRepository.recordScrollBatch(
                epochDays = 20000L,
                batch = match { it["com.threads"] == 80.0 }
            )
        }
        assertEquals(0, collector.getPendingCount())
    }

    @Test
    fun `100 events within 5 seconds trigger exactly 1 batch persistence`() = testScope.runTest {
        val eventFlow = MutableSharedFlow<RawScrollEvent>(extraBufferCapacity = 200)
        collector.startCollecting(this, eventFlow)
        runCurrent()

        // Emit 100 events of 50px each (total 5000px, which is under 20,000px volume threshold)
        repeat(100) { i ->
            eventFlow.emit(RawScrollEvent("com.instagram", 0f, 50f, timestamp = i.toLong()))
        }
        runCurrent()

        // Verify repository has NOT been called 100 times
        coVerify(exactly = 0) {
            scrollRepository.recordScrollBatch(any(), any())
        }

        // Advance virtual time by 5000ms
        advanceTimeBy(5001)
        runCurrent()

        // Verify repository was called exactly 1 time in a single batch
        coVerify(exactly = 1) {
            scrollRepository.recordScrollBatch(
                epochDays = 20000L,
                batch = match { it["com.instagram"] == 5000.0 }
            )
        }

        collector.stopCollecting()
    }
}
