package com.scrollmeter.app.presentation.dashboard

import androidx.compose.runtime.Immutable
import com.scrollmeter.app.domain.model.AppScrollSession
import com.scrollmeter.app.domain.model.MetaphorMilestone
import com.scrollmeter.app.domain.model.PhysicalDistance

/**
 * Immutable UI State representation for the Dashboard screen.
 */
@Immutable
data class DashboardUiState(
    val totalDistance: PhysicalDistance = PhysicalDistance.ZERO,
    val todayMeters: Double = 0.0,
    val dailyGoalMeters: Float = 100.0f,
    val progressPercentage: Float = 0.0f,
    val currentMetaphor: MetaphorMilestone? = null,
    val nextMetaphor: MetaphorMilestone? = null,
    val metersRemainingToNext: Double = 0.0,
    val topApps: List<AppScrollSession> = emptyList(),
    val dominantApp: AppScrollSession? = null,
    val isServiceActive: Boolean = false,
    val isDemoMode: Boolean = false,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

/**
 * User intents and interactions dispatched from the Dashboard UI.
 */
sealed interface DashboardIntent {
    data object Refresh : DashboardIntent
    data class UpdateDailyGoal(val goalMeters: Float) : DashboardIntent
    data object OnGoalClicked : DashboardIntent
    data object OnAnalyticsClicked : DashboardIntent
    data object OnSettingsClicked : DashboardIntent
}
