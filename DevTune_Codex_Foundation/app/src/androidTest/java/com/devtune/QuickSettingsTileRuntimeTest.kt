package com.devtune

import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.devtune.data.datastore.TemporarySessionDataSource
import com.devtune.data.repository.AnimationRepositoryImpl
import com.devtune.data.repository.TemporarySessionRepositoryImpl
import com.devtune.domain.usecase.QuickTileResult
import com.devtune.domain.usecase.ToggleQuickAnimationProfileUseCase
import com.devtune.platform.AndroidSystemControlProvider
import com.devtune.platform.shizuku.ShizukuPrivilegedBridge
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import rikka.shizuku.Shizuku

@RunWith(AndroidJUnit4::class)
class QuickSettingsTileRuntimeTest {

    @Test
    fun verifyQuickSettingsTileToggleLifecycle() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        val binderAlive = Shizuku.pingBinder()
        val isGranted = if (binderAlive) Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED else false

        println("=== M8 QUICK SETTINGS TILE RUNTIME TEST METADATA ===")
        println("Android Version: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
        println("Shizuku Binder Alive: $binderAlive")
        println("Shizuku Permission Granted: $isGranted")

        if (!binderAlive || !isGranted) {
            println("=== TEST SKIPPED: Shizuku unavailable or permission denied ===")
            return@runBlocking
        }

        val bridge = ShizukuPrivilegedBridge()
        val systemControlProvider = AndroidSystemControlProvider(context)
        val animationRepository = AnimationRepositoryImpl(systemControlProvider, bridge)
        val sessionDataSource = TemporarySessionDataSource(context)
        val sessionRepository = TemporarySessionRepositoryImpl(sessionDataSource)

        val toggleUseCase = ToggleQuickAnimationProfileUseCase(
            animationRepository,
            sessionRepository
        )

        val origWin = Settings.Global.getFloat(context.contentResolver, Settings.Global.WINDOW_ANIMATION_SCALE, 1f)
        val origTrans = Settings.Global.getFloat(context.contentResolver, Settings.Global.TRANSITION_ANIMATION_SCALE, 1f)
        val origAnim = Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f)

        try {
            // 1. First toggle: Standard 1.0x -> Fast 0.5x
            val firstResult = toggleUseCase()
            assertTrue("First toggle success", firstResult is QuickTileResult.Success)

            val fastWin = Settings.Global.getFloat(context.contentResolver, Settings.Global.WINDOW_ANIMATION_SCALE, 1f)
            val fastTrans = Settings.Global.getFloat(context.contentResolver, Settings.Global.TRANSITION_ANIMATION_SCALE, 1f)
            val fastAnim = Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f)

            assertEquals("Fast window scale = 0.5x", 0.5f, fastWin, 0.01f)
            assertEquals("Fast transition scale = 0.5x", 0.5f, fastTrans, 0.01f)
            assertEquals("Fast animator scale = 0.5x", 0.5f, fastAnim, 0.01f)

            // 2. Second toggle: Fast 0.5x -> Standard 1.0x
            val secondResult = toggleUseCase()
            assertTrue("Second toggle success", secondResult is QuickTileResult.Success)

            val restoredWin = Settings.Global.getFloat(context.contentResolver, Settings.Global.WINDOW_ANIMATION_SCALE, 1f)
            val restoredTrans = Settings.Global.getFloat(context.contentResolver, Settings.Global.TRANSITION_ANIMATION_SCALE, 1f)
            val restoredAnim = Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f)

            assertEquals("Restored window scale matches original", origWin, restoredWin, 0.01f)
            assertEquals("Restored transition scale matches original", origTrans, restoredTrans, 0.01f)
            assertEquals("Restored animator scale matches original", origAnim, restoredAnim, 0.01f)
        } finally {
            bridge.setAnimationScales(origWin, origTrans, origAnim)
            sessionRepository.clearSession()
        }
    }
}
