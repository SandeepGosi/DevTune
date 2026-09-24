package com.devtune.platform.shizuku

/**
 * Narrowly scoped bridge for executing privileged WindowManager operations via Shizuku Binder forwarding.
 */
interface PrivilegedSystemBridge {
    fun isShizukuReady(): Boolean
    fun getShizukuBackend(): String?
    fun getShizukuUid(): Int

    suspend fun setAnimationScale(which: Int, scale: Float): Result<Unit>
    suspend fun setAnimationScales(window: Float, transition: Float, animator: Float): Result<Unit>

    suspend fun setForcedDisplayDensityForUser(displayId: Int, density: Int, userId: Int): Result<Unit>
    suspend fun clearForcedDisplayDensityForUser(displayId: Int, userId: Int): Result<Unit>
}
