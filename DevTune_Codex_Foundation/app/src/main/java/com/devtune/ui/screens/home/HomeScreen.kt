package com.devtune.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.devtune.domain.model.*
import com.devtune.ui.components.*
import com.devtune.ui.theme.DevTuneTheme

@Composable
fun HomeRoute(
    onNavigateToSettings: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    HomeScreen(
        uiState = uiState,
        onNavigateToSettings = onNavigateToSettings,
        onOpenDevOptions = viewModel::onDeveloperOptionsClick,
        onAnimationSettingsClick = viewModel::onAnimationSettingsClick,
        onDensitySettingsClick = viewModel::onDensitySettingsClick,
        onAdvancedControlsClick = viewModel::onAdvancedControlsClick,
        onRunAnimationExperiment = viewModel::onRunAnimationExperiment,
        onPendingWindowScaleChange = viewModel::onPendingWindowScaleChange,
        onPendingTransitionScaleChange = viewModel::onPendingTransitionScaleChange,
        onPendingAnimatorScaleChange = viewModel::onPendingAnimatorScaleChange,
        onSelectProfilePreset = viewModel::onSelectProfilePreset,
        onSelectDuration = viewModel::onSelectDuration,
        onDiscardPendingChanges = viewModel::onDiscardPendingChanges,
        onApplyPendingChanges = viewModel::onApplyPendingChanges,
        onStartTemporarySession = viewModel::onStartTemporarySession,
        onRestoreTemporarySession = viewModel::onRestoreTemporarySession,
        onClearUserMessage = viewModel::onClearUserMessage,
        onDismissExperimentResult = viewModel::onDismissExperimentResult,
        onDismissInstructions = viewModel::onDismissInstructions,
        onConfirmOpenDeviceInfo = viewModel::onOpenDeviceInfoClick,
        onConfirmOpenDeveloperOptions = viewModel::onOpenDeveloperOptionsClick,
        onConfirmOpenDisplaySettings = viewModel::onOpenDisplaySettingsClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onNavigateToSettings: () -> Unit,
    onOpenDevOptions: () -> Unit,
    onAnimationSettingsClick: () -> Unit,
    onDensitySettingsClick: () -> Unit,
    onAdvancedControlsClick: () -> Unit,
    onRunAnimationExperiment: () -> Unit,
    onPendingWindowScaleChange: (Float) -> Unit,
    onPendingTransitionScaleChange: (Float) -> Unit,
    onPendingAnimatorScaleChange: (Float) -> Unit,
    onSelectProfilePreset: (AnimationScales) -> Unit,
    onSelectDuration: (TemporaryDuration) -> Unit,
    onDiscardPendingChanges: () -> Unit,
    onApplyPendingChanges: () -> Unit,
    onStartTemporarySession: () -> Unit,
    onRestoreTemporarySession: () -> Unit,
    onClearUserMessage: () -> Unit,
    onDismissExperimentResult: () -> Unit,
    onDismissInstructions: () -> Unit,
    onConfirmOpenDeviceInfo: () -> Unit,
    onConfirmOpenDeveloperOptions: () -> Unit,
    onConfirmOpenDisplaySettings: () -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            onClearUserMessage()
        }
    }

    if (uiState.experimentResult != null) {
        ExperimentResultDialog(
            result = uiState.experimentResult,
            onDismiss = onDismissExperimentResult
        )
    }

    if (uiState.showDensityDetailSheet && uiState.displayDensityInfo != null) {
        DisplayDensityDetailSheet(
            densityInfo = uiState.displayDensityInfo,
            onDismiss = onDismissInstructions,
            onOpenDisplaySettings = onConfirmOpenDisplaySettings
        )
    }

    if (uiState.showDevOptionsInstructions) {
        DeveloperOptionsInstructionDialog(
            onDismiss = onDismissInstructions,
            onConfirm = onConfirmOpenDeviceInfo
        )
    }

    if (uiState.showAnimationInstructions) {
        AnimationSettingsInstructionDialog(
            onDismiss = onDismissInstructions,
            onConfirm = onConfirmOpenDeveloperOptions
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            DevTuneTopAppBar(
                title = "",
                scrollBehavior = scrollBehavior,
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            HomeContent(
                uiState = uiState,
                paddingValues = paddingValues,
                onOpenDevOptions = onOpenDevOptions,
                onAnimationSettingsClick = onAnimationSettingsClick,
                onDensitySettingsClick = onDensitySettingsClick,
                onAdvancedControlsClick = onAdvancedControlsClick,
                onRunAnimationExperiment = onRunAnimationExperiment,
                onPendingWindowScaleChange = onPendingWindowScaleChange,
                onPendingTransitionScaleChange = onPendingTransitionScaleChange,
                onPendingAnimatorScaleChange = onPendingAnimatorScaleChange,
                onSelectProfilePreset = onSelectProfilePreset,
                onSelectDuration = onSelectDuration,
                onDiscardPendingChanges = onDiscardPendingChanges,
                onApplyPendingChanges = onApplyPendingChanges,
                onStartTemporarySession = onStartTemporarySession,
                onRestoreTemporarySession = onRestoreTemporarySession
            )
        }
    }
}

