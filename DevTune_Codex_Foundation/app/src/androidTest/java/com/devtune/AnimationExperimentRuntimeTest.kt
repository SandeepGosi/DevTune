package com.devtune

import android.content.Context
import android.provider.Settings
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.devtune.data.repository.AnimationRepositoryImpl
import com.devtune.domain.model.AnimationExperimentResult
import com.devtune.domain.usecase.VerifyAnimationWriteUseCase
import com.devtune.platform.shizuku.ShizukuAnimationProvider
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

        // Pre-read original values directly from Settings.Global
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

        val provider = ShizukuAnimationProvider(context)
        val repository = AnimationRepositoryImpl(provider)
        val useCase = VerifyAnimationWriteUseCase(repository)

        val result = runBlocking { useCase() }

        println("=== EXPERIMENT EXECUTION ===")
        println("Result Type: ${result.javaClass.simpleName}")

        when (result) {
            is AnimationExperimentResult.Success -> {
                println("Requested WINDOW_ANIMATION_SCALE: ${result.testValue}")
                println("Read-back WINDOW_ANIMATION_SCALE: ${result.readBackValue}")
                println("Read-back Verification: MATCH")
                println("Restored WINDOW_ANIMATION_SCALE: ${result.restoredScales.windowScale}")
                println("Restored TRANSITION_ANIMATION_SCALE: ${result.restoredScales.transitionScale}")
                println("Restored ANIMATOR_DURATION_SCALE: ${result.restoredScales.animatorScale}")
                println("Restoration Verification: MATCH")
            }
            is AnimationExperimentResult.Failure -> {
                println("Stage: ${result.stage}")
                println("Message: ${result.message}")
            }
        }

        // Post-test final read
        val finalWindow = Settings.Global.getFloat(context.contentResolver, Settings.Global.WINDOW_ANIMATION_SCALE, 1f)
        val finalTransition = Settings.Global.getFloat(context.contentResolver, Settings.Global.TRANSITION_ANIMATION_SCALE, 1f)
        val finalAnimator = Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f)

        println("=== FINAL SYSTEM STATE ===")
        println("Final WINDOW_ANIMATION_SCALE: $finalWindow")
        println("Final TRANSITION_ANIMATION_SCALE: $finalTransition")
        println("Final ANIMATOR_DURATION_SCALE: $finalAnimator")
    }
}
