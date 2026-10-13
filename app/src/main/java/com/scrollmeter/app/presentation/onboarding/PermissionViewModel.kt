package com.scrollmeter.app.presentation.onboarding

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scrollmeter.app.data.datastore.SettingsDataStore
import com.scrollmeter.app.domain.model.TrackingMode
import com.scrollmeter.app.domain.repository.IUsageStatsProvider
import com.scrollmeter.app.domain.usecase.SeedDemoDataUseCase
import com.scrollmeter.app.domain.usecase.SyncUsageStatsScrollUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PermissionUiState(
    val isUsageAccessGranted: Boolean = false,
    val isAccessibilityGranted: Boolean = false,
    val isDemoLoading: Boolean = false,
    val isSyncing: Boolean = false
)

@HiltViewModel
class PermissionViewModel @Inject constructor(
    private val usageStatsProvider: IUsageStatsProvider,
    private val syncUsageStatsScrollUseCase: SyncUsageStatsScrollUseCase,
    private val seedDemoDataUseCase: SeedDemoDataUseCase,
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(PermissionUiState())
    val uiState: StateFlow<PermissionUiState> = _uiState.asStateFlow()

    fun refreshPermissions(context: Context) {
        val usageGranted = usageStatsProvider.isUsageAccessGranted()
        val accessibilityGranted = isAccessibilityPermissionGranted(context)
        _uiState.update {
            it.copy(
                isUsageAccessGranted = usageGranted,
                isAccessibilityGranted = accessibilityGranted
            )
        }
    }

    fun selectSafeMode(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncing = true) }
            try {
                settingsDataStore.setTrackingMode(TrackingMode.SAFE_USAGE_STATS)
                settingsDataStore.setDemoMode(false)
                settingsDataStore.setOnboardingCompleted(true)
                syncUsageStatsScrollUseCase()
                onSuccess()
            } finally {
                _uiState.update { it.copy(isSyncing = false) }
            }
        }
    }

    fun selectAccessibilityMode(onSuccess: () -> Unit) {
        viewModelScope.launch {
            settingsDataStore.setTrackingMode(TrackingMode.ACCESSIBILITY_LAB)
            settingsDataStore.setDemoMode(false)
            settingsDataStore.setOnboardingCompleted(true)
            onSuccess()
        }
    }

    fun activateDemoMode(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isDemoLoading = true) }
            try {
                seedDemoDataUseCase()
                onSuccess()
            } finally {
                _uiState.update { it.copy(isDemoLoading = false) }
            }
        }
    }
}
