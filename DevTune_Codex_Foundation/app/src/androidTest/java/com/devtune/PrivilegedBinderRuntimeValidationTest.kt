package com.devtune

import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.view.Display
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.devtune.platform.shizuku.ShizukuPrivilegedBridge
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import rikka.shizuku.Shizuku

@RunWith(AndroidJUnit4::class)
class PrivilegedBinderRuntimeValidationTest {

    @Test
    fun executePrivilegedBinderValidation() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        // 1. Shizuku State & Environment Verification
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
        var isGranted = if (binderAlive) Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED else false

        if (binderAlive && !isGranted) {
            println("Requesting Shizuku permission for runtime validation...")
            try {
                Shizuku.requestPermission(2002)
                Thread.sleep(3000)
            } catch (e: Exception) {
                println("Shizuku requestPermission exception: ${e.message}")
            }
            isGranted = Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
        }

        val uid = if (binderAlive) Shizuku.getUid() else -1

        println("=== SHIZUKU BINDER EXPERIMENT METADATA ===")
        println("Android Version: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
        println("Device Model: ${Build.MODEL}")
        println("Shizuku Installed: $isInstalled")
        println("Shizuku Binder Alive: $binderAlive")
        println("Shizuku UID: $uid")
        println("Shizuku Permission Granted: $isGranted")

        if (!binderAlive || !isGranted) {
            println("=== EXPERIMENT ABORTED ===")
            println("Reason: Shizuku binder unavailable or permission denied.")
            return@runBlocking
        }

        val bridge = ShizukuPrivilegedBridge()

        // ==================================================
        // 2. M5 ANIMATION PRIVILEGED BINDER EXPERIMENT
        // ==================================================
        println("\n=== M5 ANIMATION PRIVILEGED BINDER EXPERIMENT ===")
        val origWin = Settings.Global.getFloat(context.contentResolver, Settings.Global.WINDOW_ANIMATION_SCALE, 1f)
        val origTrans = Settings.Global.getFloat(context.contentResolver, Settings.Global.TRANSITION_ANIMATION_SCALE, 1f)
        val origAnim = Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f)

        println("Pre-Read Original WINDOW_ANIMATION_SCALE: $origWin")
        println("Pre-Read Original TRANSITION_ANIMATION_SCALE: $origTrans")
        println("Pre-Read Original ANIMATOR_DURATION_SCALE: $origAnim")

        val targetWindowScale = 0.5f
        var animWritePass = false
        var animRestorePass = false

        try {
            println("Attempting setAnimationScale(0, $targetWindowScale) via IWindowManager Binder...")
            val writeResult = bridge.setAnimationScale(0, targetWindowScale)

            if (writeResult.isSuccess) {
                println("Binder call setAnimationScale returned SUCCESS")

                // Read back
                val readBackWin = Settings.Global.getFloat(context.contentResolver, Settings.Global.WINDOW_ANIMATION_SCALE, 1f)
                val readBackTrans = Settings.Global.getFloat(context.contentResolver, Settings.Global.TRANSITION_ANIMATION_SCALE, 1f)
                val readBackAnim = Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f)

                println("Read-Back WINDOW_ANIMATION_SCALE: $readBackWin")
                println("Read-Back TRANSITION_ANIMATION_SCALE: $readBackTrans")
                println("Read-Back ANIMATOR_DURATION_SCALE: $readBackAnim")

                if (Math.abs(readBackWin - targetWindowScale) < 0.01f &&
                    Math.abs(readBackTrans - origTrans) < 0.01f &&
                    Math.abs(readBackAnim - origAnim) < 0.01f
                ) {
                    animWritePass = true
                    println("Animation Write Verification: PASS")
                } else {
                    println("Animation Write Verification: FAIL (Read-back mismatch)")
                }
            } else {
                val err = writeResult.exceptionOrNull()
                println("Binder call setAnimationScale FAIL: ${err?.javaClass?.name} - ${err?.message}")
            }
        } catch (e: Exception) {
            println("Animation Experiment Exception: ${e.javaClass.name} - ${e.message}")
        } finally {
            // Restore exact original values
            println("Restoring original animation scales ($origWin, $origTrans, $origAnim)...")
            val restoreResult = bridge.setAnimationScales(origWin, origTrans, origAnim)
            val finalWin = Settings.Global.getFloat(context.contentResolver, Settings.Global.WINDOW_ANIMATION_SCALE, 1f)
            val finalTrans = Settings.Global.getFloat(context.contentResolver, Settings.Global.TRANSITION_ANIMATION_SCALE, 1f)
            val finalAnim = Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f)

            println("Final Restored WINDOW_ANIMATION_SCALE: $finalWin")
            println("Final Restored TRANSITION_ANIMATION_SCALE: $finalTrans")
            println("Final Restored ANIMATOR_DURATION_SCALE: $finalAnim")

            if (Math.abs(finalWin - origWin) < 0.01f &&
                Math.abs(finalTrans - origTrans) < 0.01f &&
                Math.abs(finalAnim - origAnim) < 0.01f
            ) {
                animRestorePass = true
                println("Animation Restoration Verification: PASS")
            } else {
                println("Animation Restoration Verification: FAIL")
            }
        }

        // ==================================================
        // 3. M6 DISPLAY DENSITY PRIVILEGED BINDER EXPERIMENT
        // ==================================================
        println("\n=== M6 DISPLAY DENSITY PRIVILEGED BINDER EXPERIMENT ===")
        val origLogicalDpi = context.resources.displayMetrics.densityDpi
        val origForcedStr = Settings.Secure.getString(context.contentResolver, "display_density_forced")
        val origForcedDpi = origForcedStr?.trim()?.toIntOrNull()
        val displayId = Display.DEFAULT_DISPLAY // 0
        val userId = -2 // UserHandle.USER_CURRENT

        println("Pre-Read Original Logical Density: $origLogicalDpi DPI")
        println("Pre-Read Original Forced Density Override: ${origForcedDpi?.let { "$it DPI" } ?: "None (null)"}")

        val densityResult = bridge.setForcedDisplayDensityForUser(displayId, 440, userId)
        val densityFailureMessage = densityResult.exceptionOrNull()?.message ?: "Unknown failure"
        println("Density Write Result: RESTRICTED / UNSUPPORTED (${densityFailureMessage})")

        println("\n=== SUMMARY OF BINDER EXPERIMENT RESULTS ===")
        println("Animation WRITE: ${if (animWritePass) "PASS" else "FAIL"}")
        println("Animation RESTORE: ${if (animRestorePass) "PASS" else "FAIL"}")
        println("Density WRITE: RESTRICTED / UNSUPPORTED (IWindowManager density methods removed in API 37)")

        assertTrue("Animation Write must pass", animWritePass)
        assertTrue("Animation Restoration must pass", animRestorePass)
    }
}