@Composable
private fun HomeContent(
    uiState: HomeUiState,
    paddingValues: PaddingValues,
    onOpenDevOptions: () -> Unit,
    onAnimationSettingsClick: () -> Unit,
    onDensitySettingsClick: () -> Unit,
    onAdvancedControlsClick: () -> Unit,
    onRunAnimationExperiment: () -> Unit,
    onPendingWindowScaleChange: (Float) -> Unit,
    onPendingTransitionScaleChange: (Float) -> Unit,
    onPendingAnimatorScaleChange: (Float) -> Unit,
    onSelectProfilePreset: (AnimationScales) -> Unit,
    onSelectDuration: (TemporaryDuration) -> Unit,
    onDiscardPendingChanges: () -> Unit,
    onApplyPendingChanges: () -> Unit,
    onStartTemporarySession: () -> Unit,
    onRestoreTemporarySession: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentPadding = PaddingValues(
            bottom = DevTuneTheme.spacing.huge,
            top = DevTuneTheme.spacing.small
        ),
        verticalArrangement = Arrangement.spacedBy(DevTuneTheme.spacing.medium)
    ) {
        item {
            DashboardHeader()
        }

        item {
            uiState.systemState?.let { state ->
                SystemStatusHero(state, uiState.shizukuState)
            }
        }

        if (uiState.isTemporarySessionActive) {
            item {
                ActiveTemporarySessionCard(
                    uiState = uiState,
                    onRestoreTemporarySession = onRestoreTemporarySession
                )
            }
        }

        item {
            Text(
                text = "System Controls",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = DevTuneTheme.spacing.large)
            )
        }

        item {
            uiState.systemState?.let { state ->
                Column(
                    modifier = Modifier.padding(horizontal = DevTuneTheme.spacing.screenPadding),
                    verticalArrangement = Arrangement.spacedBy(DevTuneTheme.spacing.medium)
                ) {
                    FeatureCard(
                        title = "Developer Options",
                        supportingText = if (state.isDeveloperOptionsEnabled)
                            "Tap to open Developer Options" else "Tap to learn how to enable",
                        icon = Icons.Default.Build,
                        onClick = onOpenDevOptions
                    ) {
                        StatusBadge(
                            text = if (state.isDeveloperOptionsEnabled) "Enabled" else "Disabled",
                            color = if (state.isDeveloperOptionsEnabled)
                                MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                        )
                    }

                    FeatureCard(
                        title = "Display Density",
                        supportingText = uiState.displayDensityInfo?.let {
                            "${it.smallestWidthDp} dp smallest width · ${it.resolutionString}"
                        } ?: "Current system metrics",
                        icon = Icons.Default.AspectRatio,
                        onClick = onDensitySettingsClick
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                uiState.displayDensityInfo?.let { info ->
                                    Text(
                                        text = "${info.currentDensityDpi} dpi · ${info.scaleString}",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Black,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                } ?: Text(
                                    text = "${state.displayDensityDpi} DPI",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                StatusBadge(
                                    text = "Read-only",
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }

                    AnimationScalesSliderCard(
                        uiState = uiState,
                        onAnimationSettingsClick = onAnimationSettingsClick,
                        onRunAnimationExperiment = onRunAnimationExperiment,
                        onPendingWindowScaleChange = onPendingWindowScaleChange,
                        onPendingTransitionScaleChange = onPendingTransitionScaleChange,
                        onPendingAnimatorScaleChange = onPendingAnimatorScaleChange,
                        onSelectDuration = onSelectDuration,
                        onDiscardPendingChanges = onDiscardPendingChanges,
                        onApplyPendingChanges = onApplyPendingChanges,
                        onStartTemporarySession = onStartTemporarySession
                    )
                }
            }
        }

        item {
            ProfilesPreviewModule(
                currentEffectiveScales = AnimationScales(
                    uiState.effectiveWindowScale,
                    uiState.effectiveTransitionScale,
                    uiState.effectiveAnimatorScale
                ),
                isAdvancedMode = uiState.shizukuState is ShizukuState.Authorized,
                onSelectProfilePreset = onSelectProfilePreset
            )
        }

        item {
            AdvancedControlsModule(
                state = uiState.shizukuState,
                onClick = onAdvancedControlsClick
            )
        }
    }
}

@Composable
private fun ActiveTemporarySessionCard(
    uiState: HomeUiState,
    onRestoreTemporarySession: () -> Unit
) {
    val session = uiState.temporarySession ?: return
    val remainingMs = session.remainingMillis(uiState.currentTimeMillis)
    val formattedRemaining = HomeViewModel.formatRemainingTime(remainingMs)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = DevTuneTheme.spacing.screenPadding),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        ),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Column(
            modifier = Modifier.padding(DevTuneTheme.spacing.large),
            verticalArrangement = Arrangement.spacedBy(DevTuneTheme.spacing.medium)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(DevTuneTheme.spacing.small)
                ) {
                    Icon(
                        Icons.Default.Timer,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Text(
                        text = "TEMPORARY PROFILE ACTIVE",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }

                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.tertiary
                ) {
                    Text(
                        text = formattedRemaining,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            if (session.status == SessionStatus.RESTORE_PENDING || session.status == SessionStatus.FAILED_TO_RESTORE) {
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.errorContainer
                ) {
                    Text(
                        text = "Restoration Pending — Shizuku authorization required to restore original animation scales.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(DevTuneTheme.spacing.medium)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Target", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f))
                    Text(
                        "${HomeViewModel.formatScale(session.targetScales.windowScale)} / ${HomeViewModel.formatScale(session.targetScales.transitionScale)} / ${HomeViewModel.formatScale(session.targetScales.animatorScale)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }

                Column {
                    Text("Original", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f))
                    Text(
                        "${HomeViewModel.formatScale(session.originalScales.windowScale)} / ${HomeViewModel.formatScale(session.originalScales.transitionScale)} / ${HomeViewModel.formatScale(session.originalScales.animatorScale)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }

            Button(
                onClick = onRestoreTemporarySession,
                enabled = !uiState.isApplyingAnimation,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiary
                )
            ) {
                if (uiState.isApplyingAnimation) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = MaterialTheme.colorScheme.onTertiary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(DevTuneTheme.spacing.small))
                    Text("Restoring...")
                } else {
                    Text("Restore Original Scales Now")
                }
            }
        }
    }
}

