package com.devtune.data.repository

import com.devtune.domain.model.*
import com.devtune.domain.repository.ShizukuRepository
import com.devtune.domain.repository.UserPreferencesRepository
import com.devtune.platform.SystemControlProvider
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class PlatformRepositoryTest {

    private val provider: SystemControlProvider = mock()
    private val prefsRepository: UserPreferencesRepository = mock()
    private val shizukuRepository: ShizukuRepository = mock()
    private val repository = PlatformRepositoryImpl(provider, prefsRepository, shizukuRepository)

    @Test
    fun `WRITE_ANIMATION_SETTINGS is AVAILABLE when current backend and version match last verified success`() = runTest {
        whenever(prefsRepository.userPreferences).thenReturn(flowOf(
            UserPreferences(
                themeMode = ThemeMode.SYSTEM,
                useDynamicColor = true,
                isOnboardingCompleted = true,
                lastVerifiedBackend = "ADB",
                lastVerifiedAndroidVersion = android.os.Build.VERSION.SDK_INT,
                lastVerifiedAt = System.currentTimeMillis(),
                lastVerifiedSuccess = true
            )
        ))
        whenever(shizukuRepository.state).thenReturn(flowOf(
            ShizukuState.Authorized(ShizukuBackend.ADB)
        ))
        
        val state = repository.getCapabilityState(PlatformCapability.WRITE_ANIMATION_SETTINGS)
        assertEquals(CapabilityState.AVAILABLE, state)
    }

    @Test
    fun `WRITE_ANIMATION_SETTINGS returns RESTRICTED when backend changes after verification`() = runTest {
        whenever(prefsRepository.userPreferences).thenReturn(flowOf(
            UserPreferences(
                themeMode = ThemeMode.SYSTEM,
                useDynamicColor = true,
                isOnboardingCompleted = true,
                lastVerifiedBackend = "ROOT",
                lastVerifiedAndroidVersion = android.os.Build.VERSION.SDK_INT,
                lastVerifiedAt = System.currentTimeMillis(),
                lastVerifiedSuccess = true
            )
        ))
        whenever(shizukuRepository.state).thenReturn(flowOf(
            ShizukuState.Authorized(ShizukuBackend.ADB) // Current backend ADB != Verified ROOT
        ))
        whenever(provider.getCapabilityState(PlatformCapability.WRITE_ANIMATION_SETTINGS))
            .thenReturn(CapabilityState.RESTRICTED)
            
        val state = repository.getCapabilityState(PlatformCapability.WRITE_ANIMATION_SETTINGS)
        assertEquals(CapabilityState.RESTRICTED, state)
    }
}
