package com.devtune.platform.shizuku

import android.content.pm.PackageManager
import android.view.IWindowManager
import rikka.shizuku.Shizuku
import rikka.shizuku.ShizukuBinderWrapper
import rikka.shizuku.SystemServiceHelper
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShizukuPrivilegedBridge @Inject constructor() : PrivilegedSystemBridge {

    override fun isShizukuReady(): Boolean {
        return Shizuku.pingBinder() && Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
    }

    override fun getShizukuBackend(): String? {
        return if (Shizuku.pingBinder()) {
            "ADB/ROOT"
        } else {
            null
        }
    }

    override fun getShizukuUid(): Int {
        return if (Shizuku.pingBinder()) Shizuku.getUid() else -1
    }

    private fun getWindowManagerService(): IWindowManager? {
        if (!isShizukuReady()) return null
        return try {
            val windowBinder = SystemServiceHelper.getSystemService("window") ?: return null
            val wrappedBinder = ShizukuBinderWrapper(windowBinder)
            IWindowManager.Stub.asInterface(wrappedBinder)
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun setAnimationScale(which: Int, scale: Float): Result<Unit> {
        val wm = getWindowManagerService() ?: return Result.failure(IllegalStateException("Shizuku WindowManager service unavailable"))
        return try {
            wm.setAnimationScale(which, scale)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun setAnimationScales(window: Float, transition: Float, animator: Float): Result<Unit> {
        val wm = getWindowManagerService() ?: return Result.failure(IllegalStateException("Shizuku WindowManager service unavailable"))
        return try {
            wm.setAnimationScale(0, window)
            wm.setAnimationScale(1, transition)
            wm.setAnimationScale(2, animator)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun setForcedDisplayDensityForUser(displayId: Int, density: Int, userId: Int): Result<Unit> {
        return Result.failure(UnsupportedOperationException("Display density modification APIs (setForcedDisplayDensityForUser) were removed from IWindowManager in Android 17 (API 37)."))
    }

    override suspend fun clearForcedDisplayDensityForUser(displayId: Int, userId: Int): Result<Unit> {
        return Result.failure(UnsupportedOperationException("Display density modification APIs (clearForcedDisplayDensityForUser) were removed from IWindowManager in Android 17 (API 37)."))
    }
}
