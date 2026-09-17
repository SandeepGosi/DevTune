package com.devtune.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
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
                title = stringResource(R.string.app_name),
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
            top = DevTuneTheme.spacing.medium
        ),
        verticalArrangement = Arrangement.spacedBy(DevTuneTheme.spacing.medium)
    ) {
        item {
            BrandingHeader()
        }

        item {
            uiState.systemState?.let { state ->
                DevTuneSection(title = "System Status") {
                    StatusRow(
                        title = "Developer Options",
                        statusText = if (state.isDeveloperOptionsEnabled) "Enabled" else "Disabled",
                        statusColor = if (state.isDeveloperOptionsEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                        icon = Icons.Default.Info,
                        onClick = onOpenDevOptions,
                        supportingText = if (state.isDeveloperOptionsEnabled) 
                            "Tap to open system settings" else "Tap to learn how to enable"
                    )
                    
                    ValueRow(
                        label = "Logical Display Density",
                        value = "${state.displayDensityDpi} DPI",
                        supportingText = "Current system configuration",
                        onClick = onDensitySettingsClick
                    )
                }
            }
        }

        item {
            uiState.systemState?.animationSettings?.let { anim ->
                DevTuneSection(title = "Animation Scales") {
                    ValueRow(
                        label = "Window Animation",
                        value = "${anim.windowScale}x"
                    )
                    ValueRow(
                        label = "Transition Animation",
                        value = "${anim.transitionScale}x"
                    )
                    ValueRow(
                        label = "Animator Duration",
                        value = "${anim.animatorDurationScale}x"
                    )
                    
                    Column(modifier = Modifier.padding(DevTuneTheme.spacing.medium)) {
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

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = DevTuneTheme.spacing.screenPadding)
            ) {
                Text(
                    text = "DevTune helps you understand and manage Android developer and system preferences.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun BrandingHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = DevTuneTheme.spacing.screenPadding),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier.size(64.dp),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.primaryContainer,
            tonalElevation = 4.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.Tune,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(DevTuneTheme.spacing.medium))
        Text(
            text = "System Companion",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
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
