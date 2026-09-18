package com.devtune.platform.shizuku

import android.content.Context
import android.provider.Settings
import com.devtune.shizuku.IAnimationService
import kotlin.system.exitProcess

class ShizukuAnimationService(context: Context) : IAnimationService.Stub() {

    private val contentResolver = context.contentResolver

    override fun writeScale(name: String, value: Float): Boolean {
        return try {
            Settings.Global.putFloat(contentResolver, name, value)
        } catch (e: Exception) {
            false
        }
    }

    override fun destroy() {
        exitProcess(0)
    }
}
