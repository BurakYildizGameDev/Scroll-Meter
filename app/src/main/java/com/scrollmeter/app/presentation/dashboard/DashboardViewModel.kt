package com.scrollmeter.app.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scrollmeter.app.core.result.Result
import com.scrollmeter.app.data.datastore.SettingsDataStore
import com.scrollmeter.app.domain.model.AppScrollSession
import com.scrollmeter.app.domain.model.PhysicalDistance
import com.scrollmeter.app.domain.model.TrackingMode
import com.scrollmeter.app.domain.usecase.CalculateMetaphorUseCase
import com.scrollmeter.app.domain.usecase.GetAppLeaderboardUseCase
import com.scrollmeter.app.domain.usecase.GetDailyTotalDistanceUseCase
import com.scrollmeter.app.domain.usecase.SyncUsageStatsScrollUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val getDailyTotalDistanceUseCase: GetDailyTotalDistanceUseCase,
    private val calculateMetaphorUseCase: CalculateMetaphorUseCase,
    private val getAppLeaderboardUseCase: GetAppLeaderboardUseCase,
    private val syncUsageStatsScrollUseCase: SyncUsageStatsScrollUseCase,
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {

    // Open for unit testing with fixed epoch date
    var epochDaysSupplier: () -> Long = { LocalDate.now().toEpochDay() }

    private val refreshTrigger = MutableStateFlow(0)

    init {
        // Automatically sync if in SAFE_USAGE_STATS mode
        viewModelScope.launch {
            if (settingsDataStore.trackingMode.first() == TrackingMode.SAFE_USAGE_STATS) {
                syncUsageStatsScrollUseCase()
            }
        }
    }

    private data class DashboardSettingsConfig(
        val goalMeters: Float,
        val isDemo: Boolean,
        val trackingMode: TrackingMode,
        val speedMultiplier: Float
    )

    private val settingsConfigFlow = combine(
        settingsDataStore.dailyGoalMeters,
        settingsDataStore.isDemoModeActive,
        settingsDataStore.trackingMode,
        settingsDataStore.cadenceSpeedMultiplier
    ) { goal, isDemo, mode, speed ->
        DashboardSettingsConfig(goal, isDemo, mode, speed)
    }

    val uiState: StateFlow<DashboardUiState> = refreshTrigger
        .flatMapLatest {
            val epoch = epochDaysSupplier()
            combine(
                getDailyTotalDistanceUseCase(epoch),
                getAppLeaderboardUseCase(epoch),
                settingsConfigFlow
            ) { distanceResult, leaderboardResult, config ->
                when (distanceResult) {
                    is Result.Loading -> {
                        DashboardUiState(
                            isLoading = true,
                            isDemoMode = config.isDemo,
                            trackingMode = config.trackingMode,
                            cadenceSpeedMultiplier = config.speedMultiplier
                        )
                    }
                    is Result.Error -> {
                        DashboardUiState(
                            isLoading = false,
                            isDemoMode = config.isDemo,
                            trackingMode = config.trackingMode,
                            cadenceSpeedMultiplier = config.speedMultiplier,
                            errorMessage = distanceResult.throwable.message ?: "Failed to load telemetry"
                        )
                    }
                    is Result.Success -> {
                        val distance = distanceResult.data
                        val todayMeters = distance.inMeters
                        val progressFraction = if (config.goalMeters > 0f) {
                            (todayMeters / config.goalMeters.toDouble()).toFloat().coerceAtLeast(0f)
                        } else {
                            0f
                        }
                        val metaphorProgress = calculateMetaphorUseCase(todayMeters)
                        val topApps = (leaderboardResult as? Result.Success)?.data ?: emptyList()

                        DashboardUiState(
                            totalDistance = distance,
                            todayMeters = todayMeters,
                            dailyGoalMeters = config.goalMeters,
                            progressPercentage = progressFraction,
                            currentMetaphor = metaphorProgress.currentMilestone,
                            nextMetaphor = metaphorProgress.nextMilestone,
                            metersRemainingToNext = metaphorProgress.remainingMetersToNext,
                            topApps = topApps,
                            dominantApp = topApps.firstOrNull(),
                            isDemoMode = config.isDemo,
                            trackingMode = config.trackingMode,
                            cadenceSpeedMultiplier = config.speedMultiplier,
                            isLoading = false
                        )
                    }
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DashboardUiState(isLoading = true)
        )

    fun processIntent(intent: DashboardIntent) {
        when (intent) {
            is DashboardIntent.Refresh -> {
                viewModelScope.launch {
                    if (settingsDataStore.trackingMode.first() == TrackingMode.SAFE_USAGE_STATS) {
                        syncUsageStatsScrollUseCase()
                    }
                    refreshTrigger.value += 1
                }
            }
            is DashboardIntent.UpdateDailyGoal -> {
                viewModelScope.launch {
                    settingsDataStore.setDailyGoalMeters(intent.goalMeters)
                }
            }
            DashboardIntent.OnGoalClicked -> {
                // Handled at navigation level
            }
            DashboardIntent.OnAnalyticsClicked -> {
                // Handled at navigation level
            }
            DashboardIntent.OnSettingsClicked -> {
                // Handled at navigation level
            }
        }
    }
}
