package com.devtune.platform

import com.devtune.domain.model.AnimationSettings
import com.devtune.domain.model.CapabilityState
import com.devtune.domain.model.PlatformCapability

/**
 * Boundary for verified platform integrations.
 */
interface SystemControlProvider {
    fun getCapabilityState(capability: PlatformCapability): CapabilityState
    fun isDeveloperOptionsEnabled(): Boolean
    fun getAnimationSettings(): AnimationSettings?
    fun getDisplayDensityDpi(): Int
    fun openDeveloperOptions(): Boolean
    fun openDeviceInfoSettings(): Boolean
    fun openDisplaySettings(): Boolean
}

