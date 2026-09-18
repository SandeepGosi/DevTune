package com.devtune.platform.shizuku

import android.content.Context
import android.content.pm.PackageManager
import com.devtune.domain.model.ShizukuBackend
import com.devtune.domain.model.ShizukuState
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*
import rikka.shizuku.Shizuku

/**
 * M4 REVIEW REQUIREMENT:
 * Confirm state accuracy and backend detection.
 * 
 * NOTE: Since Shizuku uses static methods, we rely on the implementation 
 * of computeState() and getBackend() in AndroidShizukuProvider.
 * 
 * This test uses a TestableAndroidShizukuProvider to verify the logic
 * by mocking the dependencies that computeState() uses.
 */
class ShizukuStateMappingTest {

    private val context: Context = mock()
    private val packageManager: PackageManager = mock()
    
    @Before
    fun setup() {
        whenever(context.packageManager).thenReturn(packageManager)
    }

    // We create a testable subclass to mock the static-calling methods
    // since Shizuku SDK uses statics extensively.
    private open class TestableAndroidShizukuProvider(context: Context) : AndroidShizukuProvider(context) {
        var mockPingBinder = false
        var mockIsInstalled = false
        var mockPermissionGranted = false
        var mockUid = -1

        override fun isShizukuInstalledForTest(): Boolean = mockIsInstalled
        override fun pingBinderForTest(): Boolean = mockPingBinder
        override fun checkSelfPermissionForTest(): Int = 
            if (mockPermissionGranted) PackageManager.PERMISSION_GRANTED else PackageManager.PERMISSION_DENIED
        override fun getUidForTest(): Int = mockUid

        fun testComputeState() = computeStateInternal()
    }

    @Test
    fun `NOT_INSTALLED is determined by package availability`() {
        val provider = object : TestableAndroidShizukuProvider(context) {
            override fun isShizukuInstalledForTest() = false
        }
        
        assertEquals(ShizukuState.NotInstalled, provider.testComputeState())
    }

    @Test
    fun `STOPPED is distinguishable from NOT_INSTALLED`() {
        val provider = object : TestableAndroidShizukuProvider(context) {
            override fun isShizukuInstalledForTest() = true
            override fun pingBinderForTest() = false
        }
        
        assertEquals(ShizukuState.Stopped, provider.testComputeState())
    }

    @Test
    fun `PERMISSION_REQUIRED means binder available but permission not granted`() {
        val provider = object : TestableAndroidShizukuProvider(context) {
            override fun isShizukuInstalledForTest() = true
            override fun pingBinderForTest() = true
            override fun checkSelfPermissionForTest() = PackageManager.PERMISSION_DENIED
        }
        
        assertEquals(ShizukuState.PermissionRequired, provider.testComputeState())
    }

    @Test
    fun `AUTHORIZED means binder available and permission granted`() {
        val provider = object : TestableAndroidShizukuProvider(context) {
            override fun isShizukuInstalledForTest() = true
            override fun pingBinderForTest() = true
            override fun checkSelfPermissionForTest() = PackageManager.PERMISSION_GRANTED
            override fun getUidForTest() = 2000 // ADB
        }
        
        val state = provider.testComputeState()
        assert(state is ShizukuState.Authorized)
        assertEquals(ShizukuBackend.ADB, (state as ShizukuState.Authorized).backend)
    }

    @Test
    fun `Backend ROOT is detected for UID 0`() {
        val provider = object : TestableAndroidShizukuProvider(context) {
            override fun isShizukuInstalledForTest() = true
            override fun pingBinderForTest() = true
            override fun checkSelfPermissionForTest() = PackageManager.PERMISSION_GRANTED
            override fun getUidForTest() = 0
        }
        
        val state = provider.testComputeState()
        assertEquals(ShizukuBackend.ROOT, (state as ShizukuState.Authorized).backend)
    }

    @Test
    fun `Backend UNKNOWN is returned for unrecognized UID`() {
        val provider = object : TestableAndroidShizukuProvider(context) {
            override fun isShizukuInstalledForTest() = true
            override fun pingBinderForTest() = true
            override fun checkSelfPermissionForTest() = PackageManager.PERMISSION_GRANTED
            override fun getUidForTest() = 9999
        }
        
        val state = provider.testComputeState()
        assertEquals(ShizukuBackend.UNKNOWN, (state as ShizukuState.Authorized).backend)
    }
}
