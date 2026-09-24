package com.devtune

import android.provider.Settings
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.devtune.data.repository.AnimationRepositoryImpl
import com.devtune.domain.usecase.VerifyAnimationWriteUseCase
import com.devtune.platform.AndroidSystemControlProvider
import com.devtune.platform.shizuku.ShizukuPrivilegedBridge
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import rikka.shizuku.Shizuku

@RunWith(AndroidJUnit4::class)
class AnimationExperimentRuntimeTest {

    @Test
    fun executeAnimationExperimentRuntime() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        val isInstalled = try {
            context.packageManager.getPackageInfo("moe.shizuku.privileged.api", 0)
            true
        } catch (e: Exception) {
            try {
                context.packageManager.getPackageInfo("rikka.app.shizuku", 0)
                true
            } catch (e2: Exception) {
                false
            }
        }

        val binderAlive = Shizuku.pingBinder()
        var isGranted = if (binderAlive) Shizuku.checkSelfPermission() == android.content.pm.PackageManager.PERMISSION_GRANTED else false

        if (binderAlive && !isGranted) {
            println("Requesting Shizuku permission...")
            try {
                Shizuku.requestPermission(1001)
                Thread.sleep(3000)
            } catch (e: Exception) {
                println("Request permission exception: ${e.message}")
            }
            isGranted = Shizuku.checkSelfPermission() == android.content.pm.PackageManager.PERMISSION_GRANTED
        }

        val uid = if (binderAlive) Shizuku.getUid() else -1

        println("=== SHIZUKU RUNTIME METADATA ===")
        println("Android Version: ${android.os.Build.VERSION.RELEASE} (API ${android.os.Build.VERSION.SDK_INT})")
        println("Device: ${android.os.Build.MODEL}")
        println("Shizuku Installed: $isInstalled")
        println("Shizuku Binder Alive: $binderAlive")
        println("Shizuku UID: $uid")
        println("Shizuku Permission Granted: $isGranted")

        val origWindow = Settings.Global.getFloat(context.contentResolver, Settings.Global.WINDOW_ANIMATION_SCALE, 1f)
        val origTransition = Settings.Global.getFloat(context.contentResolver, Settings.Global.TRANSITION_ANIMATION_SCALE, 1f)
        val origAnimator = Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f)

        println("=== PRE-READ ORIGINAL VALUES ===")
        println("Original WINDOW_ANIMATION_SCALE: $origWindow")
        println("Original TRANSITION_ANIMATION_SCALE: $origTransition")
        println("Original ANIMATOR_DURATION_SCALE: $origAnimator")

        if (!binderAlive || !isGranted) {
            println("=== RUNTIME EXPERIMENT RESULT ===")
            println("Status: PENDING / RESTRICTED (Shizuku not authorized in runtime environment)")
            return
        }

        val provider = AndroidSystemControlProvider(context)
        val bridge = ShizukuPrivilegedBridge()
        val repository = AnimationRepositoryImpl(provider, bridge)
        val useCase = VerifyAnimationWriteUseCase(repository)

        val result = runBlocking { useCase() }

        println("=== EXPERIMENT EXECUTION ===")
        println("Result: ${if (result.isSuccess) "PASS" else "FAIL"}")

        val finalWindow = Settings.Global.getFloat(context.contentResolver, Settings.Global.WINDOW_ANIMATION_SCALE, 1f)
        val finalTransition = Settings.Global.getFloat(context.contentResolver, Settings.Global.TRANSITION_ANIMATION_SCALE, 1f)
        val finalAnimator = Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f)

        println("=== FINAL SYSTEM STATE ===")
        println("Final WINDOW_ANIMATION_SCALE: $finalWindow")
        println("Final TRANSITION_ANIMATION_SCALE: $finalTransition")
        println("Final ANIMATOR_DURATION_SCALE: $finalAnimator")
    }
}
