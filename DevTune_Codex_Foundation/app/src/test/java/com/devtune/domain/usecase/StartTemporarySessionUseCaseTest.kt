package com.devtune.domain.usecase

import com.devtune.domain.model.*
import com.devtune.domain.repository.AnimationRepository
import com.devtune.domain.repository.TemporarySessionRepository
import com.devtune.platform.TimeProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*

@OptIn(ExperimentalCoroutinesApi::class)
class StartTemporarySessionUseCaseTest {

    private val animationRepository: AnimationRepository = mock()
    private val temporarySessionRepository: TemporarySessionRepository = mock()
    private val setAnimationScalesUseCase: SetAnimationScalesUseCase = mock()
    private val timeProvider: TimeProvider = mock()

    private lateinit var useCase: StartTemporarySessionUseCase

    @Before
    fun setUp() {
        whenever(timeProvider.currentTimeMillis()).thenReturn(1000000L)
        useCase = StartTemporarySessionUseCase(
            animationRepository,
            temporarySessionRepository,
            setAnimationScalesUseCase,
            timeProvider
        )
    }

    @Test
    fun `invoke fails if a session is already active`() = runTest {
        val activeSession = TemporaryAnimationSession(
            sessionId = "existing-123",
            startedAtEpochMillis = 900000L,
            expiresAtEpochMillis = 1800000L,
            originalScales = AnimationScales.DEFAULT,
            targetScales = AnimationScales.FAST,
            status = SessionStatus.ACTIVE
        )
        whenever(temporarySessionRepository.getActiveSessionOnce()).thenReturn(activeSession)

        val result = useCase(AnimationScales.FAST, TemporaryDuration.THIRTY_MINUTES)

        assertTrue(result.isFailure)
        verifyNoInteractions(setAnimationScalesUseCase)
    }

    @Test
    fun `invoke captures current scales, persists APPLYING, and activates on success`() = runTest {
        whenever(temporarySessionRepository.getActiveSessionOnce()).thenReturn(null)
        whenever(animationRepository.getAnimationScalesOnce()).thenReturn(Result.success(AnimationScales.DEFAULT))
        whenever(setAnimationScalesUseCase(AnimationScales.FAST)).thenReturn(Result.success(Unit))

        val result = useCase(AnimationScales.FAST, TemporaryDuration.THIRTY_MINUTES)

        assertTrue(result.isSuccess)
        val session = result.getOrThrow()
        assertEquals(SessionStatus.ACTIVE, session.status)
        assertEquals(AnimationScales.DEFAULT, session.originalScales)
        assertEquals(AnimationScales.FAST, session.targetScales)
        assertEquals(1000000L + 30 * 60 * 1000L, session.expiresAtEpochMillis)

        verify(setAnimationScalesUseCase).invoke(AnimationScales.FAST)
        verify(temporarySessionRepository, times(2)).saveSession(any())
    }

    @Test
    fun `invoke rolls back on apply failure`() = runTest {
        whenever(temporarySessionRepository.getActiveSessionOnce()).thenReturn(null)
        whenever(animationRepository.getAnimationScalesOnce()).thenReturn(Result.success(AnimationScales.DEFAULT))
        whenever(setAnimationScalesUseCase(AnimationScales.FAST)).thenReturn(Result.failure(RuntimeException("Write failed")))
        whenever(setAnimationScalesUseCase(AnimationScales.DEFAULT)).thenReturn(Result.success(Unit))

        val result = useCase(AnimationScales.FAST, TemporaryDuration.THIRTY_MINUTES)

        assertTrue(result.isFailure)
        verify(setAnimationScalesUseCase).invoke(AnimationScales.DEFAULT)
        verify(temporarySessionRepository).clearSession()
    }
}
