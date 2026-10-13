package com.scrollmeter.app.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.scrollmeter.app.core.dispatchers.IoDispatcher
import com.scrollmeter.app.data.local.dao.ScrollDao
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.time.LocalDate
import java.util.concurrent.TimeUnit

/**
 * Enterprise Data Retention Worker.
 * Automatically purges scroll telemetry older than 30 days to prevent SQLite bloat.
 * Runs strictly when the device is idle or charging (zero battery impact).
 */
@HiltWorker
class DataCleanupWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val scrollDao: ScrollDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(ioDispatcher) {
        try {
            val currentEpochDay = LocalDate.now().toEpochDay()
            val cutoffEpochDay = currentEpochDay - RETENTION_DAYS_THRESHOLD

            Timber.i("DataCleanupWorker starting. Purging records prior to epoch day: $cutoffEpochDay")
            val deletedRows = scrollDao.deleteRecordsOlderThan(cutoffEpochDay)
            Timber.i("DataCleanupWorker completed successfully. Purged $deletedRows rows.")

            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "DataCleanupWorker failed to purge stale telemetry.")
            Result.retry()
        }
    }

    companion object {
        const val WORK_NAME = "scroll_meter_data_cleanup_work"
        const val RETENTION_DAYS_THRESHOLD = 30L

        fun enqueuePeriodicCleanup(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiresCharging(true)
                .setRequiresBatteryNotLow(true)
                .build()

            val workRequest = PeriodicWorkRequestBuilder<DataCleanupWorker>(
                repeatInterval = 1,
                repeatIntervalTimeUnit = TimeUnit.DAYS
            )
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
        }
    }
}
