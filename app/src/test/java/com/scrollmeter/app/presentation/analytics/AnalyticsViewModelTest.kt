package com.scrollmeter.app.presentation.analytics

import app.cash.turbine.test
import com.scrollmeter.app.core.result.Result
import com.scrollmeter.app.domain.model.AppScrollSession
import com.scrollmeter.app.domain.model.PhysicalDistance
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
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AnalyticsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var getDailyTotalDistanceUseCase: GetDailyTotalDistanceUseCase
    private lateinit var getAppLeaderboardUseCase: GetAppLeaderboardUseCase
    private lateinit var viewModel: AnalyticsViewModel

    private val testEpochDay = 20000L

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        getDailyTotalDistanceUseCase = mockk()
        getAppLeaderboardUseCase = mockk()

        viewModel = AnalyticsViewModel(
            getDailyTotalDistanceUseCase = getDailyTotalDistanceUseCase,
            getAppLeaderboardUseCase = getAppLeaderboardUseCase
        ).apply {
            epochDaysSupplier = { testEpochDay }
        }
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `uiState calculates app percentages and top 5 items correctly`() = runTest(testDispatcher) {
        val totalDistance = PhysicalDistance(totalMilliMeters = 100_000.0) // 100 meters
        val sessions = listOf(
            AppScrollSession(
                packageName = "com.instagram.android",
                appDisplayName = "Instagram",
                totalPixels = 5000.0,
                physicalDistance = PhysicalDistance(60_000.0) // 60 meters (60%)
            ),
            AppScrollSession(
                packageName = "com.zhiliaoapp.musically",
                appDisplayName = "TikTok",
                totalPixels = 3000.0,
                physicalDistance = PhysicalDistance(30_000.0) // 30 meters (30%)
            ),
            AppScrollSession(
                packageName = "com.twitter.android",
                appDisplayName = "X",
                totalPixels = 1000.0,
                physicalDistance = PhysicalDistance(10_000.0) // 10 meters (10%)
            )
        )

        every { getDailyTotalDistanceUseCase(testEpochDay) } returns flowOf(Result.Success(totalDistance))
        every { getAppLeaderboardUseCase(testEpochDay, limit = 50) } returns flowOf(Result.Success(sessions))

        viewModel.uiState.test {
            val initial = awaitItem()
            val state = if (initial.isLoading) awaitItem() else initial

            assertFalse(state.isLoading)
            assertEquals(100.0, state.totalDistanceMeters, 0.01)
            assertEquals(3, state.apps.size)
            assertEquals(3, state.top5Apps.size)

            val topApp = state.apps.first()
            assertEquals("Instagram", topApp.appName)
            assertEquals(60.0f, topApp.percentage, 0.1f)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `selecting YESTERDAY queries previous epoch day`() = runTest(testDispatcher) {
        val yesterdayEpoch = testEpochDay - 1L
        val yesterdayDistance = PhysicalDistance(totalMilliMeters = 50_000.0) // 50m

        every { getDailyTotalDistanceUseCase(testEpochDay) } returns flowOf(Result.Success(PhysicalDistance.ZERO))
        every { getAppLeaderboardUseCase(testEpochDay, limit = 50) } returns flowOf(Result.Success(emptyList()))

        every { getDailyTotalDistanceUseCase(yesterdayEpoch) } returns flowOf(Result.Success(yesterdayDistance))
        every { getAppLeaderboardUseCase(yesterdayEpoch, limit = 50) } returns flowOf(Result.Success(emptyList()))

        viewModel.uiState.test {
            val initial = awaitItem()
            assertEquals(TimeRange.TODAY, initial.selectedTimeRange)

            viewModel.onIntent(AnalyticsIntent.SelectTimeRange(TimeRange.YESTERDAY))

            val updated = awaitItem()
            assertEquals(TimeRange.YESTERDAY, updated.selectedTimeRange)
            assertEquals(50.0, updated.totalDistanceMeters, 0.01)

            cancelAndIgnoreRemainingEvents()
        }
    }
}
