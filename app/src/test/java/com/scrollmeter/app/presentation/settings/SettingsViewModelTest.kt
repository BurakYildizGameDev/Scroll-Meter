package com.scrollmeter.app.presentation.settings

import app.cash.turbine.test
import com.scrollmeter.app.data.datastore.SettingsDataStore
import com.scrollmeter.app.domain.repository.IDeviceMetricsProvider
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var settingsDataStore: SettingsDataStore
    private lateinit var deviceMetricsProvider: IDeviceMetricsProvider
    private lateinit var usageStatsProvider: com.scrollmeter.app.domain.repository.IUsageStatsProvider
    private lateinit var syncUsageStatsScrollUseCase: com.scrollmeter.app.domain.usecase.SyncUsageStatsScrollUseCase
    private lateinit var viewModel: SettingsViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        settingsDataStore = mockk(relaxed = true)
        deviceMetricsProvider = mockk(relaxed = true)
        usageStatsProvider = mockk(relaxed = true)
        syncUsageStatsScrollUseCase = mockk(relaxed = true)

        every { settingsDataStore.calibrationMultiplier } returns flowOf(1.0f)
        every { settingsDataStore.dailyGoalMeters } returns flowOf(100.0f)
        every { deviceMetricsProvider.getDevicePpi() } returns flowOf(440.0f)
        every { settingsDataStore.themeMode } returns flowOf(0)
        every { settingsDataStore.trackingMode } returns flowOf(com.scrollmeter.app.domain.model.TrackingMode.SAFE_USAGE_STATS)
        every { settingsDataStore.cadenceSpeedMultiplier } returns flowOf(1.0f)
        every { settingsDataStore.selectedLanguage } returns flowOf("")

        viewModel = SettingsViewModel(
            settingsDataStore = settingsDataStore,
            deviceMetricsProvider = deviceMetricsProvider,
            usageStatsProvider = usageStatsProvider,
            syncUsageStatsScrollUseCase = syncUsageStatsScrollUseCase
        )
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `uiState emits initial settings correctly`() = runTest(testDispatcher) {
        viewModel.uiState.test {
            val initial = awaitItem()
            val state = if (initial.isLoading) awaitItem() else initial

            assertFalse(state.isLoading)
            assertEquals(1.0f, state.calibrationMultiplier)
            assertEquals(100.0f, state.dailyGoalMeters)
            assertEquals(440.0f, state.devicePpi)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `UpdateCalibration intent delegates to deviceMetricsProvider`() = runTest(testDispatcher) {
        coEvery { deviceMetricsProvider.updateCalibrationMultiplier(1.05f) } returns Unit

        viewModel.onIntent(SettingsIntent.UpdateCalibration(1.05f))
        testScheduler.advanceUntilIdle()

        coVerify(exactly = 1) { deviceMetricsProvider.updateCalibrationMultiplier(1.05f) }
    }

    @Test
    fun `UpdateDailyGoal intent delegates to settingsDataStore`() = runTest(testDispatcher) {
        coEvery { settingsDataStore.setDailyGoalMeters(250.0f) } returns Unit

        viewModel.onIntent(SettingsIntent.UpdateDailyGoal(250.0f))
        testScheduler.advanceUntilIdle()

        coVerify(exactly = 1) { settingsDataStore.setDailyGoalMeters(250.0f) }
    }

    @Test
    fun `ResetDailyTelemetry intent calls syncUsageStatsScrollUseCase with force reset`() = runTest(testDispatcher) {
        coEvery { syncUsageStatsScrollUseCase(forceResetBaseline = true) } returns true

        viewModel.onIntent(SettingsIntent.ResetDailyTelemetry)
        testScheduler.advanceUntilIdle()

        coVerify(exactly = 1) { syncUsageStatsScrollUseCase(forceResetBaseline = true) }
    }

    @Test
    fun `UpdateLanguage intent delegates to settingsDataStore`() = runTest(testDispatcher) {
        coEvery { settingsDataStore.setSelectedLanguage("tr") } returns Unit

        viewModel.onIntent(SettingsIntent.UpdateLanguage("tr"))
        testScheduler.advanceUntilIdle()

        coVerify(exactly = 1) { settingsDataStore.setSelectedLanguage("tr") }
    }
}
