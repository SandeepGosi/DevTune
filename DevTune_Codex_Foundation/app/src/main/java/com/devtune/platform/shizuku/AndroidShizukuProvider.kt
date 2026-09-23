package com.devtune.platform.shizuku

import android.content.Context
import android.content.pm.PackageManager
import androidx.annotation.VisibleForTesting
import com.devtune.domain.model.ShizukuBackend
import com.devtune.domain.model.ShizukuState
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import rikka.shizuku.Shizuku
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class AndroidShizukuProvider @Inject constructor(
    @ApplicationContext private val context: Context
) : ShizukuProvider {

    override val state: Flow<ShizukuState> = callbackFlow {
        val binderReceivedListener = Shizuku.OnBinderReceivedListener {
            trySend(computeState())
        }
        val binderDeadListener = Shizuku.OnBinderDeadListener {
            trySend(ShizukuState.ConnectionLost)
        }
        val permissionResultListener = Shizuku.OnRequestPermissionResultListener { _, _ ->
            trySend(computeState())
        }

        Shizuku.addBinderReceivedListener(binderReceivedListener)
        Shizuku.addBinderDeadListener(binderDeadListener)
        Shizuku.addRequestPermissionResultListener(permissionResultListener)

        // Initial state
        trySend(computeState())

        awaitClose {
            Shizuku.removeBinderReceivedListener(binderReceivedListener)
            Shizuku.removeBinderDeadListener(binderDeadListener)
            Shizuku.removeRequestPermissionResultListener(permissionResultListener)
        }
    }

    override fun requestPermission() {
        if (pingBinderForTest()) {
            Shizuku.requestPermission(0)
        }
    }

    private fun computeState(): ShizukuState {
        if (!isShizukuInstalledForTest()) {
            return ShizukuState.NotInstalled
        }

        if (!pingBinderForTest()) {
            return ShizukuState.Stopped
        }

        return if (checkSelfPermissionForTest() == PackageManager.PERMISSION_GRANTED) {
            ShizukuState.Authorized(getBackend())
        } else {
            ShizukuState.PermissionRequired
        }
    }

    private fun getBackend(): ShizukuBackend {
        return try {
            val uid = getUidForTest()
            when (uid) {
                0 -> ShizukuBackend.ROOT
                2000 -> ShizukuBackend.ADB
                else -> ShizukuBackend.UNKNOWN
            }
        } catch (e: Exception) {
            ShizukuBackend.UNKNOWN
        }
    }

    @VisibleForTesting
    internal fun computeStateInternal() = computeState()

    @VisibleForTesting
    open fun isShizukuInstalledForTest(): Boolean {
        return try {
            context.packageManager.getPackageInfo("moe.shizuku.privileged.api", 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            try {
                context.packageManager.getPackageInfo("rikka.app.shizuku", 0)
                true
            } catch (e2: PackageManager.NameNotFoundException) {
                false
            }
        }
    }

    @VisibleForTesting
    open fun pingBinderForTest(): Boolean = Shizuku.pingBinder()

    @VisibleForTesting
    open fun checkSelfPermissionForTest(): Int = Shizuku.checkSelfPermission()

    @VisibleForTesting
    open fun getUidForTest(): Int = Shizuku.getUid()
}
