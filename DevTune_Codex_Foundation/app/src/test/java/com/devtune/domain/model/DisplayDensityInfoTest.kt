package com.devtune.domain.model

import org.junit.Assert.*
import org.junit.Test

class DisplayDensityInfoTest {

    @Test
    fun `densityScale calculates correct float ratio for 420 dpi`() {
        val info = DisplayDensityInfo(
            currentDensityDpi = 420,
            widthPx = 1080,
            heightPx = 2400,
            configurationDensityDpi = 420,
            displayMetricsDensityDpi = 420
        )
        assertEquals(2.625f, info.densityScale, 0.001f)
        assertEquals("2.63×", info.scaleString)
    }

    @Test
    fun `densityScale calculates 1x for 160 dpi baseline`() {
        val info = DisplayDensityInfo(
            currentDensityDpi = 160,
            widthPx = 720,
            heightPx = 1280,
            configurationDensityDpi = 160,
            displayMetricsDensityDpi = 160
        )
        assertEquals(1.0f, info.densityScale, 0.001f)
        assertEquals("1.00×", info.scaleString)
    }

    @Test
    fun `calculatedSmallestWidthDp calculates correct value for 1080x2400 at 420 dpi`() {
        val info = DisplayDensityInfo(
            currentDensityDpi = 420,
            widthPx = 1080,
            heightPx = 2400,
            configurationDensityDpi = 420,
            displayMetricsDensityDpi = 420
        )
        assertEquals(411, info.calculatedSmallestWidthDp)
        assertEquals(411, info.smallestWidthDp)
    }

    @Test
    fun `smallestWidthDp prefers config value when provided`() {
        val info = DisplayDensityInfo(
            currentDensityDpi = 420,
            widthPx = 1080,
            heightPx = 2400,
            configurationDensityDpi = 420,
            displayMetricsDensityDpi = 420,
            smallestWidthDpFromConfig = 412
        )
        assertEquals(411, info.calculatedSmallestWidthDp)
        assertEquals(412, info.smallestWidthDp)
    }

    @Test
    fun `zero density protects against division by zero`() {
        val info = DisplayDensityInfo(
            currentDensityDpi = 0,
            widthPx = 1080,
            heightPx = 1920,
            configurationDensityDpi = 0,
            displayMetricsDensityDpi = 0
        )
        assertEquals(1.0f, info.densityScale, 0.001f)
        assertEquals(0, info.calculatedSmallestWidthDp)
        assertEquals(0, info.smallestWidthDp)
    }

    @Test
    fun `resolutionString formats dimensions cleanly`() {
        val info = DisplayDensityInfo(
            currentDensityDpi = 480,
            widthPx = 1440,
            heightPx = 3040,
            configurationDensityDpi = 480,
            displayMetricsDensityDpi = 480,
            forcedDensityDpi = 480
        )
        assertEquals("1440 × 3040", info.resolutionString)
        assertEquals(480, info.forcedDensityDpi)
    }
}
