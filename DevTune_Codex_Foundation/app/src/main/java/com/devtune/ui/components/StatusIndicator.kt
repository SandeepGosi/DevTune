package com.devtune.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.devtune.ui.theme.DevTuneTheme

@Composable
fun StatusIndicator(
    text: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(DevTuneTheme.spacing.small)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun AvailableIndicator(modifier: Modifier = Modifier) {
    StatusIndicator(
        text = "Available",
        color = Color(0xFF4CAF50), // Standard green
        modifier = modifier
    )
}

@Composable
fun RestrictedIndicator(modifier: Modifier = Modifier) {
    StatusIndicator(
        text = "Restricted",
        color = MaterialTheme.colorScheme.error,
        modifier = modifier
    )
}
