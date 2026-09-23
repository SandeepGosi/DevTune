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
    val displayDensityInfo: com.devtune.domain.model.DisplayDensityInfo? = null,
    val shizukuState: ShizukuState = ShizukuState.Unknown,
    val canOpenDevOptions: CapabilityState = CapabilityState.UNKNOWN,
    val writeAnimationCapability: CapabilityState = CapabilityState.UNKNOWN,
    val readDensityCapability: CapabilityState = CapabilityState.UNKNOWN,
    val writeDensityCapability: CapabilityState = CapabilityState.UNKNOWN,
    val experimentResult: AnimationExperimentResult? = null,
    val isExperimentRunning: Boolean = false,
    val showDevOptionsInstructions: Boolean = false,
    val showAnimationInstructions: Boolean = false,
    val showDensityInstructions: Boolean = false,
    val showDensityDevOptionsDisabled: Boolean = false,
    val showDensityDetailSheet: Boolean = false,
    val isLoading: Boolean = true
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    getSystemStateUseCase: GetSystemStateUseCase,
    getShizukuStateUseCase: GetShizukuStateUseCase,
    getDisplayDensityInfoUseCase: GetDisplayDensityInfoUseCase,
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
        getDisplayDensityInfoUseCase(),
        _uiState
    ) { systemState, shizukuState, densityInfo, currentUiState ->
        currentUiState.copy(
            systemState = systemState,
            displayDensityInfo = densityInfo,
            shizukuState = shizukuState,
            canOpenDevOptions = getCapabilityStateUseCase(PlatformCapability.OPEN_DEVELOPER_OPTIONS),
            writeAnimationCapability = getCapabilityStateUseCase(PlatformCapability.WRITE_ANIMATION_SETTINGS),
            readDensityCapability = getCapabilityStateUseCase(PlatformCapability.READ_DISPLAY_DENSITY),
            writeDensityCapability = getCapabilityStateUseCase(PlatformCapability.WRITE_DISPLAY_DENSITY),
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
            
            val shizukuState = uiState.value.shizukuState
            val currentBackend = if (shizukuState is ShizukuState.Authorized) shizukuState.backend.name else "NONE"
            val androidVersion = android.os.Build.VERSION.SDK_INT
            val success = result is AnimationExperimentResult.Success

            userPreferencesRepository.recordAnimationWriteVerification(
                backend = currentBackend,
                androidVersion = androidVersion,
                success = success
            )
            
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
        _uiState.update { it.copy(showDensityDetailSheet = true) }
    }

    fun onDismissDensityDetailSheet() {
        _uiState.update { it.copy(showDensityDetailSheet = false) }
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
            showDensityDevOptionsDisabled = false,
            showDensityDetailSheet = false
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

