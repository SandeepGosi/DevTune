package com.devtune.platform.shizuku

import android.content.Context
import android.provider.Settings
import com.devtune.shizuku.IAnimationService
import kotlin.system.exitProcess

class ShizukuAnimationService(private val context: Context) : IAnimationService.Stub() {

    override fun writeScale(name: String, value: Float): Boolean {
        android.util.Log.i("ShizukuAnimationService", "writeScale called for $name = $value, context class=${context.javaClass.name}, pkg=${context.packageName}")
        return try {
            val success = Settings.Global.putFloat(context.contentResolver, name, value)
            android.util.Log.i("ShizukuAnimationService", "putFloat returned $success")
            success
        } catch (t: Throwable) {
            android.util.Log.e("ShizukuAnimationService", "Throwable in writeScale: ${t.javaClass.name} - ${t.message}", t)
            false
        }
    }

    override fun destroy() {
        exitProcess(0)
    }
}
