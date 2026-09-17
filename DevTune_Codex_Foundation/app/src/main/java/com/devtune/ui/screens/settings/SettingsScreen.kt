package com.devtune.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import com.devtune.domain.model.ThemeMode
import com.devtune.ui.components.DevTuneTopAppBar
import com.devtune.ui.theme.DevTuneTheme

@Composable
fun SettingsRoute(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val themeMode by viewModel.themeMode.collectAsState()
    
    SettingsScreen(
        themeMode = themeMode,
        onThemeModeChange = viewModel::setThemeMode,
        onNavigateBack = onNavigateBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            DevTuneTopAppBar(
                title = "Settings",
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            SettingsSection(title = "Appearance") {
                ThemeSelectionRow(
                    currentMode = themeMode,
                    onModeChange = onThemeModeChange
                )
            }

            SettingsSection(title = "App Info") {
                ListItem(
                    headlineContent = { Text("App Version", fontWeight = FontWeight.Bold) },
                    supportingContent = { Text("0.1.0") },
                    leadingContent = { Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
                )
            }
        }
    }
}

@Composable
private fun ThemeSelectionRow(
    currentMode: ThemeMode,
    onModeChange: (ThemeMode) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    ListItem(
        headlineContent = { Text("Theme", fontWeight = FontWeight.Bold) },
        supportingContent = { 
            val text = when (currentMode) {
                ThemeMode.SYSTEM -> "System default"
                ThemeMode.LIGHT -> "Light"
                ThemeMode.DARK -> "Dark"
            }
            Text(text)
        },
        leadingContent = { Icon(Icons.Default.Palette, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
        modifier = Modifier.clickable { showDialog = true },
        colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
    )

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Select Theme") },
            text = {
                Column {
                    ThemeOption(
                        text = "System default",
                        selected = currentMode == ThemeMode.SYSTEM,
                        onClick = { onModeChange(ThemeMode.SYSTEM); showDialog = false }
                    )
                    ThemeOption(
                        text = "Light",
                        selected = currentMode == ThemeMode.LIGHT,
                        onClick = { onModeChange(ThemeMode.LIGHT); showDialog = false }
                    )
                    ThemeOption(
                        text = "Dark",
                        selected = currentMode == ThemeMode.DARK,
                        onClick = { onModeChange(ThemeMode.DARK); showDialog = false }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun ThemeOption(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                onClick = onClick,
                role = androidx.compose.ui.semantics.Role.RadioButton
            )
            .padding(vertical = DevTuneTheme.spacing.medium),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = null // handled by row
        )
        Spacer(modifier = Modifier.width(DevTuneTheme.spacing.medium))
        Text(text = text, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(DevTuneTheme.spacing.medium)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = DevTuneTheme.spacing.medium, bottom = DevTuneTheme.spacing.small)
        )
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            shape = MaterialTheme.shapes.medium
        ) {
            Column(content = content)
        }
    }
}
