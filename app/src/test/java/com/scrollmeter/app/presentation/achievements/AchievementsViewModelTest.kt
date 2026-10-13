package com.scrollmeter.app.presentation.achievements

import app.cash.turbine.test
import com.scrollmeter.app.core.result.Result
import com.scrollmeter.app.domain.model.Achievement
import com.scrollmeter.app.domain.model.AchievementCategory
import com.scrollmeter.app.domain.usecase.GetAchievementsUseCase
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
class AchievementsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var getAchievementsUseCase: GetAchievementsUseCase
    private lateinit var viewModel: AchievementsViewModel

    private val testEpochDay = 20000L

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        getAchievementsUseCase = mockk()
        viewModel = AchievementsViewModel(getAchievementsUseCase).apply {
            epochDaysSupplier = { testEpochDay }
        }
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `uiState emits unlocked achievements count and items accurately`() = runTest(testDispatcher) {
        val fakeAchievements = listOf(
            Achievement("1", "First Step", "desc", "🥉", AchievementCategory.MILESTONE, true, 1.0f, "Done"),
            Achievement("2", "Giraffe", "desc", "🦒", AchievementCategory.MILESTONE, true, 1.0f, "Done"),
            Achievement("3", "Burj Khalifa", "desc", "🏙️", AchievementCategory.MILESTONE, false, 0.5f, "Halfway")
        )

        every { getAchievementsUseCase(testEpochDay) } returns flowOf(Result.Success(fakeAchievements))

        viewModel.uiState.test {
            val initial = awaitItem()
            val state = if (initial.isLoading) awaitItem() else initial

            assertFalse(state.isLoading)
            assertEquals(2, state.unlockedCount)
            assertEquals(3, state.totalCount)
            assertEquals(3, state.achievements.size)

            cancelAndIgnoreRemainingEvents()
        }
    }
}
