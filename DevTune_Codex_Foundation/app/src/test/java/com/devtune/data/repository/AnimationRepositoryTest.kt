package com.devtune.data.repository

import com.devtune.domain.model.AnimationError
import com.devtune.domain.model.AnimationScales
import com.devtune.domain.model.AnimationSettings
import com.devtune.platform.SystemControlProvider
import com.devtune.platform.shizuku.PrivilegedSystemBridge
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import org.mockito.kotlin.*

class AnimationRepositoryTest {

    private val systemControlProvider: SystemControlProvider = mock()
    private val privilegedBridge: PrivilegedSystemBridge = mock()
    private val repository = AnimationRepositoryImpl(systemControlProvider, privilegedBridge)

    @Test
    fun `applyAnimationScales fails when Shizuku unavailable`() = runTest {
        whenever(privilegedBridge.isShizukuReady()).thenReturn(false)

        val result = repository.applyAnimationScales(AnimationScales.DEFAULT)

        assertTrue(result.isFailure)
        assertEquals(AnimationError.ShizukuUnavailable, result.exceptionOrNull())
    }

    @Test
    fun `applyAnimationScales succeeds and verifies read back`() = runTest {
        whenever(privilegedBridge.isShizukuReady()).thenReturn(true)
        whenever(systemControlProvider.getAnimationSettings()).thenReturn(
            AnimationSettings(1.0f, 1.0f, 1.0f), // Pre-read
            AnimationSettings(0.5f, 0.5f, 0.5f)  // Read-back
        )
        whenever(privilegedBridge.setAnimationScale(any(), any())).thenReturn(Result.success(Unit))

        val result = repository.applyAnimationScales(AnimationScales.FAST)

        assertTrue(result.isSuccess)
        verify(privilegedBridge).setAnimationScale(0, 0.5f)
        verify(privilegedBridge).setAnimationScale(1, 0.5f)
        verify(privilegedBridge).setAnimationScale(2, 0.5f)
    }

    @Test
    fun `applyAnimationScales rolls back on partial write failure`() = runTest {
        whenever(privilegedBridge.isShizukuReady()).thenReturn(true)
        whenever(systemControlProvider.getAnimationSettings()).thenReturn(AnimationSettings(1.0f, 1.0f, 1.0f))
        whenever(privilegedBridge.setAnimationScale(eq(0), any())).thenReturn(Result.success(Unit))
        whenever(privilegedBridge.setAnimationScale(eq(1), any())).thenReturn(Result.failure(IllegalStateException("IPC Error")))
        whenever(privilegedBridge.setAnimationScales(any(), any(), any())).thenReturn(Result.success(Unit))

        val result = repository.applyAnimationScales(AnimationScales.FAST)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is AnimationError.WriteFailed)
        // Verify rollback was executed
        verify(privilegedBridge).setAnimationScales(1.0f, 1.0f, 1.0f)
    }
}
