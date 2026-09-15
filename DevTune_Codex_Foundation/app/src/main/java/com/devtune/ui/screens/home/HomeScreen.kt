package com.devtune.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.devtune.R
import com.devtune.ui.components.*
import com.devtune.ui.theme.DevTuneTheme

@Composable
fun HomeRoute(
    onNavigateToSettings: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    HomeScreen(onNavigateToSettings = onNavigateToSettings)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onNavigateToSettings: () -> Unit) {
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
        HomeContent(paddingValues)
    }
}

@Composable
private fun HomeContent(paddingValues: PaddingValues) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentPadding = PaddingValues(bottom = DevTuneTheme.spacing.huge)
    ) {
        item {
            DevTuneSection(title = "System Status") {
                SettingRow(
                    title = "Developer Options",
                    description = "Global system developer mode status",
                    icon = { Icon(Icons.Default.Info, contentDescription = null) },
                    trailingContent = { AvailableIndicator() }
                )
                ValueRow(
                    label = "Display Density",
                    value = "420 DPI",
                    supportingText = "Default density for this hardware"
                )
            }
        }

        item {
            DevTuneSection(title = "Foundation Demo") {
                SwitchSettingRow(
                    title = "Demonstration Toggle",
                    description = "This represents a future system control",
                    checked = true,
                    onCheckedChange = { /* Placeholder */ }
                )
                SettingRow(
                    title = "Secondary Action",
                    description = "A standard clickable setting row",
                    onClick = { /* Placeholder */ }
                )
            }
        }

        item {
            DevTuneSection(title = "Technical Values") {
                ValueRow(label = "Window Scale", value = "1.0x")
                ValueRow(label = "Transition Scale", value = "0.5x")
                ValueRow(label = "Animator Scale", value = "0.0x")
            }
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(DevTuneTheme.spacing.screenPadding)
            ) {
                Button(
                    onClick = { /* Placeholder */ },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Primary Action")
                }
                Spacer(modifier = Modifier.height(DevTuneTheme.spacing.small))
                OutlinedButton(
                    onClick = { /* Placeholder */ },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Secondary Action")
                }
            }
        }
    }
}
