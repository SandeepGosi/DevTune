package com.devtune.platform

import com.devtune.domain.model.CapabilityState
import com.devtune.domain.model.PlatformCapability

/**
 * Boundary for future supported platform integrations. M0 intentionally has no implementation.
 */
interface SystemControlProvider {
    fun capabilityState(capability: PlatformCapability): CapabilityState
}
