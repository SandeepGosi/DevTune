package com.devtune.ui.screens.home

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

import androidx.lifecycle.viewModelScope
import com.devtune.domain.model.CapabilityState
import com.devtune.domain.model.PlatformCapability
import com.devtune.domain.model.SystemState
import com.devtune.domain.usecase.GetCapabilityStateUseCase
import com.devtune.domain.usecase.GetSystemStateUseCase
import com.devtune.domain.usecase.OpenDeveloperOptionsUseCase
import kotlinx.coroutines.flow.*

data class HomeUiState(
    val systemState: SystemState? = null,
    val canOpenDevOptions: CapabilityState = CapabilityState.UNKNOWN,
    val isLoading: Boolean = true
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    getSystemStateUseCase: GetSystemStateUseCase,
    private val getCapabilityStateUseCase: GetCapabilityStateUseCase,
    private val openDeveloperOptionsUseCase: OpenDeveloperOptionsUseCase
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = getSystemStateUseCase()
        .map { systemState ->
            HomeUiState(
                systemState = systemState,
                canOpenDevOptions = getCapabilityStateUseCase(PlatformCapability.OPEN_DEVELOPER_OPTIONS),
                isLoading = false
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = HomeUiState()
        )

    fun openDeveloperOptions() {
        openDeveloperOptionsUseCase()
    }
}

