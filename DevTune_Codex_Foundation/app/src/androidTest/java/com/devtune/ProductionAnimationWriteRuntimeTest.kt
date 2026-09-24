package com.devtune

import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.devtune.platform.shizuku.ShizukuPrivilegedBridge
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import rikka.shizuku.Shizuku
import kotlin.math.abs

@RunWith(AndroidJUnit4::class)
class ProductionAnimationWriteRuntimeTest {

    @Test
    fun verifyProductionAnimationWriteAndRestoration() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        val binderAlive = Shizuku.pingBinder()
        val isGranted = if (binderAlive) Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED else false
        val uid = if (binderAlive) Shizuku.getUid() else -1

        println("=== M5A PRODUCTION ANIMATION RUNTIME TEST METADATA ===")
        println("Android Version: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
        println("Device: ${Build.MODEL}")
        println("Shizuku Binder Alive: $binderAlive")
        println("Shizuku UID: $uid")
        println("Shizuku Permission Granted: $isGranted")

        if (!binderAlive || !isGranted) {
            println("=== TEST SKIPPED: Shizuku unavailable or permission denied ===")
            return@runBlocking
        }

        val bridge = ShizukuPrivilegedBridge()

        // 1. Pre-Read original scales
        val origWin = Settings.Global.getFloat(context.contentResolver, Settings.Global.WINDOW_ANIMATION_SCALE, 1f)
        val origTrans = Settings.Global.getFloat(context.contentResolver, Settings.Global.TRANSITION_ANIMATION_SCALE, 1f)
        val origAnim = Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f)

        println("Pre-Read WINDOW: $origWin, TRANSITION: $origTrans, ANIMATOR: $origAnim")

        val targetWin = 0.5f
        var writePass = false
        var restorePass = false

        try {
            println("Attempting privileged write setAnimationScale(0, $targetWin)...")
            val writeResult = bridge.setAnimationScale(0, targetWin)

            if (writeResult.isSuccess) {
                val readWin = Settings.Global.getFloat(context.contentResolver, Settings.Global.WINDOW_ANIMATION_SCALE, 1f)
                println("Read-back WINDOW_ANIMATION_SCALE: $readWin")

                if (abs(readWin - targetWin) < 0.01f) {
                    writePass = true
                    println("Animation Write Verification: PASS")
                }
            }
        } finally {
            println("Restoring original scales ($origWin, $origTrans, $origAnim)...")
            bridge.setAnimationScales(origWin, origTrans, origAnim)

            val finalWin = Settings.Global.getFloat(context.contentResolver, Settings.Global.WINDOW_ANIMATION_SCALE, 1f)
            val finalTrans = Settings.Global.getFloat(context.contentResolver, Settings.Global.TRANSITION_ANIMATION_SCALE, 1f)
            val finalAnim = Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f)

            println("Final Restored WINDOW: $finalWin, TRANSITION: $finalTrans, ANIMATOR: $finalAnim")

            if (abs(finalWin - origWin) < 0.01f &&
                abs(finalTrans - origTrans) < 0.01f &&
                abs(finalAnim - origAnim) < 0.01f
            ) {
                restorePass = true
                println("Animation Restoration Verification: PASS")
            }
        }

        assertTrue("Animation Write must succeed", writePass)
        assertTrue("Animation Restoration must succeed", restorePass)
    }
}
