package com.devtune.domain.usecase

import com.devtune.domain.model.AnimationScales
import com.devtune.domain.repository.AnimationRepository
import javax.inject.Inject

class SetAnimationScalesUseCase @Inject constructor(
    private val animationRepository: AnimationRepository
) {
    suspend operator fun invoke(scales: AnimationScales): Result<Unit> {
        return animationRepository.applyAnimationScales(scales)
    }

    suspend fun setSingleScale(which: Int, scale: Float): Result<Unit> {
        return animationRepository.setAnimationScale(which, scale)
    }
}