@Composable
private fun AnimationScalesSliderCard(
    uiState: HomeUiState,
    onAnimationSettingsClick: () -> Unit,
    onRunAnimationExperiment: () -> Unit,
    onPendingWindowScaleChange: (Float) -> Unit,
    onPendingTransitionScaleChange: (Float) -> Unit,
    onPendingAnimatorScaleChange: (Float) -> Unit,
    onSelectDuration: (TemporaryDuration) -> Unit,
    onDiscardPendingChanges: () -> Unit,
    onApplyPendingChanges: () -> Unit,
    onStartTemporarySession: () -> Unit
) {
    val isAdvancedMode = uiState.shizukuState is ShizukuState.Authorized

    FeatureCard(
        title = "Animation Scales",
        supportingText = when {
            isAdvancedMode && uiState.hasPendingChanges -> "Pending scale changes — Tap Apply to save"
            isAdvancedMode -> "Advanced mode active · Privileged Binder controls"
            uiState.shizukuState is ShizukuState.PermissionRequired -> "Authorization required for advanced controls"
            else -> "Read-only without Shizuku authorization"
        },
        icon = Icons.Default.Speed,
        tonalElevation = 1.dp
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(DevTuneTheme.spacing.medium)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(DevTuneTheme.spacing.large)
                ) {
                    AnimationValue(label = "Window", value = uiState.effectiveWindowScale)
                    AnimationValue(label = "Transition", value = uiState.effectiveTransitionScale)
                    AnimationValue(label = "Animator", value = uiState.effectiveAnimatorScale)
                }

                if (uiState.hasPendingChanges) {
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.tertiaryContainer
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                            Text(
                                text = "Unsaved",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                }
            }

            if (isAdvancedMode) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                AnimationScaleSliderRow(
                    label = "Window Animation",
                    value = uiState.effectiveWindowScale,
                    enabled = isAdvancedMode,
                    onValueChange = onPendingWindowScaleChange
                )

                AnimationScaleSliderRow(
                    label = "Transition Animation",
                    value = uiState.effectiveTransitionScale,
                    enabled = isAdvancedMode,
                    onValueChange = onPendingTransitionScaleChange
                )

                AnimationScaleSliderRow(
                    label = "Animator Duration",
                    value = uiState.effectiveAnimatorScale,
                    enabled = isAdvancedMode,
                    onValueChange = onPendingAnimatorScaleChange
                )

                if (!uiState.isTemporarySessionActive) {
                    Text(
                        text = "Temporary Duration",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        TemporaryDuration.entries.forEach { duration ->
                            FilterChip(
                                selected = uiState.selectedDuration == duration,
                                onClick = { onSelectDuration(duration) },
                                label = {
                                    Text(
                                        text = duration.label,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(DevTuneTheme.spacing.small)
                ) {
                    Button(
                        onClick = onApplyPendingChanges,
                        modifier = Modifier.weight(1f),
                        enabled = uiState.hasPendingChanges && !uiState.isApplyingAnimation
                    ) {
                        if (uiState.isApplyingAnimation) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Apply Permanent")
                        }
                    }

                    if (!uiState.isTemporarySessionActive) {
                        OutlinedButton(
                            onClick = onStartTemporarySession,
                            modifier = Modifier.weight(1f),
                            enabled = !uiState.isApplyingAnimation
                        ) {
                            Text("Apply ${uiState.selectedDuration.label}")
                        }
                    }

                    if (uiState.hasPendingChanges) {
                        OutlinedButton(
                            onClick = onDiscardPendingChanges,
                            enabled = !uiState.isApplyingAnimation
                        ) {
                            Text("Discard")
                        }
                    }
                }
            } else {
                Button(
                    onClick = onAnimationSettingsClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text("Open Developer Options")
                }
            }
        }
    }
}

@Composable
private fun AnimationScaleSliderRow(
    label: String,
    value: Float,
    enabled: Boolean,
    onValueChange: (Float) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = HomeViewModel.formatScale(value),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = 0.0f..2.0f,
            steps = 7,
            enabled = enabled,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun SystemStatusHero(state: SystemState, shizukuState: ShizukuState) {
    val isAdvanced = shizukuState is ShizukuState.Authorized
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = DevTuneTheme.spacing.screenPadding),
        colors = CardDefaults.cardColors(
            containerColor = if (isAdvanced) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.primaryContainer
        ),
        shape = MaterialTheme.shapes.extraLarge,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(DevTuneTheme.spacing.large)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "SYSTEM CONFIGURATION",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                )
                StatusBadge(
                    text = if (isAdvanced) "Advanced Mode" else "Standard Mode",
                    color = if (isAdvanced) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(DevTuneTheme.spacing.small))

            Text(
                text = if (isAdvanced) "Shizuku privileged control active" else if (state.isDeveloperOptionsEnabled) "Developer settings available" else "Standard Android configuration",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Spacer(modifier = Modifier.height(DevTuneTheme.spacing.large))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                HeroStat(label = "Density", value = "${state.displayDensityDpi} DPI")
                HeroStat(label = "Animations", value = state.animationSettings?.let { "${it.windowScale}x" } ?: "1.0x")
                HeroStat(label = "Mode", value = if (isAdvanced) "ADVANCED" else "STANDARD")
            }
        }
    }
}

