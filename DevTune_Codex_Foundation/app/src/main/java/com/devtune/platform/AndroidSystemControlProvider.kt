package com.devtune.platform

import android.content.Context
import android.content.Intent
import android.provider.Settings
import com.devtune.domain.model.AnimationSettings
import com.devtune.domain.model.CapabilityState
import com.devtune.domain.model.PlatformCapability
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidSystemControlProvider @Inject constructor(
    @ApplicationContext private val context: Context
) : SystemControlProvider {

    override fun getCapabilityState(capability: PlatformCapability): CapabilityState {
        return when (capability) {
            PlatformCapability.READ_DEVELOPER_OPTIONS -> CapabilityState.AVAILABLE
            PlatformCapability.OPEN_DEVELOPER_OPTIONS -> {
                val intent = Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)
                if (intent.resolveActivity(context.packageManager) != null) {
                    CapabilityState.AVAILABLE
                } else {
                    CapabilityState.UNAVAILABLE
                }
            }
            PlatformCapability.READ_ANIMATION_SETTINGS -> CapabilityState.AVAILABLE
            PlatformCapability.WRITE_ANIMATION_SETTINGS -> CapabilityState.RESTRICTED
            PlatformCapability.READ_DISPLAY_DENSITY -> CapabilityState.AVAILABLE
            PlatformCapability.WRITE_DISPLAY_DENSITY -> CapabilityState.RESTRICTED
            PlatformCapability.CONTROL_DEVELOPER_OPTIONS -> CapabilityState.RESTRICTED
            PlatformCapability.ADB_AVAILABLE -> CapabilityState.UNKNOWN
        }
    }

    override fun isDeveloperOptionsEnabled(): Boolean {
        return Settings.Global.getInt(
            context.contentResolver,
            Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0
        ) != 0
    }

    override fun getAnimationSettings(): AnimationSettings? {
        return try {
            val window = Settings.Global.getFloat(context.contentResolver, Settings.Global.WINDOW_ANIMATION_SCALE)
            val transition = Settings.Global.getFloat(context.contentResolver, Settings.Global.TRANSITION_ANIMATION_SCALE)
            val animator = Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE)
            AnimationSettings(window, transition, animator)
        } catch (e: Exception) {
            null
        }
    }

    override fun getDisplayDensityDpi(): Int {
        return context.resources.configuration.densityDpi
    }

    override fun openDeveloperOptions(): Boolean {
        val intent = Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        return safeStartActivity(intent)
    }

    override fun openDeviceInfoSettings(): Boolean {
        val intent = Intent(Settings.ACTION_DEVICE_INFO_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        return safeStartActivity(intent)
    }

    override fun openDisplaySettings(): Boolean {
        val intent = Intent(Settings.ACTION_DISPLAY_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        return safeStartActivity(intent)
    }

    private fun safeStartActivity(intent: Intent): Boolean {
        return try {
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }
}
