package com.devtune.domain.usecase

import com.devtune.domain.repository.AnimationRepository
import javax.inject.Inject

class VerifyAnimationWriteUseCase @Inject constructor(
    private val repository: AnimationRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        return repository.verifyAnimationWriteCapability()
    }
}
