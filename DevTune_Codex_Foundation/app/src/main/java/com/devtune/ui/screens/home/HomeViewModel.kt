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
import com.devtune.domain.usecase.OpenDeviceInfoSettingsUseCase
import kotlinx.coroutines.flow.*

data class HomeUiState(
    val systemState: SystemState? = null,
    val canOpenDevOptions: CapabilityState = CapabilityState.UNKNOWN,
    val showDevOptionsInstructions: Boolean = false,
    val isLoading: Boolean = true
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    getSystemStateUseCase: GetSystemStateUseCase,
    private val getCapabilityStateUseCase: GetCapabilityStateUseCase,
    private val openDeveloperOptionsUseCase: OpenDeveloperOptionsUseCase,
    private val openDeviceInfoSettingsUseCase: OpenDeviceInfoSettingsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = combine(
        getSystemStateUseCase(),
        _uiState
    ) { systemState, currentUiState ->
        currentUiState.copy(
            systemState = systemState,
            canOpenDevOptions = getCapabilityStateUseCase(PlatformCapability.OPEN_DEVELOPER_OPTIONS),
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState()
    )

    fun onDeveloperOptionsClick() {
        val currentState = uiState.value.systemState ?: return
        if (currentState.isDeveloperOptionsEnabled) {
            openDeveloperOptionsUseCase()
        } else {
            _uiState.update { it.copy(showDevOptionsInstructions = true) }
        }
    }

    fun onDismissInstructions() {
        _uiState.update { it.copy(showDevOptionsInstructions = false) }
    }

    fun onOpenDeviceInfoClick() {
        openDeviceInfoSettingsUseCase()
        onDismissInstructions()
    }
}

