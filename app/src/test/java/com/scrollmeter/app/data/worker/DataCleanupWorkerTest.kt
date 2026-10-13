package com.scrollmeter.app.data.worker

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.scrollmeter.app.data.local.dao.ScrollDao
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class DataCleanupWorkerTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var context: Context
    private lateinit var workerParams: WorkerParameters
    private lateinit var scrollDao: ScrollDao

    @BeforeEach
    fun setUp() {
        context = mockk(relaxed = true)
        workerParams = mockk(relaxed = true)
        scrollDao = mockk(relaxed = true)
    }

    @Test
    fun `doWork successfully deletes records older than 30 days and returns success`() = runTest(testDispatcher) {
        coEvery { scrollDao.deleteRecordsOlderThan(any()) } returns 42

        val worker = DataCleanupWorker(
            appContext = context,
            workerParams = workerParams,
            scrollDao = scrollDao,
            ioDispatcher = testDispatcher
        )

        val result = worker.doWork()

        assertEquals(ListenableWorker.Result.success(), result)
        coVerify(exactly = 1) { scrollDao.deleteRecordsOlderThan(any()) }
    }

    @Test
    fun `doWork returns retry when dao throws exception`() = runTest(testDispatcher) {
        coEvery { scrollDao.deleteRecordsOlderThan(any()) } throws RuntimeException("SQLite lock failed")

        val worker = DataCleanupWorker(
            appContext = context,
            workerParams = workerParams,
            scrollDao = scrollDao,
            ioDispatcher = testDispatcher
        )

        val result = worker.doWork()

        assertEquals(ListenableWorker.Result.retry(), result)
    }
}
