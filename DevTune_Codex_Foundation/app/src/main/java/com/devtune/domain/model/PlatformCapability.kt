package com.devtune.domain.model

/**
 * Product-facing capabilities. Platform implementations must report support rather than assuming it.
 */
enum class PlatformCapability {
    READ_DEVELOPER_OPTIONS,
    OPEN_DEVELOPER_OPTIONS,
    READ_ANIMATION_SETTINGS,
    WRITE_ANIMATION_SETTINGS,
    READ_DISPLAY_DENSITY,
    WRITE_DISPLAY_DENSITY,
    CONTROL_DEVELOPER_OPTIONS,
    ADB_AVAILABLE,
}

enum class CapabilityState {
    AVAILABLE,
    UNAVAILABLE,
    RESTRICTED,
    UNKNOWN,
}

data class CapabilityInfo(
    val capability: PlatformCapability,
    val state: CapabilityState,
    val diagnosticMessage: String? = null
)

