package com.devtune.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.devtune.ui.theme.DevTuneTheme
import com.devtune.ui.theme.technicalValue

@Composable
fun ValueRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    supportingText: String? = null
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = DevTuneTheme.spacing.screenPadding, vertical = DevTuneTheme.spacing.small)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.technicalValue,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            if (supportingText != null) {
                Text(
                    text = supportingText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = DevTuneTheme.spacing.extraSmall)
                )
            }
        }
    }
}
