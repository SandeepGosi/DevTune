package com.devtune.domain.usecase

import com.devtune.domain.model.AnimationError
import com.devtune.domain.repository.AnimationRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import org.mockito.kotlin.*

class VerifyAnimationWriteUseCaseTest {

    private val repository: AnimationRepository = mock()
    private val useCase = VerifyAnimationWriteUseCase(repository)

    @Test
    fun `successful capability verification returns success`() = runTest {
        whenever(repository.verifyAnimationWriteCapability()).thenReturn(Result.success(Unit))

        val result = useCase()

        assertTrue(result.isSuccess)
        verify(repository).verifyAnimationWriteCapability()
    }

    @Test
    fun `failed capability verification returns failure`() = runTest {
        whenever(repository.verifyAnimationWriteCapability()).thenReturn(Result.failure(AnimationError.ShizukuUnavailable))

        val result = useCase()

        assertTrue(result.isFailure)
        assertEquals(AnimationError.ShizukuUnavailable, result.exceptionOrNull())
        verify(repository).verifyAnimationWriteCapability()
    }
}
