package com.devtune.domain.usecase

import com.devtune.domain.model.AnimationScales
import com.devtune.domain.model.SessionStatus
import com.devtune.domain.model.TemporaryAnimationSession
import com.devtune.domain.repository.TemporarySessionRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*

@OptIn(ExperimentalCoroutinesApi::class)
class RestoreTemporarySessionUseCaseTest {

    private val temporarySessionRepository: TemporarySessionRepository = mock()
    private val setAnimationScalesUseCase: SetAnimationScalesUseCase = mock()

    private lateinit var useCase: RestoreTemporarySessionUseCase

    @Before
    fun setUp() {
        useCase = RestoreTemporarySessionUseCase(
            temporarySessionRepository,
            setAnimationScalesUseCase
        )
    }

    @Test
    fun `invoke succeeds if no active session exists`() = runTest {
        whenever(temporarySessionRepository.getActiveSessionOnce()).thenReturn(null)

        val result = useCase()

        assertTrue(result.isSuccess)
        verifyNoInteractions(setAnimationScalesUseCase)
    }

    @Test
    fun `invoke sets RESTORING, applies original scales, and clears session on success`() = runTest {
        val originalScales = AnimationScales(0.75f, 1.25f, 0.5f)
        val activeSession = TemporaryAnimationSession(
            sessionId = "sess-123",
            startedAtEpochMillis = 1000L,
            expiresAtEpochMillis = 2000L,
            originalScales = originalScales,
            targetScales = AnimationScales.FAST,
            status = SessionStatus.ACTIVE
        )
        whenever(temporarySessionRepository.getActiveSessionOnce()).thenReturn(activeSession)
        whenever(setAnimationScalesUseCase(originalScales)).thenReturn(Result.success(Unit))

        val result = useCase()

        assertTrue(result.isSuccess)
        verify(temporarySessionRepository).updateStatus(SessionStatus.RESTORING)
        verify(setAnimationScalesUseCase).invoke(originalScales)
        verify(temporarySessionRepository).updateStatus(SessionStatus.COMPLETED)
        verify(temporarySessionRepository).clearSession()
    }

    @Test
    fun `invoke updates status to FAILED_TO_RESTORE on restoration failure`() = runTest {
        val originalScales = AnimationScales(0.75f, 1.25f, 0.5f)
        val activeSession = TemporaryAnimationSession(
            sessionId = "sess-123",
            startedAtEpochMillis = 1000L,
            expiresAtEpochMillis = 2000L,
            originalScales = originalScales,
            targetScales = AnimationScales.FAST,
            status = SessionStatus.ACTIVE
        )
        whenever(temporarySessionRepository.getActiveSessionOnce()).thenReturn(activeSession)
        whenever(setAnimationScalesUseCase(originalScales)).thenReturn(Result.failure(RuntimeException("Restoration failed")))

        val result = useCase()

        assertTrue(result.isFailure)
        verify(temporarySessionRepository).updateStatus(SessionStatus.RESTORING)
        verify(setAnimationScalesUseCase).invoke(originalScales)
        verify(temporarySessionRepository).updateStatus(SessionStatus.FAILED_TO_RESTORE)
        verify(temporarySessionRepository, never()).clearSession()
    }
}
