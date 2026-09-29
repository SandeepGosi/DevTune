package com.devtune.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devtune.domain.model.*
import com.devtune.domain.repository.AnimationRepository
import com.devtune.domain.repository.TemporarySessionRepository
import com.devtune.domain.repository.UserPreferencesRepository
import com.devtune.domain.usecase.*
import com.devtune.platform.TimeProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.round

data class SystemData(
    val systemState: SystemState?,
    val shizukuState: ShizukuState,
    val densityInfo: DisplayDensityInfo?,
    val scales: AnimationScales,
    val session: TemporaryAnimationSession?
)

data class HomeUiState(
    val systemState: SystemState? = null,
    val displayDensityInfo: DisplayDensityInfo? = null,
    val animationScales: AnimationScales = AnimationScales.DEFAULT,
    val pendingWindowScale: Float? = null,
    val pendingTransitionScale: Float? = null,
    val pendingAnimatorScale: Float? = null,
    val temporarySession: TemporaryAnimationSession? = null,
    val selectedDuration: TemporaryDuration = TemporaryDuration.THIRTY_MINUTES,
    val currentTimeMillis: Long = 0L,
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
) {
    val effectiveWindowScale: Float
        get() = pendingWindowScale ?: animationScales.windowScale

    val effectiveTransitionScale: Float
        get() = pendingTransitionScale ?: animationScales.transitionScale

    val effectiveAnimatorScale: Float
        get() = pendingAnimatorScale ?: animationScales.animatorScale

    val hasPendingChanges: Boolean
        get() = (pendingWindowScale != null && pendingWindowScale != animationScales.windowScale) ||
                (pendingTransitionScale != null && pendingTransitionScale != animationScales.transitionScale) ||
                (pendingAnimatorScale != null && pendingAnimatorScale != animationScales.animatorScale)

    val isTemporarySessionActive: Boolean
        get() = temporarySession != null && (
            temporarySession.status == SessionStatus.ACTIVE ||
            temporarySession.status == SessionStatus.RESTORE_PENDING ||
            temporarySession.status == SessionStatus.RESTORING ||
            temporarySession.status == SessionStatus.FAILED_TO_RESTORE
        )
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    getSystemStateUseCase: GetSystemStateUseCase,
    private val getShizukuStateUseCase: GetShizukuStateUseCase,
    getDisplayDensityInfoUseCase: GetDisplayDensityInfoUseCase,
    private val animationRepository: AnimationRepository,
    private val temporarySessionRepository: TemporarySessionRepository,
    private val startTemporarySessionUseCase: StartTemporarySessionUseCase,
    private val restoreTemporarySessionUseCase: RestoreTemporarySessionUseCase,
    private val timeProvider: TimeProvider,
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

    private val systemDataFlow = combine(
        getSystemStateUseCase(),
        getShizukuStateUseCase(),
        getDisplayDensityInfoUseCase(),
        animationRepository.getAnimationScales(),
        temporarySessionRepository.activeSession
    ) { systemState, shizukuState, densityInfo, scales, session ->
        SystemData(systemState, shizukuState, densityInfo, scales, session)
    }

    val uiState: StateFlow<HomeUiState> = combine(
        systemDataFlow,
        _uiState
    ) { data, currentUiState ->
        val writeAnimCap = if (data.shizukuState is ShizukuState.Authorized) {
            getCapabilityStateUseCase(PlatformCapability.WRITE_ANIMATION_SETTINGS)
        } else {
            CapabilityState.UNKNOWN
        }

        val now = timeProvider.currentTimeMillis()

        currentUiState.copy(
            systemState = data.systemState,
            displayDensityInfo = data.densityInfo,
            animationScales = data.scales,
            temporarySession = data.session,
            currentTimeMillis = now,
            shizukuState = data.shizukuState,
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

    init {
        observeSessionAndRecovery()
    }

    private fun observeSessionAndRecovery() {
        viewModelScope.launch {
            combine(
                temporarySessionRepository.activeSession,
                getShizukuStateUseCase()
            ) { session, shizukuState ->
                Pair(session, shizukuState)
            }.collect { (session, shizukuState) ->
                if (session == null) return@collect

                val now = timeProvider.currentTimeMillis()

                if (session.status == SessionStatus.APPLYING) {
                    reconcileApplyingCrash(session)
                    return@collect
                }

                if (session.isExpired(now) &&
                    (session.status == SessionStatus.ACTIVE ||
                     session.status == SessionStatus.RESTORE_PENDING ||
                     session.status == SessionStatus.FAILED_TO_RESTORE)
                ) {
                    if (shizukuState is ShizukuState.Authorized) {
                        restoreTemporarySessionUseCase()
                    } else if (session.status != SessionStatus.RESTORE_PENDING) {
                        temporarySessionRepository.updateStatus(SessionStatus.RESTORE_PENDING)
                    }
                }
            }
        }
    }

    private suspend fun reconcileApplyingCrash(session: TemporaryAnimationSession) {
        val actualResult = animationRepository.getAnimationScalesOnce()
        if (actualResult.isFailure) return
        val actual = actualResult.getOrThrow()

        when {
            actual == session.targetScales -> {
                temporarySessionRepository.updateStatus(SessionStatus.ACTIVE)
            }
            actual == session.originalScales -> {
                temporarySessionRepository.updateStatus(SessionStatus.CANCELLED)
                temporarySessionRepository.clearSession()
            }
            else -> {
                restoreTemporarySessionUseCase()
            }
        }
    }

    fun onPendingWindowScaleChange(rawScale: Float) {
        val snapped = snapToStep(rawScale)
        _uiState.update { it.copy(pendingWindowScale = snapped) }
    }

    fun onPendingTransitionScaleChange(rawScale: Float) {
        val snapped = snapToStep(rawScale)
        _uiState.update { it.copy(pendingTransitionScale = snapped) }
    }

    fun onPendingAnimatorScaleChange(rawScale: Float) {
        val snapped = snapToStep(rawScale)
        _uiState.update { it.copy(pendingAnimatorScale = snapped) }
    }

    fun onSelectProfilePreset(profile: AnimationScales) {
        _uiState.update {
            it.copy(
                pendingWindowScale = snapToStep(profile.windowScale),
                pendingTransitionScale = snapToStep(profile.transitionScale),
                pendingAnimatorScale = snapToStep(profile.animatorScale)
            )
        }
    }

    fun onSelectDuration(duration: TemporaryDuration) {
        _uiState.update { it.copy(selectedDuration = duration) }
    }

    fun onDiscardPendingChanges() {
        _uiState.update {
            it.copy(
                pendingWindowScale = null,
                pendingTransitionScale = null,
                pendingAnimatorScale = null
            )
        }
    }

    fun onApplyPendingChanges() {
        val current = _uiState.value
        if (!current.hasPendingChanges || current.isApplyingAnimation) return

        viewModelScope.launch {
            _uiState.update { it.copy(isApplyingAnimation = true) }
            val targetScales = AnimationScales(
                windowScale = current.effectiveWindowScale,
                transitionScale = current.effectiveTransitionScale,
                animatorScale = current.effectiveAnimatorScale
            )

            val result = setAnimationScalesUseCase(targetScales)

            if (result.isSuccess) {
                _uiState.update {
                    it.copy(
                        isApplyingAnimation = false,
                        pendingWindowScale = null,
                        pendingTransitionScale = null,
                        pendingAnimatorScale = null,
                        userMessage = "Animation scales updated to ${formatScale(targetScales.windowScale)} / ${formatScale(targetScales.transitionScale)} / ${formatScale(targetScales.animatorScale)}"
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

    fun onStartTemporarySession() {
        val current = _uiState.value
        if (current.isApplyingAnimation) return

        viewModelScope.launch {
            _uiState.update { it.copy(isApplyingAnimation = true) }
            val targetScales = AnimationScales(
                windowScale = current.effectiveWindowScale,
                transitionScale = current.effectiveTransitionScale,
                animatorScale = current.effectiveAnimatorScale
            )

            val result = startTemporarySessionUseCase(targetScales, current.selectedDuration)

            if (result.isSuccess) {
                _uiState.update {
                    it.copy(
                        isApplyingAnimation = false,
                        pendingWindowScale = null,
                        pendingTransitionScale = null,
                        pendingAnimatorScale = null,
                        userMessage = "Temporary profile active for ${current.selectedDuration.label}"
                    )
                }
            } else {
                val err = result.exceptionOrNull()?.message ?: "Failed to start temporary profile session"
                _uiState.update {
                    it.copy(
                        isApplyingAnimation = false,
                        userMessage = err
                    )
                }
            }
        }
    }

    fun onRestoreTemporarySession() {
        if (_uiState.value.isApplyingAnimation) return

        viewModelScope.launch {
            _uiState.update { it.copy(isApplyingAnimation = true) }
            val result = restoreTemporarySessionUseCase()

            if (result.isSuccess) {
                _uiState.update {
                    it.copy(
                        isApplyingAnimation = false,
                        userMessage = "Original animation scales restored"
                    )
                }
            } else {
                val err = result.exceptionOrNull()?.message ?: "Failed to restore original animation scales"
                _uiState.update {
                    it.copy(
                        isApplyingAnimation = false,
                        userMessage = err
                    )
                }
            }
        }
    }

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

    companion object {
        fun snapToStep(value: Float): Float {
            val clamped = value.coerceIn(0.0f, 2.0f)
            return (round(clamped * 4.0f) / 4.0f)
        }

        fun formatScale(scale: Float): String {
            return String.format(java.util.Locale.US, "%.2fx", scale)
        }

        fun formatRemainingTime(millis: Long): String {
            val totalSeconds = millis / 1000L
            val minutes = totalSeconds / 60L
            val seconds = totalSeconds % 60L
            return String.format(java.util.Locale.US, "%02d:%02d", minutes, seconds)
        }
    }
}
