package com.devtune.data.repository

import com.devtune.domain.model.ShizukuBackend
import com.devtune.domain.model.ShizukuState
import com.devtune.platform.shizuku.ShizukuProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class ShizukuRepositoryTest {

    private val provider: ShizukuProvider = mock()

    @Test
    fun `repository emits state from provider`() = runTest {
        val expectedState = ShizukuState.Authorized(ShizukuBackend.ADB)
        whenever(provider.state).thenReturn(flowOf(expectedState))
        
        val repository = ShizukuRepositoryImpl(provider)

        val result = repository.state.first()
        assertEquals(expectedState, result)
    }
}
