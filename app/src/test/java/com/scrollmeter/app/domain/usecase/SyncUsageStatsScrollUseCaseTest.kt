package com.scrollmeter.app.domain.usecase

import com.scrollmeter.app.data.datastore.SettingsDataStore
import com.scrollmeter.app.domain.repository.IDeviceMetricsProvider
import com.scrollmeter.app.domain.repository.IScrollRepository
import com.scrollmeter.app.domain.repository.IUsageStatsProvider
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SyncUsageStatsScrollUseCaseTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var usageStatsProvider: IUsageStatsProvider
    private lateinit var scrollRepository: IScrollRepository
    private lateinit var deviceMetricsProvider: IDeviceMetricsProvider
    private lateinit var settingsDataStore: SettingsDataStore
    private lateinit var useCase: SyncUsageStatsScrollUseCase

    private val fixedEpochDay = 20500L

    @BeforeEach
    fun setUp() {
        usageStatsProvider = mockk(relaxed = true)
        scrollRepository = mockk(relaxed = true)
        deviceMetricsProvider = mockk(relaxed = true)
        settingsDataStore = mockk(relaxed = true)

        every { deviceMetricsProvider.getDevicePpi() } returns flowOf(400f)
        every { deviceMetricsProvider.getCalibrationMultiplier() } returns flowOf(1.0f)
        every { settingsDataStore.cadenceSpeedMultiplier } returns flowOf(1.0f)
        every { settingsDataStore.usageBaselineEpochDay } returns flowOf(fixedEpochDay)
        every { settingsDataStore.usageBaselineData } returns flowOf("")

        useCase = SyncUsageStatsScrollUseCase(
            usageStatsProvider = usageStatsProvider,
            scrollRepository = scrollRepository,
            deviceMetricsProvider = deviceMetricsProvider,
            settingsDataStore = settingsDataStore,
            ioDispatcher = testDispatcher
        ).apply {
            epochDaysSupplier = { fixedEpochDay }
        }
    }

    @Test
    fun `invoke returns false when usage access permission is not granted`() = runTest(testDispatcher) {
        every { usageStatsProvider.isUsageAccessGranted() } returns false

        val result = useCase()

        assertFalse(result)
        coVerify(exactly = 0) { scrollRepository.setDailyAppScroll(any(), any(), any(), any()) }
    }

    @Test
    fun `invoke returns true and snapshots baseline on initial first launch`() = runTest(testDispatcher) {
        every { usageStatsProvider.isUsageAccessGranted() } returns true
        every { settingsDataStore.usageBaselineEpochDay } returns flowOf(-1L)
        every { usageStatsProvider.queryTodayForegroundMillis() } returns mapOf(
            "com.zhiliaoapp.musically" to 600_000L
        )

        val result = useCase()

        assertTrue(result)
        coVerify(exactly = 1) { scrollRepository.clearDailyScroll(fixedEpochDay) }
        coVerify(exactly = 1) { settingsDataStore.setUsageBaseline(fixedEpochDay, match { it.contains("com.zhiliaoapp.musically") }) }
        coVerify(exactly = 0) { scrollRepository.setDailyAppScroll(any(), any(), any(), any()) }
    }

    @Test
    fun `invoke returns true and persists calculated cadence distance when usage stats exist`() = runTest(testDispatcher) {
        every { usageStatsProvider.isUsageAccessGranted() } returns true
        every { usageStatsProvider.queryTodayForegroundMillis() } returns mapOf(
            "com.zhiliaoapp.musically" to 600_000L // 10 minutes of TikTok
        )

        val result = useCase()

        assertTrue(result)
        // 10 minutes * 0.18 m/min = 1.80 meters = 1,800.0 mm
        coVerify(exactly = 1) {
            scrollRepository.setDailyAppScroll(
                epochDays = fixedEpochDay,
                packageName = "com.zhiliaoapp.musically",
                totalPixels = match { it > 0.0 },
                totalMm = match { Math.abs(it - 1_800.0) < 0.1 }
            )
        }
    }

    @Test
    fun `invoke filters blacklisted android framework and system apps`() = runTest(testDispatcher) {
        every { usageStatsProvider.isUsageAccessGranted() } returns true
        every { usageStatsProvider.queryTodayForegroundMillis() } returns mapOf(
            "android" to 10_000_000L,
            "com.android.systemui" to 5_000_000L
        )

        val result = useCase()

        assertTrue(result)
        coVerify(exactly = 0) { scrollRepository.setDailyAppScroll(any(), any(), any(), any()) }
        coVerify(atLeast = 1) { scrollRepository.deletePackage("android") }
    }

    @Test
    fun `invoke returns true without persisting if usage map is empty`() = runTest(testDispatcher) {
        every { usageStatsProvider.isUsageAccessGranted() } returns true
        every { usageStatsProvider.queryTodayForegroundMillis() } returns emptyMap()

        val result = useCase()

        assertTrue(result)
        coVerify(exactly = 0) { scrollRepository.setDailyAppScroll(any(), any(), any(), any()) }
    }
}
