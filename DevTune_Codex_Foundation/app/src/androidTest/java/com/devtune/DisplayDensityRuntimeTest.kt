package com.devtune

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.devtune.platform.AndroidDisplayDensityProvider
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DisplayDensityRuntimeTest {

    @Test
    fun executeDisplayDensityRuntimeRead() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val provider = AndroidDisplayDensityProvider(context)
        val densityInfo = provider.getDisplayDensityInfo()

        println("=== DISPLAY DENSITY RUNTIME METRICS ===")
        println("Android Version: ${android.os.Build.VERSION.RELEASE} (API ${android.os.Build.VERSION.SDK_INT})")
        println("Device: ${android.os.Build.MODEL}")
        println("Current Logical Density: ${densityInfo.currentDensityDpi} DPI")
        println("Density Scale: ${densityInfo.scaleString} (${densityInfo.densityScale}x)")
        println("Screen Resolution: ${densityInfo.resolutionString}")
        println("Smallest Width: ${densityInfo.smallestWidthDp} dp")
        println("Configuration Density: ${densityInfo.configurationDensityDpi} DPI")
        println("DisplayMetrics Density: ${densityInfo.displayMetricsDensityDpi} DPI")
        println("Forced Density Override: ${densityInfo.forcedDensityDpi?.let { "$it DPI" } ?: "None (Default)"}")

        assertNotNull(densityInfo)
        assertTrue("Density DPI must be > 0", densityInfo.currentDensityDpi > 0)
        assertTrue("Screen width must be > 0", densityInfo.widthPx > 0)
        assertTrue("Screen height must be > 0", densityInfo.heightPx > 0)
        assertTrue("Smallest width must be > 0", densityInfo.smallestWidthDp > 0)
    }
}
