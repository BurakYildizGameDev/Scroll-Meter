package com.scrollmeter.app.presentation.achievements

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scrollmeter.app.core.result.Result
import com.scrollmeter.app.domain.model.AchievementCategory
import com.scrollmeter.app.domain.usecase.GetAchievementsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class AchievementsViewModel @Inject constructor(
    private val getAchievementsUseCase: GetAchievementsUseCase
) : ViewModel() {

    var epochDaysSupplier: () -> Long = { LocalDate.now().toEpochDay() }

    private val selectedCategory = MutableStateFlow<AchievementCategory?>(null)
    private val refreshTrigger = MutableStateFlow(0)

    val uiState: StateFlow<AchievementsUiState> = combine(
        selectedCategory,
        refreshTrigger
    ) { category, _ -> category }
        .flatMapLatest { filterCategory ->
            val epoch = epochDaysSupplier()
            getAchievementsUseCase(epoch)
                .combine(selectedCategory) { result, category ->
                    when (result) {
                        is Result.Loading -> AchievementsUiState(isLoading = true)
                        is Result.Error -> AchievementsUiState(
                            isLoading = false,
                            errorMessage = result.throwable.message ?: "Failed to load achievements"
                        )
                        is Result.Success -> {
                            val allAchievements = result.data
                            val filtered = if (category != null) {
                                allAchievements.filter { it.category == category }
                            } else {
                                allAchievements
                            }
                            AchievementsUiState(
                                isLoading = false,
                                unlockedCount = allAchievements.count { it.isUnlocked },
                                totalCount = allAchievements.size,
                                selectedCategory = category,
                                achievements = filtered
                            )
                        }
                    }
                }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AchievementsUiState(isLoading = true)
        )

    fun onIntent(intent: AchievementsIntent) {
        when (intent) {
            is AchievementsIntent.FilterCategory -> {
                selectedCategory.value = intent.category
            }
            is AchievementsIntent.Refresh -> {
                refreshTrigger.value += 1
            }
        }
    }
}
