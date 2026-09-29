package com.devtune

import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.devtune.data.datastore.TemporarySessionDataSource
import com.devtune.data.repository.AnimationRepositoryImpl
import com.devtune.data.repository.TemporarySessionRepositoryImpl
import com.devtune.domain.model.AnimationScales
import com.devtune.domain.model.TemporaryDuration
import com.devtune.domain.usecase.RestoreTemporarySessionUseCase
import com.devtune.domain.usecase.SetAnimationScalesUseCase
import com.devtune.domain.usecase.StartTemporarySessionUseCase
import com.devtune.platform.AndroidSystemControlProvider
import com.devtune.platform.SystemTimeProvider
import com.devtune.platform.shizuku.ShizukuPrivilegedBridge
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import rikka.shizuku.Shizuku

@RunWith(AndroidJUnit4::class)
class TemporaryAnimationSessionRuntimeTest {

    @Test
    fun verifyTemporaryAnimationSessionLifecycle() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        val binderAlive = Shizuku.pingBinder()
        val isGranted = if (binderAlive) Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED else false

        println("=== M7 TEMPORARY SESSION RUNTIME TEST METADATA ===")
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
        val setAnimationScalesUseCase = SetAnimationScalesUseCase(animationRepository)
        val timeProvider = SystemTimeProvider()

        val startSessionUseCase = StartTemporarySessionUseCase(
            animationRepository,
            sessionRepository,
            setAnimationScalesUseCase,
            timeProvider
        )

        val restoreSessionUseCase = RestoreTemporarySessionUseCase(
            sessionRepository,
            setAnimationScalesUseCase
        )

        val origResult = animationRepository.getAnimationScalesOnce()
        assertTrue("Pre-read initial baseline scales", origResult.isSuccess)
        val origScales = origResult.getOrThrow()

        try {
            val startResult = startSessionUseCase(AnimationScales.FAST, TemporaryDuration.FIVE_MINUTES)
            assertTrue("Start temporary session success", startResult.isSuccess)

            val readWin = Settings.Global.getFloat(context.contentResolver, Settings.Global.WINDOW_ANIMATION_SCALE, 1f)
            val readTrans = Settings.Global.getFloat(context.contentResolver, Settings.Global.TRANSITION_ANIMATION_SCALE, 1f)
            val readAnim = Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f)

            assertEquals("Window scale set to 0.5x", 0.5f, readWin, 0.01f)
            assertEquals("Transition scale set to 0.5x", 0.5f, readTrans, 0.01f)
            assertEquals("Animator scale set to 0.5x", 0.5f, readAnim, 0.01f)

            val restoreResult = restoreSessionUseCase()
            assertTrue("Restore temporary session success", restoreResult.isSuccess)

            val finalWin = Settings.Global.getFloat(context.contentResolver, Settings.Global.WINDOW_ANIMATION_SCALE, 1f)
            val finalTrans = Settings.Global.getFloat(context.contentResolver, Settings.Global.TRANSITION_ANIMATION_SCALE, 1f)
            val finalAnim = Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f)

            assertEquals("Restored window scale matches original", origScales.windowScale, finalWin, 0.01f)
            assertEquals("Restored transition scale matches original", origScales.transitionScale, finalTrans, 0.01f)
            assertEquals("Restored animator scale matches original", origScales.animatorScale, finalAnim, 0.01f)
        } finally {
            animationRepository.applyAnimationScales(origScales)
            sessionRepository.clearSession()
        }
    }
}
