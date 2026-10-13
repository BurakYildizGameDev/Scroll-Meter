package com.scrollmeter.app.presentation.dashboard

import app.cash.turbine.test
import com.scrollmeter.app.core.result.Result
import com.scrollmeter.app.data.datastore.SettingsDataStore
import com.scrollmeter.app.domain.model.AppScrollSession
import com.scrollmeter.app.domain.model.PhysicalDistance
import com.scrollmeter.app.domain.usecase.CalculateMetaphorUseCase
import com.scrollmeter.app.domain.usecase.GetAppLeaderboardUseCase
import com.scrollmeter.app.domain.usecase.GetDailyTotalDistanceUseCase
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
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var getDailyTotalDistanceUseCase: GetDailyTotalDistanceUseCase
    private lateinit var calculateMetaphorUseCase: CalculateMetaphorUseCase
    private lateinit var getAppLeaderboardUseCase: GetAppLeaderboardUseCase
    private lateinit var settingsDataStore: SettingsDataStore
    private lateinit var viewModel: DashboardViewModel

    private val testEpochDay = 20000L

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        getDailyTotalDistanceUseCase = mockk()
        calculateMetaphorUseCase = CalculateMetaphorUseCase() // Real pure domain class
        getAppLeaderboardUseCase = mockk()
        settingsDataStore = mockk(relaxed = true)

        every { settingsDataStore.dailyGoalMeters } returns flowOf(100.0f)
        every { getAppLeaderboardUseCase(testEpochDay) } returns flowOf(Result.Success(emptyList()))
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `uiState emits 100 meters and correct progress percentage`() = runTest(testDispatcher) {
        // 100 meters = 100,000 millimeters
        val hundredMeters = PhysicalDistance(100_000.0)
        every { getDailyTotalDistanceUseCase(testEpochDay) } returns flowOf(Result.Success(hundredMeters))

        viewModel = DashboardViewModel(
            getDailyTotalDistanceUseCase = getDailyTotalDistanceUseCase,
            calculateMetaphorUseCase = calculateMetaphorUseCase,
            getAppLeaderboardUseCase = getAppLeaderboardUseCase,
            settingsDataStore = settingsDataStore
        ).apply {
            epochDaysSupplier = { testEpochDay }
        }

        viewModel.uiState.test {
            // Initial state is Loading
            val initial = awaitItem()
            // Subsequent combined emission
            val successState = if (initial.isLoading) awaitItem() else initial

            assertFalse(successState.isLoading)
            assertEquals(100.0, successState.todayMeters, 0.001)
            assertEquals(1.0f, successState.progressPercentage, 0.001f)
            assertNotNull(successState.currentMetaphor)
            assertEquals("Futbol Sahası", successState.currentMetaphor?.name)
        }
    }

    @Test
    fun `uiState correctly identifies dominant application`() = runTest(testDispatcher) {
        val sixtyMeters = PhysicalDistance(60_000.0)
        val sessions = listOf(
            AppScrollSession(
                packageName = "com.instagram.android",
                appDisplayName = "Instagram",
                totalPixels = 4000.0,
                physicalDistance = PhysicalDistance(40_000.0)
            ),
            AppScrollSession(
                packageName = "com.twitter.android",
                appDisplayName = "X",
                totalPixels = 2000.0,
                physicalDistance = PhysicalDistance(20_000.0)
            )
        )

        every { getDailyTotalDistanceUseCase(testEpochDay) } returns flowOf(Result.Success(sixtyMeters))
        every { getAppLeaderboardUseCase(testEpochDay) } returns flowOf(Result.Success(sessions))

        viewModel = DashboardViewModel(
            getDailyTotalDistanceUseCase = getDailyTotalDistanceUseCase,
            calculateMetaphorUseCase = calculateMetaphorUseCase,
            getAppLeaderboardUseCase = getAppLeaderboardUseCase,
            settingsDataStore = settingsDataStore
        ).apply {
            epochDaysSupplier = { testEpochDay }
        }

        viewModel.uiState.test {
            val initial = awaitItem()
            val state = if (initial.isLoading) awaitItem() else initial

            assertEquals("Instagram", state.dominantApp?.appDisplayName)
            assertEquals(2, state.topApps.size)
        }
    }
}
