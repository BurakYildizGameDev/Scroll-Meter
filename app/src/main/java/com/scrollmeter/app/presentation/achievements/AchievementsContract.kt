package com.scrollmeter.app.presentation.achievements

import androidx.compose.runtime.Immutable
import com.scrollmeter.app.domain.model.Achievement
import com.scrollmeter.app.domain.model.AchievementCategory

@Immutable
data class AchievementsUiState(
    val isLoading: Boolean = true,
    val unlockedCount: Int = 0,
    val totalCount: Int = 9,
    val selectedCategory: AchievementCategory? = null,
    val achievements: List<Achievement> = emptyList(),
    val errorMessage: String? = null
)

sealed interface AchievementsIntent {
    data class FilterCategory(val category: AchievementCategory?) : AchievementsIntent
    data object Refresh : AchievementsIntent
}
