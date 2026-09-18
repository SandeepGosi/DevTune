package com.devtune.data.repository

import com.devtune.domain.model.CapabilityState
import com.devtune.domain.model.PlatformCapability
import com.devtune.domain.model.ThemeMode
import com.devtune.domain.model.UserPreferences
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
    private val repository = PlatformRepositoryImpl(provider, prefsRepository)

    @Test
    fun `WRITE_ANIMATION_SETTINGS is AVAILABLE when verified in preferences`() = runTest {
        whenever(prefsRepository.userPreferences).thenReturn(flowOf(
            UserPreferences(ThemeMode.SYSTEM, true, true, isAnimationWriteVerified = true)
        ))
        
        val state = repository.getCapabilityState(PlatformCapability.WRITE_ANIMATION_SETTINGS)
        assertEquals(CapabilityState.AVAILABLE, state)
    }

    @Test
    fun `WRITE_ANIMATION_SETTINGS returns provider state when not verified`() = runTest {
        whenever(prefsRepository.userPreferences).thenReturn(flowOf(
            UserPreferences(ThemeMode.SYSTEM, true, true, isAnimationWriteVerified = false)
        ))
        whenever(provider.getCapabilityState(PlatformCapability.WRITE_ANIMATION_SETTINGS))
            .thenReturn(CapabilityState.RESTRICTED)
            
        val state = repository.getCapabilityState(PlatformCapability.WRITE_ANIMATION_SETTINGS)
        assertEquals(CapabilityState.RESTRICTED, state)
    }
}
