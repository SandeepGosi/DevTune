package com.devtune.domain.usecase

import com.devtune.domain.model.AnimationError
import com.devtune.domain.model.AnimationScales
import com.devtune.domain.model.SessionStatus
import com.devtune.domain.repository.AnimationRepository
import com.devtune.domain.repository.TemporarySessionRepository
import javax.inject.Inject

sealed class QuickTileResult {
    data class Success(val appliedScales: AnimationScales) : QuickTileResult()
    object OpenAppRequired : QuickTileResult()
    data class Failure(val error: Throwable) : QuickTileResult()
}

class ToggleQuickAnimationProfileUseCase @Inject constructor(
    private val animationRepository: AnimationRepository,
    private val temporarySessionRepository: TemporarySessionRepository
) {
    suspend operator fun invoke(): QuickTileResult {
        val session = temporarySessionRepository.getActiveSessionOnce()
        if (session != null && (
            session.status == SessionStatus.ACTIVE ||
            session.status == SessionStatus.RESTORING ||
            session.status == SessionStatus.RESTORE_PENDING ||
            session.status == SessionStatus.FAILED_TO_RESTORE
        )) {
            return QuickTileResult.OpenAppRequired
        }

        val currentResult = animationRepository.getAnimationScalesOnce()
        if (currentResult.isFailure) {
            return QuickTileResult.Failure(currentResult.exceptionOrNull() ?: AnimationError.ReadFailed)
        }

        val current = currentResult.getOrThrow()
        val targetScales = if (current == AnimationScales.FAST) {
            AnimationScales.DEFAULT
        } else {
            AnimationScales.FAST
        }

        val applyResult = animationRepository.applyAnimationScales(targetScales)
        return if (applyResult.isSuccess) {
            QuickTileResult.Success(targetScales)
        } else {
            QuickTileResult.Failure(applyResult.exceptionOrNull() ?: AnimationError.WriteFailed(0, null))
        }
    }
}
