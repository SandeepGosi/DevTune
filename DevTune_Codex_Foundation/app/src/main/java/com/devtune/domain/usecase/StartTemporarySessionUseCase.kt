package com.devtune.domain.usecase

import com.devtune.domain.model.*
import com.devtune.domain.repository.AnimationRepository
import com.devtune.domain.repository.TemporarySessionRepository
import com.devtune.platform.TimeProvider
import java.util.UUID
import javax.inject.Inject

class StartTemporarySessionUseCase @Inject constructor(
    private val animationRepository: AnimationRepository,
    private val temporarySessionRepository: TemporarySessionRepository,
    private val setAnimationScalesUseCase: SetAnimationScalesUseCase,
    private val timeProvider: TimeProvider
) {
    suspend operator fun invoke(
        targetScales: AnimationScales,
        duration: TemporaryDuration
    ): Result<TemporaryAnimationSession> {
        val existingSession = temporarySessionRepository.getActiveSessionOnce()
        if (existingSession != null && (existingSession.status == SessionStatus.ACTIVE || existingSession.status == SessionStatus.RESTORE_PENDING)) {
            return Result.failure(IllegalStateException("A temporary profile session is already active"))
        }

        val originalResult = animationRepository.getAnimationScalesOnce()
        if (originalResult.isFailure) {
            return Result.failure(originalResult.exceptionOrNull() ?: IllegalStateException("Failed to read current animation scales"))
        }
        val originalScales = originalResult.getOrThrow()

        val now = timeProvider.currentTimeMillis()
        val expiresAt = now + (duration.durationMinutes * 60 * 1000L)
        val sessionId = UUID.randomUUID().toString()

        val session = TemporaryAnimationSession(
            sessionId = sessionId,
            startedAtEpochMillis = now,
            expiresAtEpochMillis = expiresAt,
            originalScales = originalScales,
            targetScales = targetScales,
            status = SessionStatus.APPLYING
        )

        temporarySessionRepository.saveSession(session)

        val applyResult = setAnimationScalesUseCase(targetScales)

        return if (applyResult.isSuccess) {
            val activeSession = session.copy(status = SessionStatus.ACTIVE)
            temporarySessionRepository.saveSession(activeSession)
            Result.success(activeSession)
        } else {
            val rollbackResult = setAnimationScalesUseCase(originalScales)
            if (rollbackResult.isSuccess) {
                temporarySessionRepository.saveSession(session.copy(status = SessionStatus.FAILED_TO_APPLY))
                temporarySessionRepository.clearSession()
                Result.failure(applyResult.exceptionOrNull() ?: IllegalStateException("Failed to apply target scales"))
            } else {
                temporarySessionRepository.saveSession(session.copy(status = SessionStatus.RESTORE_PENDING))
                Result.failure(IllegalStateException("Failed to apply target scales and rollback failed"))
            }
        }
    }
}
