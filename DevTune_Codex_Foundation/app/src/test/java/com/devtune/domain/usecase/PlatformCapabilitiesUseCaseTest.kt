package com.devtune.domain.usecase

import com.devtune.domain.model.CapabilityState
import com.devtune.domain.model.PlatformCapability
import com.devtune.domain.model.SystemState
import com.devtune.domain.repository.PlatformRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class PlatformCapabilitiesUseCaseTest {

    private val repository: PlatformRepository = mock()

    @Test
    fun `GetCapabilityStateUseCase returns state from repository`() {
        val useCase = GetCapabilityStateUseCase(repository)
        whenever(repository.getCapabilityState(PlatformCapability.READ_DEVELOPER_OPTIONS))
            .thenReturn(CapabilityState.AVAILABLE)

        val result = useCase(PlatformCapability.READ_DEVELOPER_OPTIONS)
        assertEquals(CapabilityState.AVAILABLE, result)
    }

    @Test
    fun `GetSystemStateUseCase returns flow from repository`() = runTest {
        val useCase = GetSystemStateUseCase(repository)
        val expectedState = SystemState(isDeveloperOptionsEnabled = true, displayDensityDpi = 420)
        whenever(repository.getSystemState()).thenReturn(flowOf(expectedState))

        val result = useCase().first()
        assertEquals(expectedState, result)
    }
}
