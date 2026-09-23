package com.devtune.domain.model

import kotlin.math.min

/**
 * Domain representation of current logical display density and resolution metrics.
 */
data class DisplayDensityInfo(
    val currentDensityDpi: Int,
    val widthPx: Int,
    val heightPx: Int,
    val configurationDensityDpi: Int,
    val displayMetricsDensityDpi: Int,
    val smallestWidthDpFromConfig: Int? = null,
    val forcedDensityDpi: Int? = null
) {
    /**
     * Logical density scale factor relative to standard 160 dpi baseline (1.0x).
     */
    val densityScale: Float
        get() = if (currentDensityDpi > 0) currentDensityDpi / 160f else 1.0f

    /**
     * Calculated smallest width in dp cross-checked from screen pixel dimensions.
     */
    val calculatedSmallestWidthDp: Int
        get() {
            if (currentDensityDpi <= 0) return 0
            val minDimensionPx = min(widthPx, heightPx)
            return (minDimensionPx * 160) / currentDensityDpi
        }

    /**
     * Primary smallest width in dp, preferring the authoritative system configuration value when available.
     */
    val smallestWidthDp: Int
        get() = smallestWidthDpFromConfig ?: calculatedSmallestWidthDp

    /**
     * Formatted resolution string (e.g. "1080 × 2400").
     */
    val resolutionString: String
        get() = "$widthPx × $heightPx"

    /**
     * Formatted density scale string (e.g. "2.63×").
     */
    val scaleString: String
        get() = "%.2f×".format(densityScale)
}
