package com.devtune.domain.usecase

import com.devtune.domain.model.SessionStatus
import com.devtune.domain.repository.TemporarySessionRepository
import javax.inject.Inject

class RestoreTemporarySessionUseCase @Inject constructor(
    private val temporarySessionRepository: TemporarySessionRepository,
    private val setAnimationScalesUseCase: SetAnimationScalesUseCase
) {
    suspend operator fun invoke(): Result<Unit> {
        val session = temporarySessionRepository.getActiveSessionOnce()
            ?: return Result.success(Unit)

        if (session.status == SessionStatus.COMPLETED) {
            return Result.success(Unit)
        }

        temporarySessionRepository.updateStatus(SessionStatus.RESTORING)

        val restoreResult = setAnimationScalesUseCase(session.originalScales)

        return if (restoreResult.isSuccess) {
            temporarySessionRepository.updateStatus(SessionStatus.COMPLETED)
            temporarySessionRepository.clearSession()
            Result.success(Unit)
        } else {
            temporarySessionRepository.updateStatus(SessionStatus.FAILED_TO_RESTORE)
            Result.failure(restoreResult.exceptionOrNull() ?: IllegalStateException("Failed to restore original scales"))
        }
    }
}
