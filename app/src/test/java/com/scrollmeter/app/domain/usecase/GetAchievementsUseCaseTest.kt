package com.scrollmeter.app.domain.usecase

import app.cash.turbine.test
import com.scrollmeter.app.core.result.Result
import com.scrollmeter.app.data.datastore.SettingsDataStore
import com.scrollmeter.app.domain.model.AppScrollSession
import com.scrollmeter.app.domain.model.PhysicalDistance
import com.scrollmeter.app.domain.repository.IScrollRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GetAchievementsUseCaseTest {

    private lateinit var getDailyTotalDistanceUseCase: GetDailyTotalDistanceUseCase
    private lateinit var scrollRepository: IScrollRepository
    private lateinit var settingsDataStore: SettingsDataStore

    private val testEpochDay = 20500L

    @BeforeEach
    fun setUp() {
        getDailyTotalDistanceUseCase = mockk()
        scrollRepository = mockk()
        settingsDataStore = mockk()
    }

    @Test
    fun `when daily distance is 150m, unlocks First Step and Giraffe Club and Liberty`() = runTest {
        val testDispatcher = UnconfinedTestDispatcher(testScheduler)
        val useCase = GetAchievementsUseCase(
            getDailyTotalDistanceUseCase = getDailyTotalDistanceUseCase,
            scrollRepository = scrollRepository,
            settingsDataStore = settingsDataStore,
            ioDispatcher = testDispatcher
        )

        // 150m = 150,000 mm
        val distance = PhysicalDistance(totalMilliMeters = 150_000.0)
        val sessions = listOf(
            AppScrollSession(
                packageName = "com.twitter.android",
                appDisplayName = "X",
                totalPixels = 1000.0,
                physicalDistance = PhysicalDistance(totalMilliMeters = 80_000.0)
            ),
            AppScrollSession(
                packageName = "com.instagram.android",
                appDisplayName = "Instagram",
                totalPixels = 500.0,
                physicalDistance = PhysicalDistance(totalMilliMeters = 70_000.0)
            )
        )

        every { getDailyTotalDistanceUseCase(testEpochDay) } returns flowOf(Result.Success(distance))
        every { scrollRepository.getDailyAppSessions(testEpochDay) } returns flowOf(sessions)
        every { settingsDataStore.calibrationMultiplier } returns flowOf(1.0f)

        useCase(testEpochDay).test {
            val item = awaitItem()
            assertTrue(item is Result.Success)
            val achievements = (item as Result.Success).data
            assertEquals(9, achievements.size)

            val firstStep = achievements.first { it.id == "FIRST_STEP" }
            val giraffe = achievements.first { it.id == "GIRAFFE_CLUB" }
            val liberty = achievements.first { it.id == "STATUE_OF_LIBERTY" }
            val eiffel = achievements.first { it.id == "EIFFEL_CLIMBER" }

            assertTrue(firstStep.isUnlocked)
            assertTrue(giraffe.isUnlocked)
            assertTrue(liberty.isUnlocked)
            assertFalse(eiffel.isUnlocked)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `when calibrated to 1_05x, unlocks Precision Tuner badge`() = runTest {
        val testDispatcher = UnconfinedTestDispatcher(testScheduler)
        val useCase = GetAchievementsUseCase(
            getDailyTotalDistanceUseCase = getDailyTotalDistanceUseCase,
            scrollRepository = scrollRepository,
            settingsDataStore = settingsDataStore,
            ioDispatcher = testDispatcher
        )

        val distance = PhysicalDistance(totalMilliMeters = 10_000.0) // 10m
        val sessions = emptyList<AppScrollSession>()

        every { getDailyTotalDistanceUseCase(testEpochDay) } returns flowOf(Result.Success(distance))
        every { scrollRepository.getDailyAppSessions(testEpochDay) } returns flowOf(sessions)
        every { settingsDataStore.calibrationMultiplier } returns flowOf(1.05f)

        useCase(testEpochDay).test {
            val item = awaitItem()
            assertTrue(item is Result.Success)
            val achievements = (item as Result.Success).data

            val precisionTuner = achievements.first { it.id == "PRECISION_TUNER" }
            assertTrue(precisionTuner.isUnlocked)
            assertEquals(1.0f, precisionTuner.progress)

            cancelAndIgnoreRemainingEvents()
        }
    }
}
