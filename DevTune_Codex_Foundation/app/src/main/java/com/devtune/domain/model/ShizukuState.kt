package com.devtune.domain.model

enum class ShizukuBackend {
    NONE,
    ADB,
    ROOT,
    UNKNOWN
}

sealed class ShizukuState {
    data object NotInstalled : ShizukuState()
    data object Stopped : ShizukuState()
    data object PermissionRequired : ShizukuState()
    data class Authorized(val backend: ShizukuBackend) : ShizukuState()
    data object ConnectionLost : ShizukuState()
    data object Unsupported : ShizukuState()
    data object Unknown : ShizukuState()
}
