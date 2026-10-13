package com.scrollmeter.app.presentation.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scrollmeter.app.core.result.Result
import com.scrollmeter.app.domain.usecase.GetAppLeaderboardUseCase
import com.scrollmeter.app.domain.usecase.GetDailyTotalDistanceUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val getDailyTotalDistanceUseCase: GetDailyTotalDistanceUseCase,
    private val getAppLeaderboardUseCase: GetAppLeaderboardUseCase
) : ViewModel() {

    var epochDaysSupplier: () -> Long = { LocalDate.now().toEpochDay() }

    private val selectedTimeRange = MutableStateFlow(TimeRange.TODAY)
    private val refreshTrigger = MutableStateFlow(0)

    private val _effect = MutableSharedFlow<AnalyticsEffect>()
    val effect: SharedFlow<AnalyticsEffect> = _effect.asSharedFlow()

    val uiState: StateFlow<AnalyticsUiState> = combine(
        selectedTimeRange,
        refreshTrigger
    ) { range, _ -> range }
        .flatMapLatest { range ->
            val baseEpoch = epochDaysSupplier()
            val targetEpoch = when (range) {
                TimeRange.TODAY -> baseEpoch
                TimeRange.YESTERDAY -> baseEpoch - 1L
            }

            combine(
                getDailyTotalDistanceUseCase(targetEpoch),
                getAppLeaderboardUseCase(targetEpoch, limit = 50)
            ) { distanceResult, leaderboardResult ->
                when {
                    distanceResult is Result.Loading || leaderboardResult is Result.Loading -> {
                        AnalyticsUiState(isLoading = true, selectedTimeRange = range)
                    }
                    distanceResult is Result.Error -> {
                        AnalyticsUiState(
                            isLoading = false,
                            selectedTimeRange = range,
                            error = distanceResult.throwable.message ?: "Failed to load distance metrics"
                        )
                    }
                    leaderboardResult is Result.Error -> {
                        AnalyticsUiState(
                            isLoading = false,
                            selectedTimeRange = range,
                            error = leaderboardResult.throwable.message ?: "Failed to load app leaderboard"
                        )
                    }
                    distanceResult is Result.Success && leaderboardResult is Result.Success -> {
                        val totalDistance = distanceResult.data
                        val totalMeters = totalDistance.inMeters
                        val sessions = leaderboardResult.data

                        val appUiModels = sessions.map { session ->
                            val appMeters = session.physicalDistance.inMeters
                            val pct = if (totalMeters > 0.0) {
                                ((appMeters / totalMeters) * 100.0).toFloat().coerceIn(0f, 100f)
                            } else {
                                0f
                            }
                            AppScrollItemUiModel(
                                packageName = session.packageName,
                                appName = session.appDisplayName,
                                distanceMeters = appMeters,
                                percentage = pct,
                                formattedDistance = session.physicalDistance.formattedAdaptive,
                                totalPixels = session.totalPixels
                            )
                        }

                        AnalyticsUiState(
                            isLoading = false,
                            totalDistanceMeters = totalMeters,
                            formattedTotalDistance = totalDistance.formattedAdaptive,
                            apps = appUiModels,
                            top5Apps = appUiModels.take(5),
                            selectedTimeRange = range
                        )
                    }
                    else -> AnalyticsUiState(isLoading = false, selectedTimeRange = range)
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AnalyticsUiState(isLoading = true)
        )

    fun onIntent(intent: AnalyticsIntent) {
        when (intent) {
            is AnalyticsIntent.SelectTimeRange -> {
                selectedTimeRange.value = intent.range
            }
            is AnalyticsIntent.Refresh -> {
                refreshTrigger.value += 1
            }
        }
    }
}
