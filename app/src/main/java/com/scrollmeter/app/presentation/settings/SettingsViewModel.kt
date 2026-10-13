package com.scrollmeter.app.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scrollmeter.app.data.datastore.SettingsDataStore
import com.scrollmeter.app.domain.repository.IDeviceMetricsProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsDataStore: SettingsDataStore,
    private val deviceMetricsProvider: IDeviceMetricsProvider
) : ViewModel() {

    private val _effect = MutableSharedFlow<SettingsEffect>()
    val effect: SharedFlow<SettingsEffect> = _effect.asSharedFlow()

    val uiState: StateFlow<SettingsUiState> = combine(
        settingsDataStore.calibrationMultiplier,
        settingsDataStore.dailyGoalMeters,
        deviceMetricsProvider.getDevicePpi(),
        settingsDataStore.themeMode
    ) { multiplier, goal, ppi, theme ->
        SettingsUiState(
            calibrationMultiplier = multiplier,
            dailyGoalMeters = goal,
            devicePpi = ppi,
            themeMode = theme,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState(isLoading = true)
    )

    fun onIntent(intent: SettingsIntent) {
        viewModelScope.launch {
            when (intent) {
                is SettingsIntent.UpdateCalibration -> {
                    deviceMetricsProvider.updateCalibrationMultiplier(intent.multiplier)
                }
                is SettingsIntent.UpdateDailyGoal -> {
                    settingsDataStore.setDailyGoalMeters(intent.goalMeters)
                }
                is SettingsIntent.UpdateThemeMode -> {
                    settingsDataStore.setThemeMode(intent.mode)
                }
                is SettingsIntent.ResetCalibration -> {
                    deviceMetricsProvider.updateCalibrationMultiplier(1.0f)
                    _effect.emit(SettingsEffect.ShowSnackbar("Calibration reset to 1.00x default"))
                }
            }
        }
    }
}
