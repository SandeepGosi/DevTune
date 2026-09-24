package com.devtune.platform.shizuku

import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class ShizukuPrivilegedBridgeTest {

    private class FakePrivilegedBridge(
        var isReady: Boolean = true,
        var shouldWriteSucceed: Boolean = true,
        var backendName: String? = "ADB"
    ) : PrivilegedSystemBridge {

        var lastAnimWhich: Int? = null
        var lastAnimScale: Float? = null
        var lastDensity: Int? = null
        var isCleared: Boolean = false

        override fun isShizukuReady(): Boolean = isReady
        override fun getShizukuBackend(): String? = backendName
        override fun getShizukuUid(): Int = if (isReady) 2000 else -1

        override suspend fun setAnimationScale(which: Int, scale: Float): Result<Unit> {
            if (!isReady || !shouldWriteSucceed) return Result.failure(IllegalStateException("Failed"))
            lastAnimWhich = which
            lastAnimScale = scale
            return Result.success(Unit)
        }

        override suspend fun setAnimationScales(window: Float, transition: Float, animator: Float): Result<Unit> {
            if (!isReady || !shouldWriteSucceed) return Result.failure(IllegalStateException("Failed"))
            lastAnimScale = window
            return Result.success(Unit)
        }

        override suspend fun setForcedDisplayDensityForUser(displayId: Int, density: Int, userId: Int): Result<Unit> {
            if (!isReady || !shouldWriteSucceed) return Result.failure(IllegalStateException("Failed"))
            lastDensity = density
            return Result.success(Unit)
        }

        override suspend fun clearForcedDisplayDensityForUser(displayId: Int, userId: Int): Result<Unit> {
            if (!isReady || !shouldWriteSucceed) return Result.failure(IllegalStateException("Failed"))
            isCleared = true
            lastDensity = null
            return Result.success(Unit)
        }
    }

    @Test
    fun `Shizuku unavailable returns failure`() = runBlocking {
        val bridge = FakePrivilegedBridge(isReady = false)
        assertFalse(bridge.isShizukuReady())
        assertEquals(-1, bridge.getShizukuUid())

        val result = bridge.setAnimationScale(0, 0.5f)
        assertTrue(result.isFailure)
    }

    @Test
    fun `Animation write success path updates scale`() = runBlocking {
        val bridge = FakePrivilegedBridge(isReady = true, shouldWriteSucceed = true)
        val result = bridge.setAnimationScale(0, 0.5f)

        assertTrue(result.isSuccess)
        assertEquals(0, bridge.lastAnimWhich)
        assertEquals(0.5f, bridge.lastAnimScale)
    }

    @Test
    fun `Animation write failure path returns error`() = runBlocking {
        val bridge = FakePrivilegedBridge(isReady = true, shouldWriteSucceed = false)
        val result = bridge.setAnimationScale(0, 0.5f)

        assertTrue(result.isFailure)
    }

    @Test
    fun `Density write success path updates density`() = runBlocking {
        val bridge = FakePrivilegedBridge(isReady = true, shouldWriteSucceed = true)
        val result = bridge.setForcedDisplayDensityForUser(0, 440, -2)

        assertTrue(result.isSuccess)
        assertEquals(440, bridge.lastDensity)
    }

    @Test
    fun `Density restoration with no original forced override calls clearForcedDisplayDensity`() = runBlocking {
        val bridge = FakePrivilegedBridge(isReady = true, shouldWriteSucceed = true)
        
        // Write test density
        bridge.setForcedDisplayDensityForUser(0, 440, -2)
        assertEquals(440, bridge.lastDensity)

        // Restore when original forced density was null
        val clearResult = bridge.clearForcedDisplayDensityForUser(0, -2)
        assertTrue(clearResult.isSuccess)
        assertTrue(bridge.isCleared)
        assertNull(bridge.lastDensity)
    }

    @Test
    fun `Density restoration with existing original forced override calls setForcedDisplayDensity`() = runBlocking {
        val bridge = FakePrivilegedBridge(isReady = true, shouldWriteSucceed = true)
        val originalForcedDpi = 480

        // Write test density
        bridge.setForcedDisplayDensityForUser(0, 440, -2)
        assertEquals(440, bridge.lastDensity)

        // Restore when original forced density was 480
        val restoreResult = bridge.setForcedDisplayDensityForUser(0, originalForcedDpi, -2)
        assertTrue(restoreResult.isSuccess)
        assertEquals(480, bridge.lastDensity)
    }
}
