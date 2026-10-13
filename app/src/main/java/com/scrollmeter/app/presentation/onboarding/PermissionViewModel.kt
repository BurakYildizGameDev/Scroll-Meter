package com.scrollmeter.app.presentation.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scrollmeter.app.domain.usecase.SeedDemoDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PermissionViewModel @Inject constructor(
    private val seedDemoDataUseCase: SeedDemoDataUseCase
) : ViewModel() {

    private val _isDemoLoading = MutableStateFlow(false)
    val isDemoLoading: StateFlow<Boolean> = _isDemoLoading.asStateFlow()

    fun activateDemoMode(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isDemoLoading.value = true
            try {
                seedDemoDataUseCase()
                onSuccess()
            } finally {
                _isDemoLoading.value = false
            }
        }
    }
}
