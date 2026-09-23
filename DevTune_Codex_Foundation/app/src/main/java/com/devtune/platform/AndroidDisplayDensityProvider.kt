package com.devtune.platform

import android.content.Context
import android.provider.Settings
import com.devtune.domain.model.DisplayDensityInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidDisplayDensityProvider @Inject constructor(
    @ApplicationContext private val context: Context
) : DisplayDensityProvider {

    override fun getDisplayDensityInfo(): DisplayDensityInfo {
        val resources = context.resources
        val displayMetrics = resources.displayMetrics
        val configuration = resources.configuration

        val displayMetricsDensityDpi = displayMetrics.densityDpi
        val configDensityDpi = configuration.densityDpi
        
        val widthPx = displayMetrics.widthPixels
        val heightPx = displayMetrics.heightPixels

        val smallestWidthDpFromConfig = if (configuration.smallestScreenWidthDp != android.content.res.Configuration.SMALLEST_SCREEN_WIDTH_DP_UNDEFINED) {
            configuration.smallestScreenWidthDp
        } else {
            null
        }

        val forcedDensityDpi = try {
            val forcedStr = Settings.Secure.getString(context.contentResolver, "display_density_forced")
            forcedStr?.trim()?.toIntOrNull()
        } catch (e: Exception) {
            null
        }

        val currentLogicalDpi = if (configDensityDpi > 0) {
            configDensityDpi
        } else if (displayMetricsDensityDpi > 0) {
            displayMetricsDensityDpi
        } else {
            160
        }

        return DisplayDensityInfo(
            currentDensityDpi = currentLogicalDpi,
            widthPx = widthPx,
            heightPx = heightPx,
            configurationDensityDpi = configDensityDpi,
            displayMetricsDensityDpi = displayMetricsDensityDpi,
            smallestWidthDpFromConfig = smallestWidthDpFromConfig,
            forcedDensityDpi = forcedDensityDpi
        )
    }
}
