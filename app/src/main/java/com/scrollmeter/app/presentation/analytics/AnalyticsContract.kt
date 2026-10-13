package com.scrollmeter.app.presentation.analytics

import androidx.compose.runtime.Immutable

@Immutable
data class AppScrollItemUiModel(
    val packageName: String,
    val appName: String,
    val distanceMeters: Double,
    val percentage: Float,
    val formattedDistance: String,
    val totalPixels: Double
)

enum class TimeRange {
    TODAY,
    YESTERDAY
}

@Immutable
data class AnalyticsUiState(
    val isLoading: Boolean = true,
    val totalDistanceMeters: Double = 0.0,
    val formattedTotalDistance: String = "0.00 m",
    val apps: List<AppScrollItemUiModel> = emptyList(),
    val top5Apps: List<AppScrollItemUiModel> = emptyList(),
    val selectedTimeRange: TimeRange = TimeRange.TODAY,
    val error: String? = null
)

sealed interface AnalyticsIntent {
    data class SelectTimeRange(val range: TimeRange) : AnalyticsIntent
    data object Refresh : AnalyticsIntent
}

sealed interface AnalyticsEffect {
    data class ShowToast(val message: String) : AnalyticsEffect
}
