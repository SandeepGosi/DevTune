package com.devtune.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devtune.domain.model.*
import com.devtune.domain.repository.AnimationRepository
import com.devtune.domain.repository.UserPreferencesRepository
import com.devtune.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val systemState: SystemState? = null,
    val displayDensityInfo: DisplayDensityInfo? = null,
    val animationScales: AnimationScales = AnimationScales.DEFAULT,
    val shizukuState: ShizukuState = ShizukuState.Unknown,
    val canOpenDevOptions: CapabilityState = CapabilityState.UNKNOWN,
    val writeAnimationCapability: CapabilityState = CapabilityState.UNKNOWN,
    val readDensityCapability: CapabilityState = CapabilityState.UNKNOWN,
    val writeDensityCapability: CapabilityState = CapabilityState.UNKNOWN,
    val experimentResult: AnimationExperimentResult? = null,
    val isExperimentRunning: Boolean = false,
    val isApplyingAnimation: Boolean = false,
    val userMessage: String? = null,
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
    animationRepository: AnimationRepository,
    private val getCapabilityStateUseCase: GetCapabilityStateUseCase,
    private val openDeveloperOptionsUseCase: OpenDeveloperOptionsUseCase,
    private val openDeviceInfoSettingsUseCase: OpenDeviceInfoSettingsUseCase,
    private val openDisplaySettingsUseCase: OpenDisplaySettingsUseCase,
    private val requestShizukuPermissionUseCase: RequestShizukuPermissionUseCase,
    private val verifyAnimationWriteUseCase: VerifyAnimationWriteUseCase,
    private val setAnimationScalesUseCase: SetAnimationScalesUseCase,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = combine(
        getSystemStateUseCase(),
        getShizukuStateUseCase(),
        getDisplayDensityInfoUseCase(),
        animationRepository.getAnimationScales(),
        _uiState
    ) { systemState, shizukuState, densityInfo, scales, currentUiState ->
        val writeAnimCap = if (shizukuState is ShizukuState.Authorized) {
            getCapabilityStateUseCase(PlatformCapability.WRITE_ANIMATION_SETTINGS)
        } else {
            CapabilityState.UNKNOWN
        }

        currentUiState.copy(
            systemState = systemState,
            displayDensityInfo = densityInfo,
            animationScales = scales,
            shizukuState = shizukuState,
            canOpenDevOptions = getCapabilityStateUseCase(PlatformCapability.OPEN_DEVELOPER_OPTIONS),
            writeAnimationCapability = writeAnimCap,
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
            val success = result.isSuccess

            userPreferencesRepository.recordAnimationWriteVerification(
                backend = currentBackend,
                androidVersion = androidVersion,
                success = success
            )

            val experimentResult = if (success) {
                val orig = uiState.value.animationScales
                AnimationExperimentResult.Success(orig, 0.5f, 0.5f, orig)
            } else {
                val err = result.exceptionOrNull()?.message ?: "Verification failed"
                AnimationExperimentResult.Failure(
                    AnimationExperimentResult.ExperimentStage.VERIFICATION,
                    err,
                    uiState.value.animationScales,
                    uiState.value.animationScales
                )
            }

            _uiState.update { it.copy(isExperimentRunning = false, experimentResult = experimentResult) }
        }
    }

    fun onApplyAnimationScales(windowScale: Float, transitionScale: Float, animatorScale: Float) {
        if (uiState.value.isApplyingAnimation) return

        viewModelScope.launch {
            _uiState.update { it.copy(isApplyingAnimation = true) }
            val scales = AnimationScales(windowScale, transitionScale, animatorScale)
            val result = setAnimationScalesUseCase(scales)

            if (result.isSuccess) {
                _uiState.update {
                    it.copy(
                        isApplyingAnimation = false,
                        userMessage = "Animation scales updated to ${windowScale}x / ${transitionScale}x / ${animatorScale}x"
                    )
                }
            } else {
                val err = result.exceptionOrNull()?.message ?: "Failed to apply animation scales"
                _uiState.update {
                    it.copy(
                        isApplyingAnimation = false,
                        userMessage = err
                    )
                }
            }
        }
    }

    fun onApplyAnimationProfile(profile: AnimationScales) {
        onApplyAnimationScales(profile.windowScale, profile.transitionScale, profile.animatorScale)
    }

    fun onClearUserMessage() {
        _uiState.update { it.copy(userMessage = null) }
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
            // Setup guidance screen
        }
    }

    fun onDismissInstructions() {
        _uiState.update {
            it.copy(
                showDevOptionsInstructions = false,
                showAnimationInstructions = false,
                showDensityInstructions = false,
                showDensityDevOptionsDisabled = false,
                showDensityDetailSheet = false
            )
        }
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
