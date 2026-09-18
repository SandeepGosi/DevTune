package com.devtune.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.devtune.R
import com.devtune.domain.model.AnimationExperimentResult
import com.devtune.domain.model.CapabilityState
import com.devtune.domain.model.ShizukuState
import com.devtune.domain.model.SystemState
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
    onDismissExperimentResult: () -> Unit,
    onDismissInstructions: () -> Unit,
    onConfirmOpenDeviceInfo: () -> Unit,
    onConfirmOpenDeveloperOptions: () -> Unit,
    onConfirmOpenDisplaySettings: () -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    if (uiState.experimentResult != null) {
        ExperimentResultDialog(
            result = uiState.experimentResult,
            onDismiss = onDismissExperimentResult
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

    if (uiState.showDensityInstructions) {
        DensitySettingsInstructionDialog(
            onDismiss = onDismissInstructions,
            onConfirm = onConfirmOpenDisplaySettings
        )
    }

    if (uiState.showDensityDevOptionsDisabled) {
        DensityDevOptionsDisabledDialog(
            onDismiss = onDismissInstructions,
            onLearnHowToEnable = onOpenDevOptions,
            onOpenDisplaySettings = onConfirmOpenDisplaySettings
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            DevTuneTopAppBar(
                title = "", // Branding in header
                scrollBehavior = scrollBehavior,
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
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
                onRunAnimationExperiment = onRunAnimationExperiment
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
    onRunAnimationExperiment: () -> Unit
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

        // System Status Hero Card
        item {
            uiState.systemState?.let { state ->
                SystemStatusHero(state)
            }
        }

        // Primary Controls
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
                        supportingText = "Current system configuration",
                        icon = Icons.Default.AspectRatio,
                        onClick = onDensitySettingsClick
                    ) {
                        Text(
                            text = "${state.displayDensityDpi} DPI",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    FeatureCard(
                        title = "Animation Scales",
                        supportingText = when {
                            uiState.writeAnimationCapability == CapabilityState.AVAILABLE -> "Advanced animation control verified"
                            uiState.shizukuState is ShizukuState.Authorized -> "Verification required for advanced control"
                            uiState.shizukuState is ShizukuState.PermissionRequired -> "Permission required for advanced control"
                            else -> "Read-only without advanced system access"
                        },
                        icon = Icons.Default.Speed,
                        tonalElevation = 1.dp
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(DevTuneTheme.spacing.large)
                        ) {
                            AnimationValue(label = "Window", value = state.animationSettings?.windowScale ?: 1f)
                            AnimationValue(label = "Transition", value = state.animationSettings?.transitionScale ?: 1f)
                            AnimationValue(label = "Animator", value = state.animationSettings?.animatorDurationScale ?: 1f)
                        }
                        
                        Spacer(modifier = Modifier.height(DevTuneTheme.spacing.medium))
                        
                        if (uiState.shizukuState is ShizukuState.Authorized) {
                            Button(
                                onClick = onRunAnimationExperiment,
                                modifier = Modifier.fillMaxWidth(),
                                shape = MaterialTheme.shapes.medium,
                                enabled = !uiState.isExperimentRunning
                            ) {
                                if (uiState.isExperimentRunning) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(DevTuneTheme.spacing.small))
                                    Text("Verifying...")
                                } else {
                                    Text("Test Animation Control")
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
        }

        // Extension Points
        item {
            ProfilesPreviewModule()
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
private fun SystemStatusHero(state: SystemState) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = DevTuneTheme.spacing.screenPadding),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
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
                    text = "Standard Mode",
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(DevTuneTheme.spacing.small))
            
            Text(
                text = if (state.isDeveloperOptionsEnabled) "Developer settings are available" else "Standard Android configuration",
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
                HeroStat(label = "Options", value = if (state.isDeveloperOptionsEnabled) "ON" else "OFF")
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
private fun ProfilesPreviewModule() {
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
            ProfileChip(name = "Default", value = "1.0x", selected = true, modifier = Modifier.weight(1f))
            ProfileChip(name = "Fast", value = "0.5x", modifier = Modifier.weight(1f))
            ProfileChip(name = "No Anim", value = "0x", modifier = Modifier.weight(1f))
        }
        
        Text(
            text = "Animation presets",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.padding(start = DevTuneTheme.spacing.extraSmall)
        )
    }
}

@Composable
private fun ProfileChip(name: String, value: String, selected: Boolean = false, modifier: Modifier = Modifier) {
    Surface(
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
    val (statusTitle, statusDescription, statusColor) = when (state) {
        is ShizukuState.NotInstalled -> Triple("Shizuku not detected", "Unlock advanced system controls", MaterialTheme.colorScheme.outline)
        is ShizukuState.Stopped -> Triple("Shizuku not running", "Service is stopped", MaterialTheme.colorScheme.error)
        is ShizukuState.PermissionRequired -> Triple("Permission required", "Tap to grant Shizuku access", MaterialTheme.colorScheme.secondary)
        is ShizukuState.Authorized -> Triple("Advanced system access", "Shizuku connected via ${state.backend.name}", MaterialTheme.colorScheme.primary)
        is ShizukuState.ConnectionLost -> Triple("Connection lost", "Shizuku service disconnected", MaterialTheme.colorScheme.error)
        else -> Triple("Advanced Controls", "Unknown Shizuku state", MaterialTheme.colorScheme.outline)
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
        Text(text = "${value}x", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
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
                Text("Android does not provide a public direct link to these individual settings on this device.")
                Text("You can find them manually in Developer Options:")
                
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
private fun DensitySettingsInstructionDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Display Density") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(DevTuneTheme.spacing.small)) {
                Text("Android does not provide a public direct link to this specific setting on this device.")
                Text("Advanced display-density controls such as Smallest width are available through Developer Options.")
                Text("Generic display settings may also contain 'Display size'.")
            }
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Open Display Settings")
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
            Text(if (result is AnimationExperimentResult.Success) "Test Successful" else "Test Failed")
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(DevTuneTheme.spacing.small)) {
                when (result) {
                    is AnimationExperimentResult.Success -> {
                        Text("DevTune successfully modified and restored your animation settings.")
                        Text("Original: ${result.originalScales.windowScale}x")
                        Text("Test Write: ${result.testValue}x")
                        Text("Read Back: ${result.readBackValue}x")
                        Text("Restored: ${result.restoredScales.windowScale}x")
                        Text("\nAdvanced animation control is now enabled.", fontWeight = FontWeight.Bold)
                    }
                    is AnimationExperimentResult.Failure -> {
                        Text("The test failed at the ${result.stage.name} stage.")
                        Text("Error: ${result.message}")
                        if (result.originalScales != null) {
                            Text("Your original settings were: ${result.originalScales.windowScale}x")
                        }
                        if (result.currentScales != null) {
                            Text("Current system values: ${result.currentScales.windowScale}x")
                        }
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
private fun DensityDevOptionsDisabledDialog(
    onDismiss: () -> Unit,
    onLearnHowToEnable: () -> Unit,
    onOpenDisplaySettings: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Developer Options is disabled") },
        text = {
            Text("Advanced display-density controls such as Smallest width are available through Developer Options.")
        },
        confirmButton = {
            Column(verticalArrangement = Arrangement.spacedBy(DevTuneTheme.spacing.small)) {
                Button(
                    onClick = onLearnHowToEnable,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Learn how to enable")
                }
                OutlinedButton(
                    onClick = onOpenDisplaySettings,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Open Display Settings")
                }
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
