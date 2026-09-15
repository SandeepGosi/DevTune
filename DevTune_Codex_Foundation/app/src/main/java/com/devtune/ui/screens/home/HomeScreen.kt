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
        onOpenDevOptions = viewModel::openDeveloperOptions
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onNavigateToSettings: () -> Unit,
    onOpenDevOptions: () -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

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
                onOpenDevOptions = onOpenDevOptions
            )
        }
    }
}

@Composable
private fun HomeContent(
    uiState: HomeUiState,
    paddingValues: PaddingValues,
    onOpenDevOptions: () -> Unit
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
                        onClick = if (uiState.canOpenDevOptions == CapabilityState.AVAILABLE) onOpenDevOptions else null,
                        supportingText = if (uiState.canOpenDevOptions == CapabilityState.AVAILABLE) 
                            "Tap to open system settings" else "Shortcut unavailable on this device"
                    )
                    
                    ValueRow(
                        label = "Logical Display Density",
                        value = "${state.displayDensityDpi} DPI",
                        supportingText = "Current system configuration"
                    )
                }
            }
        }

        item {
            uiState.systemState?.animationSettings?.let { anim ->
                DevTuneSection(title = "Animation Scales") {
                    ValueRow(label = "Window Animation", value = "${anim.windowScale}x")
                    ValueRow(label = "Transition Animation", value = "${anim.transitionScale}x")
                    ValueRow(label = "Animator Duration", value = "${anim.animatorDurationScale}x")
                    
                    Box(modifier = Modifier.padding(DevTuneTheme.spacing.medium)) {
                        Text(
                            text = "These values are currently read-only. Modification requires future ADB integration.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
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
