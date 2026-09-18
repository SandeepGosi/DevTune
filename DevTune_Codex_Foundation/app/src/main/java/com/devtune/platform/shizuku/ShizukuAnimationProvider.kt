package com.devtune.platform.shizuku

import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.os.IBinder
import android.provider.Settings
import com.devtune.domain.model.AnimationScales
import com.devtune.shizuku.IAnimationService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import rikka.shizuku.Shizuku
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class ShizukuAnimationProvider @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var animationService: IAnimationService? = null

    private val userServiceArgs = Shizuku.UserServiceArgs(
        ComponentName(context.packageName, ShizukuAnimationService::class.java.name)
    ).daemon(false).processNameSuffix("animation_service")

    suspend fun getService(): IAnimationService? {
        if (animationService != null) return animationService
        if (!Shizuku.pingBinder()) return null
        
        return suspendCancellableCoroutine { continuation ->
            val connection = object : ServiceConnection {
                override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                    val instance = IAnimationService.Stub.asInterface(service)
                    animationService = instance
                    if (continuation.isActive) continuation.resume(instance)
                }

                override fun onServiceDisconnected(name: ComponentName?) {
                    animationService = null
                }
            }
            
            try {
                Shizuku.bindUserService(userServiceArgs, connection)
            } catch (e: Exception) {
                if (continuation.isActive) continuation.resume(null)
            }
        }
    }

    fun readScales(): AnimationScales {
        val window = Settings.Global.getFloat(context.contentResolver, Settings.Global.WINDOW_ANIMATION_SCALE, 1f)
        val transition = Settings.Global.getFloat(context.contentResolver, Settings.Global.TRANSITION_ANIMATION_SCALE, 1f)
        val animator = Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f)
        return AnimationScales(window, transition, animator)
    }

    suspend fun writeScale(name: String, value: Float): Boolean {
        val service = getService() ?: return false
        return try {
            service.writeScale(name, value)
        } catch (e: Exception) {
            false
        }
    }
}