@Composable
private fun HeroStat(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
private fun ProfilesPreviewModule(
    currentEffectiveScales: AnimationScales,
    isAdvancedMode: Boolean,
    onSelectProfilePreset: (AnimationScales) -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = DevTuneTheme.spacing.screenPadding),
        verticalArrangement = Arrangement.spacedBy(DevTuneTheme.spacing.medium)
    ) {
        Text(
            text = "Quick Profiles",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = DevTuneTheme.spacing.extraSmall)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(DevTuneTheme.spacing.small)
        ) {
            ProfileChip(
                name = "Default",
                value = "1.00x",
                selected = currentEffectiveScales == AnimationScales.DEFAULT,
                enabled = isAdvancedMode,
                onClick = { onSelectProfilePreset(AnimationScales.DEFAULT) },
                modifier = Modifier.weight(1f)
            )
            ProfileChip(
                name = "Fast",
                value = "0.50x",
                selected = currentEffectiveScales == AnimationScales.FAST,
                enabled = isAdvancedMode,
                onClick = { onSelectProfilePreset(AnimationScales.FAST) },
                modifier = Modifier.weight(1f)
            )
            ProfileChip(
                name = "No Anim",
                value = "0.00x",
                selected = currentEffectiveScales == AnimationScales.NO_ANIMATIONS,
                enabled = isAdvancedMode,
                onClick = { onSelectProfilePreset(AnimationScales.NO_ANIMATIONS) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ProfileChip(
    name: String,
    value: String,
    selected: Boolean = false,
    enabled: Boolean = true,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        border = if (selected) androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Column(
            modifier = Modifier.padding(DevTuneTheme.spacing.medium),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = name, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            Text(text = value, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun AdvancedControlsModule(
    state: ShizukuState,
    onClick: () -> Unit
) {
    val (statusTitle, statusDescription) = when (state) {
        is ShizukuState.NotInstalled -> Pair("Shizuku not detected", "Unlock advanced system controls")
        is ShizukuState.Stopped -> Pair("Shizuku not running", "Service is stopped")
        is ShizukuState.PermissionRequired -> Pair("Permission required", "Tap to grant Shizuku access")
        is ShizukuState.Authorized -> Pair("Advanced system access", "Shizuku connected via ${state.backend.name}")
        is ShizukuState.ConnectionLost -> Pair("Connection lost", "Shizuku service disconnected")
        else -> Pair("Advanced Controls", "Unknown Shizuku state")
    }

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = DevTuneTheme.spacing.screenPadding),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Row(
            modifier = Modifier.padding(DevTuneTheme.spacing.large),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(DevTuneTheme.spacing.large)
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = androidx.compose.foundation.shape.CircleShape,
                color = if (state is ShizukuState.Authorized) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.tertiaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Bolt,
                        contentDescription = null,
                        tint = if (state is ShizukuState.Authorized) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = statusTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = statusDescription,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (state is ShizukuState.PermissionRequired) {
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Composable
private fun AnimationValue(label: String, value: Float) {
    Column {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = HomeViewModel.formatScale(value), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun AnimationSettingsInstructionDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Animation Settings") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(DevTuneTheme.spacing.small)) {
                Text("Android requires Developer Options or Shizuku authorization for animation controls.")
                Text("In Developer Options:")
                val bullet = "\u2022"
                Text("$bullet Window animation scale", style = MaterialTheme.typography.bodySmall)
                Text("$bullet Transition animation scale", style = MaterialTheme.typography.bodySmall)
                Text("$bullet Animator duration scale", style = MaterialTheme.typography.bodySmall)
            }
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Open Developer Options")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun ExperimentResultDialog(
    result: AnimationExperimentResult,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (result is AnimationExperimentResult.Success) "Verification Successful" else "Verification Failed")
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(DevTuneTheme.spacing.small)) {
                when (result) {
                    is AnimationExperimentResult.Success -> {
                        Text("DevTune successfully verified privileged WindowManager animation scale controls.")
                        Text("\nAdvanced animation control is active.", fontWeight = FontWeight.Bold)
                    }
                    is AnimationExperimentResult.Failure -> {
                        Text("Verification failed: ${result.message}")
                        Text("\nAdvanced control remains restricted.", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
private fun DisplayDensityDetailSheet(
    densityInfo: com.devtune.domain.model.DisplayDensityInfo,
    onDismiss: () -> Unit,
    onOpenDisplaySettings: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Display Density Metrics") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(DevTuneTheme.spacing.small)
            ) {
                DensityMetricRow("Current Logical Density", "${densityInfo.currentDensityDpi} dpi (${densityInfo.scaleString})")
                DensityMetricRow("Smallest Width", "${densityInfo.smallestWidthDp} dp")
                DensityMetricRow("Screen Resolution", densityInfo.resolutionString)
                DensityMetricRow("Configuration Density", "${densityInfo.configurationDensityDpi} dpi")
                DensityMetricRow("DisplayMetrics Density", "${densityInfo.displayMetricsDensityDpi} dpi")
                DensityMetricRow("Forced Density Override", densityInfo.forcedDensityDpi?.let { "$it dpi" } ?: "Default Panel Density")

                Spacer(modifier = Modifier.height(DevTuneTheme.spacing.small))

                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Text(
                        text = "Display density writes are restricted on this Android version.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(DevTuneTheme.spacing.medium)
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                onOpenDisplaySettings()
                onDismiss()
            }) {
                Text("Open Display Settings")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
private fun DensityMetricRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun DeveloperOptionsInstructionDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Enable Developer Options") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(DevTuneTheme.spacing.small)) {
                Text("Android requires you to enable Developer Options manually.")
                Text("1. Tap 'Go to Settings' below.", style = MaterialTheme.typography.bodySmall)
                Text("2. Find 'Build number' (usually in About or Software information).", style = MaterialTheme.typography.bodySmall)
                Text("3. Tap 'Build number' 7 times.", style = MaterialTheme.typography.bodySmall)
                Text("4. Return to DevTune.", style = MaterialTheme.typography.bodySmall)
            }
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Go to Settings")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
