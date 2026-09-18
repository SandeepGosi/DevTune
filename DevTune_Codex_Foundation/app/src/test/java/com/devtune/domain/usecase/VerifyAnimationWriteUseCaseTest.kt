package com.devtune.domain.usecase

import com.devtune.domain.model.AnimationExperimentResult
import com.devtune.domain.model.AnimationScales
import com.devtune.domain.repository.AnimationRepository
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import org.mockito.kotlin.*

class VerifyAnimationWriteUseCaseTest {

    private val repository: AnimationRepository = mock()
    private val useCase = VerifyAnimationWriteUseCase(repository)

    @Test
    fun `successful experiment returns success result`() = runTest {
        val original = AnimationScales(1.0f, 1.0f, 1.0f)
        val testValue = 0.5f
        
        whenever(repository.getAnimationScales()).thenReturn(
            flowOf(original), // Pre-read
            flowOf(AnimationScales(testValue, 1.0f, 1.0f)), // Read back
            flowOf(original) // Post-restoration read
        )
        whenever(repository.writeWindowAnimationScale(any())).thenReturn(true)
        whenever(repository.restoreAnimationScales(any())).thenReturn(true)

        val result = useCase()

        assertTrue(result is AnimationExperimentResult.Success)
        val success = result as AnimationExperimentResult.Success
        assertEquals(original, success.originalScales)
        assertEquals(testValue, success.readBackValue)
    }

    @Test
    fun `write failure returns write failure result`() = runTest {
        val original = AnimationScales(1.0f, 1.0f, 1.0f)
        whenever(repository.getAnimationScales()).thenReturn(flowOf(original))
        whenever(repository.writeWindowAnimationScale(any())).thenReturn(false)

        val result = useCase()

        assertTrue(result is AnimationExperimentResult.Failure)
        assertEquals(AnimationExperimentResult.ExperimentStage.WRITE, (result as AnimationExperimentResult.Failure).stage)
    }

    @Test
    fun `verification failure returns verification failure result`() = runTest {
        val original = AnimationScales(1.0f, 1.0f, 1.0f)
        whenever(repository.getAnimationScales()).thenReturn(
            flowOf(original),
            flowOf(original) // Read back same as original despite "successful" write
        )
        whenever(repository.writeWindowAnimationScale(any())).thenReturn(true)

        val result = useCase()

        assertTrue(result is AnimationExperimentResult.Failure)
        assertEquals(AnimationExperimentResult.ExperimentStage.VERIFICATION, (result as AnimationExperimentResult.Failure).stage)
        verify(repository).restoreAnimationScales(original)
    }

    @Test
    fun `restoration failure returns restoration failure result`() = runTest {
        val original = AnimationScales(1.0f, 1.0f, 1.0f)
        val testValue = 0.5f
        whenever(repository.getAnimationScales()).thenReturn(
            flowOf(original),
            flowOf(AnimationScales(testValue, 1.0f, 1.0f)),
            flowOf(AnimationScales(testValue, 1.0f, 1.0f)) // Still at test value after restoration
        )
        whenever(repository.writeWindowAnimationScale(any())).thenReturn(true)
        whenever(repository.restoreAnimationScales(any())).thenReturn(false)

        val result = useCase()

        assertTrue(result is AnimationExperimentResult.Failure)
        assertEquals(AnimationExperimentResult.ExperimentStage.RESTORATION, (result as AnimationExperimentResult.Failure).stage)
    }
}
