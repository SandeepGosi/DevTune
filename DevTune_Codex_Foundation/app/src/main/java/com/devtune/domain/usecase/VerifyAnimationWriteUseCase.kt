package com.devtune.domain.usecase

import com.devtune.domain.model.AnimationExperimentResult
import com.devtune.domain.model.AnimationExperimentResult.*
import com.devtune.domain.model.AnimationScales
import com.devtune.domain.repository.AnimationRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class VerifyAnimationWriteUseCase @Inject constructor(
    private val repository: AnimationRepository
) {
    suspend operator fun invoke(): AnimationExperimentResult {
        // 1. Pre-Read
        val original = try {
            repository.getAnimationScales().first()
        } catch (e: Exception) {
            return Failure(ExperimentStage.PRE_READ, "Failed to read initial scales: ${e.message}")
        }

        // 2. Write (Experiment: Set Window Scale to 0.5)
        val testValue = 0.5f
        val writeSuccess = try {
            repository.writeWindowAnimationScale(testValue)
        } catch (e: Exception) {
            return Failure(ExperimentStage.WRITE, "Write exception: ${e.message}", original)
        }

        if (!writeSuccess) {
            return Failure(ExperimentStage.WRITE, "Write reported failure", original)
        }

        // 3. Verification (Read Back)
        val readBack = repository.getAnimationScales().first()
        val isVerified = Math.abs(readBack.windowScale - testValue) < 0.01f

        if (!isVerified) {
            // Attempt restoration before returning failure
            repository.restoreAnimationScales(original)
            return Failure(
                ExperimentStage.VERIFICATION,
                "Verification failed. Requested $testValue, but read back ${readBack.windowScale}",
                original,
                readBack
            )
        }

        // 4. Restoration
        val restoreSuccess = try {
            repository.restoreAnimationScales(original)
        } catch (e: Exception) {
            false
        }

        val finalRead = repository.getAnimationScales().first()
        val restorationVerified = Math.abs(finalRead.windowScale - original.windowScale) < 0.01f

        return if (restoreSuccess && restorationVerified) {
            Success(original, testValue, readBack.windowScale, finalRead)
        } else {
            Failure(
                ExperimentStage.RESTORATION,
                "Restoration failed or could not be verified. Current: $finalRead",
                original,
                finalRead
            )
        }
    }
}
