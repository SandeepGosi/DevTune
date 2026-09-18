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
import com.devtune.domain.model.AnimationExperimentResult
import com.devtune.domain.model.ShizukuState
import com.devtune.domain.repository.UserPreferencesRepository
import com.devtune.domain.usecase.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class HomeUiState(
    val systemState: SystemState? = null,
    val shizukuState: ShizukuState = ShizukuState.Unknown,
    val canOpenDevOptions: CapabilityState = CapabilityState.UNKNOWN,
    val writeAnimationCapability: CapabilityState = CapabilityState.UNKNOWN,
    val experimentResult: AnimationExperimentResult? = null,
    val isExperimentRunning: Boolean = false,
    val showDevOptionsInstructions: Boolean = false,
    val showAnimationInstructions: Boolean = false,
    val showDensityInstructions: Boolean = false,
    val showDensityDevOptionsDisabled: Boolean = false,
    val isLoading: Boolean = true
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    getSystemStateUseCase: GetSystemStateUseCase,
    getShizukuStateUseCase: GetShizukuStateUseCase,
    private val getCapabilityStateUseCase: GetCapabilityStateUseCase,
    private val openDeveloperOptionsUseCase: OpenDeveloperOptionsUseCase,
    private val openDeviceInfoSettingsUseCase: OpenDeviceInfoSettingsUseCase,
    private val openDisplaySettingsUseCase: OpenDisplaySettingsUseCase,
    private val requestShizukuPermissionUseCase: RequestShizukuPermissionUseCase,
    private val verifyAnimationWriteUseCase: VerifyAnimationWriteUseCase,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = combine(
        getSystemStateUseCase(),
        getShizukuStateUseCase(),
        _uiState
    ) { systemState, shizukuState, currentUiState ->
        currentUiState.copy(
            systemState = systemState,
            shizukuState = shizukuState,
            canOpenDevOptions = getCapabilityStateUseCase(PlatformCapability.OPEN_DEVELOPER_OPTIONS),
            writeAnimationCapability = getCapabilityStateUseCase(PlatformCapability.WRITE_ANIMATION_SETTINGS),
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState()
    )

    fun onRunAnimationExperiment() {
        if (uiState.value.isExperimentRunning) return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isExperimentRunning = true, experimentResult = null) }
            val result = verifyAnimationWriteUseCase()
            
            if (result is AnimationExperimentResult.Success) {
                userPreferencesRepository.setAnimationWriteVerified(true)
            }
            
            _uiState.update { it.copy(isExperimentRunning = false, experimentResult = result) }
        }
    }

    fun onDismissExperimentResult() {
        _uiState.update { it.copy(experimentResult = null) }
    }

    fun onDeveloperOptionsClick() {
        val currentState = uiState.value.systemState ?: return
        if (currentState.isDeveloperOptionsEnabled) {
            openDeveloperOptionsUseCase()
        } else {
            _uiState.update { it.copy(showDevOptionsInstructions = true) }
        }
    }

    fun onAnimationSettingsClick() {
        _uiState.update { it.copy(showAnimationInstructions = true) }
    }

    fun onDensitySettingsClick() {
        val currentState = uiState.value.systemState ?: return
        if (currentState.isDeveloperOptionsEnabled) {
            _uiState.update { it.copy(showDensityInstructions = true) }
        } else {
            _uiState.update { it.copy(showDensityDevOptionsDisabled = true) }
        }
    }

    fun onAdvancedControlsClick() {
        val currentState = uiState.value.shizukuState
        if (currentState is ShizukuState.PermissionRequired) {
            viewModelScope.launch {
                requestShizukuPermissionUseCase()
            }
        } else if (currentState is ShizukuState.NotInstalled || currentState is ShizukuState.Stopped) {
            // TODO: Navigate to info/guide screen in future
        }
    }

    fun onDismissInstructions() {
        _uiState.update { it.copy(
            showDevOptionsInstructions = false,
            showAnimationInstructions = false,
            showDensityInstructions = false,
            showDensityDevOptionsDisabled = false
        ) }
    }

    fun onOpenDeviceInfoClick() {
        openDeviceInfoSettingsUseCase()
        onDismissInstructions()
    }

    fun onOpenDeveloperOptionsClick() {
        openDeveloperOptionsUseCase()
        onDismissInstructions()
    }

    fun onOpenDisplaySettingsClick() {
        openDisplaySettingsUseCase()
        onDismissInstructions()
    }
}

