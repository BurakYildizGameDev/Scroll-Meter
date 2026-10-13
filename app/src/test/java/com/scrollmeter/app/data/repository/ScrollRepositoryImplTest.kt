package com.scrollmeter.app.data.repository

import app.cash.turbine.test
import com.scrollmeter.app.data.local.dao.ScrollDao
import com.scrollmeter.app.data.local.entity.DailyAppScrollEntity
import com.scrollmeter.app.data.resolver.AppNameResolver
import com.scrollmeter.app.domain.repository.IDeviceMetricsProvider
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ScrollRepositoryImplTest {

    private val scrollDao: ScrollDao = mockk(relaxed = true)
    private val appNameResolver: AppNameResolver = mockk()
    private val metricsProvider: IDeviceMetricsProvider = mockk()
    private val testDispatcher = StandardTestDispatcher()

    private lateinit var repository: ScrollRepositoryImpl

    @BeforeEach
    fun setUp() {
        repository = ScrollRepositoryImpl(
            scrollDao = scrollDao,
            appNameResolver = appNameResolver,
            metricsProvider = metricsProvider,
            ioDispatcher = testDispatcher
        )
    }

    @Test
    fun `getDailyTotalPixels returns 0_0 when DAO returns null`() = runTest(testDispatcher) {
        val epochDay = 20000L
        every { scrollDao.getDailyTotalPixels(epochDay) } returns flowOf(null)

        repository.getDailyTotalPixels(epochDay).test {
            assertEquals(0.0, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `getDailyAppSessions maps entities to domain models with resolved display names`() = runTest(testDispatcher) {
        val epochDay = 20000L
        val entities = listOf(
            DailyAppScrollEntity(epochDay, "com.instagram.android", 1000.0, 100.0, 1000L),
            DailyAppScrollEntity(epochDay, "com.whatsapp", 500.0, 50.0, 1000L)
        )

        every { scrollDao.getDailySessions(epochDay) } returns flowOf(entities)
        every { appNameResolver.resolveDisplayName("com.instagram.android") } returns "Instagram"
        every { appNameResolver.resolveDisplayName("com.whatsapp") } returns "WhatsApp"

        repository.getDailyAppSessions(epochDay).test {
            val sessions = awaitItem()
            assertEquals(2, sessions.size)
            assertEquals("Instagram", sessions[0].appDisplayName)
            assertEquals(1000.0, sessions[0].totalPixels)
            assertEquals(100.0, sessions[0].physicalDistance.totalMilliMeters)

            assertEquals("WhatsApp", sessions[1].appDisplayName)
            assertEquals(500.0, sessions[1].totalPixels)
            assertEquals(50.0, sessions[1].physicalDistance.totalMilliMeters)
            awaitComplete()
        }
    }

    @Test
    fun `recordScrollBatch calculates mm and calls DAO upsertScroll for each non-zero item`() = runTest(testDispatcher) {
        val epochDay = 20000L
        val batch = mapOf(
            "com.twitter" to 254.0,
            "com.empty" to 0.0
        )

        every { metricsProvider.getDevicePpi() } returns flowOf(254.0f)
        every { metricsProvider.getCalibrationMultiplier() } returns flowOf(1.0f)

        repository.recordScrollBatch(epochDay, batch)

        // At 254 PPI and 1.0 multiplier, 254 px = 25.4 mm
        coVerify(exactly = 1) {
            scrollDao.upsertScroll(
                dateEpochDays = epochDay,
                packageName = "com.twitter",
                pixelsToAdd = 254.0,
                mmToAdd = 25.4,
                timestamp = any()
            )
        }
        coVerify(exactly = 0) {
            scrollDao.upsertScroll(
                dateEpochDays = any(),
                packageName = "com.empty",
                pixelsToAdd = any(),
                mmToAdd = any(),
                timestamp = any()
            )
        }
    }

    @Test
    fun `clearDailyScroll delegates to DAO`() = runTest(testDispatcher) {
        val epochDay = 20000L
        repository.clearDailyScroll(epochDay)
        coVerify(exactly = 1) { scrollDao.clearDailyScroll(epochDay) }
    }

    @Test
    fun `deletePackage delegates to DAO`() = runTest(testDispatcher) {
        repository.deletePackage("android")
        coVerify(exactly = 1) { scrollDao.deletePackage("android") }
    }
}
