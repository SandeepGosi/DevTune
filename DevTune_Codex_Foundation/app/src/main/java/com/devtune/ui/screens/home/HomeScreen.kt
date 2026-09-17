package com.devtune.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.devtune.R
import com.devtune.domain.model.CapabilityState
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
    onDismissInstructions: () -> Unit,
    onConfirmOpenDeviceInfo: () -> Unit,
    onConfirmOpenDeveloperOptions: () -> Unit,
    onConfirmOpenDisplaySettings: () -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

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
                onDensitySettingsClick = onDensitySettingsClick
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
    onDensitySettingsClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentPadding = PaddingValues(
            bottom = DevTuneTheme.spacing.huge,
            top = DevTuneTheme.spacing.small
        ),
        verticalArrangement = Arrangement.spacedBy(DevTuneTheme.spacing.large)
    ) {
        item {
            DashboardHeader()
        }

        // System Status Overview
        item {
            uiState.systemState?.let { state ->
                SystemStatusModule(state)
            }
        }

        // Feature Modules
        item {
            Column(
                modifier = Modifier.padding(horizontal = DevTuneTheme.spacing.screenPadding),
                verticalArrangement = Arrangement.spacedBy(DevTuneTheme.spacing.medium)
            ) {
                Text(
                    text = "System Controls",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = DevTuneTheme.spacing.extraSmall)
                )

                FeatureCard(
                    title = "Developer Options",
                    supportingText = if (uiState.systemState?.isDeveloperOptionsEnabled == true) 
                        "Developer settings are available" else "Enable to unlock advanced settings",
                    icon = Icons.Default.Build,
                    onClick = onOpenDevOptions
                ) {
                    StatusIndicator(
                        text = if (uiState.systemState?.isDeveloperOptionsEnabled == true) "Enabled" else "Disabled",
                        color = if (uiState.systemState?.isDeveloperOptionsEnabled == true) 
                            MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                    )
                }

                FeatureCard(
                    title = "Display Density",
                    supportingText = "Adjust logical screen scaling",
                    icon = Icons.Default.AspectRatio,
                    onClick = onDensitySettingsClick
                ) {
                    Text(
                        text = "${uiState.systemState?.displayDensityDpi} DPI",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                FeatureCard(
                    title = "Animation Scales",
                    supportingText = "Current system animation values",
                    icon = Icons.Default.Speed
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(DevTuneTheme.spacing.large)
                    ) {
                        AnimationValue(label = "Window", value = uiState.systemState?.animationSettings?.windowScale ?: 1f)
                        AnimationValue(label = "Transition", value = uiState.systemState?.animationSettings?.transitionScale ?: 1f)
                        AnimationValue(label = "Animator", value = uiState.systemState?.animationSettings?.animatorDurationScale ?: 1f)
                    }
                    
                    Spacer(modifier = Modifier.height(DevTuneTheme.spacing.medium))
                    
                    Column {
                        Text(
                            text = "These values are currently read-only. Modification requires future ADB integration.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(DevTuneTheme.spacing.medium))
                        Button(
                            onClick = onAnimationSettingsClick,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Open Developer Options")
                        }
                    }
                }
            }
        }

        // Profiles Preview (M3.5 Extension Point)
        item {
            ProfilesPreviewModule()
        }

        // Advanced Controls (M3.5 Extension Point)
        item {
            AdvancedControlsModule()
        }
    }
}

@Composable
private fun SystemStatusModule(state: com.devtune.domain.model.SystemState) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = DevTuneTheme.spacing.screenPadding),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
        ),
        shape = MaterialTheme.shapes.large
    ) {
        Row(
            modifier = Modifier
                .padding(DevTuneTheme.spacing.medium)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "SYSTEM STATUS",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
                Text(
                    text = if (state.isDeveloperOptionsEnabled) "Ready for Tuning" else "Standard Mode",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            StatusIndicator(
                text = if (state.isDeveloperOptionsEnabled) "Active" else "Limited",
                color = if (state.isDeveloperOptionsEnabled) 
                    MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
            )
        }
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
        
        ScrollableTabRow(
            selectedTabIndex = 0,
            divider = {},
            indicator = {},
            containerColor = androidx.compose.ui.graphics.Color.Transparent,
            edgePadding = 0.dp
        ) {
            ProfileChip(name = "Default", value = "1.0x", selected = true)
            ProfileChip(name = "Fast", value = "0.5x")
            ProfileChip(name = "No Animation", value = "0.0x")
        }
        
        Text(
            text = "Profile application will be available in future updates.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            modifier = Modifier.padding(start = DevTuneTheme.spacing.extraSmall)
        )
    }
}

@Composable
private fun ProfileChip(name: String, value: String, selected: Boolean = false) {
    Surface(
        modifier = Modifier.padding(end = DevTuneTheme.spacing.small),
        shape = MaterialTheme.shapes.medium,
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = if (selected) androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Column(
            modifier = Modifier.padding(horizontal = DevTuneTheme.spacing.medium, vertical = DevTuneTheme.spacing.small),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = name, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            Text(text = value, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun AdvancedControlsModule() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = DevTuneTheme.spacing.screenPadding),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f)
        ),
        shape = MaterialTheme.shapes.large
    ) {
        Row(
            modifier = Modifier.padding(DevTuneTheme.spacing.medium),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(DevTuneTheme.spacing.medium)
        ) {
            Icon(
                Icons.Default.Bolt,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(32.dp)
            )
            Column {
                Text(
                    text = "Advanced Controls",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Shizuku integration coming soon",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
        }
    }
}

@Composable
private fun AnimationValue(label: String, value: Float) {
    Column {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = "${value}x", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
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
                Text("It is typically found in Display settings as 'Display size' or in Developer Options as 'Smallest width'.")
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

@Composable
private fun StatusRow(
    title: String,
    statusText: String,
    statusColor: androidx.compose.ui.graphics.Color,
    icon: ImageVector,
    onClick: (() -> Unit)?,
    supportingText: String? = null
) {
    SettingRow(
        title = title,
        description = supportingText ?: "",
        icon = { Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
        trailingContent = {
            StatusIndicator(text = statusText, color = statusColor)
        },
        onClick = onClick
    )
}
