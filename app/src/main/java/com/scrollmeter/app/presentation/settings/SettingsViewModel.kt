package com.scrollmeter.app.presentation.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scrollmeter.app.data.datastore.SettingsDataStore
import com.scrollmeter.app.domain.model.TrackingMode
import com.scrollmeter.app.domain.repository.IDeviceMetricsProvider
import com.scrollmeter.app.domain.repository.IUsageStatsProvider
import com.scrollmeter.app.domain.usecase.SyncUsageStatsScrollUseCase
import com.scrollmeter.app.presentation.onboarding.isAccessibilityPermissionGranted
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
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
    private val deviceMetricsProvider: IDeviceMetricsProvider,
    private val usageStatsProvider: IUsageStatsProvider,
    private val syncUsageStatsScrollUseCase: SyncUsageStatsScrollUseCase
) : ViewModel() {

    private val _effect = MutableSharedFlow<SettingsEffect>()
    val effect: SharedFlow<SettingsEffect> = _effect.asSharedFlow()

    private val permissionProbe = MutableStateFlow(Pair(false, false))

    fun checkPermissions(context: Context) {
        val usage = usageStatsProvider.isUsageAccessGranted()
        val accessibility = isAccessibilityPermissionGranted(context)
        permissionProbe.value = Pair(usage, accessibility)
    }

    private data class BaseSettings(
        val multiplier: Float,
        val goal: Float,
        val ppi: Float,
        val theme: Int
    )

    private val baseSettingsFlow = combine(
        settingsDataStore.calibrationMultiplier,
        settingsDataStore.dailyGoalMeters,
        deviceMetricsProvider.getDevicePpi(),
        settingsDataStore.themeMode
    ) { multiplier, goal, ppi, theme ->
        BaseSettings(multiplier, goal, ppi, theme)
    }

    val uiState: StateFlow<SettingsUiState> = combine(
        baseSettingsFlow,
        settingsDataStore.trackingMode,
        settingsDataStore.cadenceSpeedMultiplier,
        permissionProbe
    ) { base, trackingMode, speed, perms ->
        SettingsUiState(
            calibrationMultiplier = base.multiplier,
            dailyGoalMeters = base.goal,
            devicePpi = base.ppi,
            themeMode = base.theme,
            trackingMode = trackingMode,
            cadenceSpeedMultiplier = speed,
            isUsageAccessGranted = perms.first,
            isAccessibilityGranted = perms.second,
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
                is SettingsIntent.UpdateTrackingMode -> {
                    settingsDataStore.setTrackingMode(intent.mode)
                    if (intent.mode == TrackingMode.SAFE_USAGE_STATS) {
                        syncUsageStatsScrollUseCase()
                    }
                    _effect.emit(SettingsEffect.ShowSnackbar("Motor değiştirildi: ${intent.mode.name}"))
                }
                is SettingsIntent.UpdateCadenceSpeed -> {
                    settingsDataStore.setCadenceSpeedMultiplier(intent.multiplier)
                    syncUsageStatsScrollUseCase()
                }
                is SettingsIntent.ResetCalibration -> {
                    deviceMetricsProvider.updateCalibrationMultiplier(1.0f)
                    _effect.emit(SettingsEffect.ShowSnackbar("Calibration reset to 1.00x default"))
                }
                is SettingsIntent.ResetDailyTelemetry -> {
                    syncUsageStatsScrollUseCase(forceResetBaseline = true)
                    _effect.emit(SettingsEffect.ShowSnackbar("Bugünkü telemetri sıfırlandı."))
                }
            }
        }
    }
}
