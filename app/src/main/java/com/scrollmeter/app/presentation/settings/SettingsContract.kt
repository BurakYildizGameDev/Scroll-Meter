package com.scrollmeter.app.presentation.settings

import androidx.compose.runtime.Immutable

@Immutable
data class SettingsUiState(
    val calibrationMultiplier: Float = 1.0f,
    val dailyGoalMeters: Float = 100.0f,
    val devicePpi: Float = 400.0f,
    val themeMode: Int = 0, // 0 = System, 1 = Dark, 2 = Light
    val isLoading: Boolean = false
)

sealed interface SettingsIntent {
    data class UpdateCalibration(val multiplier: Float) : SettingsIntent
    data class UpdateDailyGoal(val goalMeters: Float) : SettingsIntent
    data class UpdateThemeMode(val mode: Int) : SettingsIntent
    data object ResetCalibration : SettingsIntent
}

sealed interface SettingsEffect {
    data class ShowSnackbar(val message: String) : SettingsEffect
}
