package com.devtune.domain.usecase

import com.devtune.domain.model.AnimationScales
import com.devtune.domain.model.SessionStatus
import com.devtune.domain.model.TemporaryAnimationSession
import com.devtune.domain.repository.AnimationRepository
import com.devtune.domain.repository.TemporarySessionRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*

@OptIn(ExperimentalCoroutinesApi::class)
class ToggleQuickAnimationProfileUseCaseTest {

    private val animationRepository: AnimationRepository = mock()
    private val temporarySessionRepository: TemporarySessionRepository = mock()

    private lateinit var useCase: ToggleQuickAnimationProfileUseCase

    @Before
    fun setUp() {
        useCase = ToggleQuickAnimationProfileUseCase(
            animationRepository,
            temporarySessionRepository
        )
    }

    @Test
    fun `invoke returns OpenAppRequired when M7 temporary session is active`() = runTest {
        val activeSession = TemporaryAnimationSession(
            sessionId = "sess-1",
            startedAtEpochMillis = 1000L,
            expiresAtEpochMillis = 2000L,
            originalScales = AnimationScales.DEFAULT,
            targetScales = AnimationScales.FAST,
            status = SessionStatus.ACTIVE
        )
        whenever(temporarySessionRepository.getActiveSessionOnce()).thenReturn(activeSession)

        val result = useCase()

        assertEquals(QuickTileResult.OpenAppRequired, result)
        verifyNoInteractions(animationRepository)
    }

    @Test
    fun `invoke toggles Standard 1_0x to Fast 0_5x when no session is active`() = runTest {
        whenever(temporarySessionRepository.getActiveSessionOnce()).thenReturn(null)
        whenever(animationRepository.getAnimationScalesOnce()).thenReturn(Result.success(AnimationScales.DEFAULT))
        whenever(animationRepository.applyAnimationScales(AnimationScales.FAST)).thenReturn(Result.success(Unit))

        val result = useCase()

        assertTrue(result is QuickTileResult.Success)
        assertEquals(AnimationScales.FAST, (result as QuickTileResult.Success).appliedScales)
        verify(animationRepository).applyAnimationScales(AnimationScales.FAST)
    }

    @Test
    fun `invoke toggles Fast 0_5x to Standard 1_0x when current scale is Fast`() = runTest {
        whenever(temporarySessionRepository.getActiveSessionOnce()).thenReturn(null)
        whenever(animationRepository.getAnimationScalesOnce()).thenReturn(Result.success(AnimationScales.FAST))
        whenever(animationRepository.applyAnimationScales(AnimationScales.DEFAULT)).thenReturn(Result.success(Unit))

        val result = useCase()

        assertTrue(result is QuickTileResult.Success)
        assertEquals(AnimationScales.DEFAULT, (result as QuickTileResult.Success).appliedScales)
        verify(animationRepository).applyAnimationScales(AnimationScales.DEFAULT)
    }
}
