package com.devtune.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.devtune.ui.theme.DevTuneTheme

@Composable
fun SettingRow(
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    icon: (@Composable () -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true
) {
    val clickableModifier = if (onClick != null) {
        Modifier.clickable(
            enabled = enabled,
            role = Role.Button,
            onClick = onClick
        )
    } else {
        Modifier
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .then(clickableModifier),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = DevTuneTheme.spacing.screenPadding, vertical = DevTuneTheme.spacing.medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Box(modifier = Modifier.padding(end = DevTuneTheme.spacing.medium)) {
                    icon()
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = DevTuneTheme.spacing.medium)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                )
                if (description != null) {
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                    )
                }
            }

            if (trailingContent != null) {
                trailingContent()
            }
        }
    }
}

@Composable
fun SwitchSettingRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    description: String? = null,
    icon: (@Composable () -> Unit)? = null,
    enabled: Boolean = true
) {
    SettingRow(
        title = title,
        description = description,
        icon = icon,
        modifier = modifier,
        enabled = enabled,
        trailingContent = {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                enabled = enabled
            )
        },
        onClick = { onCheckedChange(!checked) }
    )
}
