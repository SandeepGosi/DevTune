package com.devtune.domain.model

sealed class AnimationExperimentResult {
    data class Success(
        val originalScales: AnimationScales,
        val testValue: Float,
        val readBackValue: Float,
        val restoredScales: AnimationScales
    ) : AnimationExperimentResult()

    data class Failure(
        val stage: ExperimentStage,
        val message: String,
        val originalScales: AnimationScales? = null,
        val currentScales: AnimationScales? = null
    ) : AnimationExperimentResult()

    enum class ExperimentStage {
        PRE_READ,
        WRITE,
        VERIFICATION,
        RESTORATION
    }
}
