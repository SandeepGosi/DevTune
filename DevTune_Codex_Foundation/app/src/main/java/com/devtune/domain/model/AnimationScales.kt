package com.devtune.domain.model

data class AnimationScales(
    val windowScale: Float,
    val transitionScale: Float,
    val animatorScale: Float
) {
    init {
        validateScale(windowScale, "windowScale")
        validateScale(transitionScale, "transitionScale")
        validateScale(animatorScale, "animatorScale")
    }

    companion object {
        val DEFAULT = AnimationScales(1.0f, 1.0f, 1.0f)
        val FAST = AnimationScales(0.5f, 0.5f, 0.5f)
        val NO_ANIMATIONS = AnimationScales(0.0f, 0.0f, 0.0f)

        fun validateScale(scale: Float, name: String = "Scale") {
            if (scale.isNaN()) {
                throw AnimationError.InvalidScaleValue(scale, "$name cannot be NaN")
            }
            if (scale.isInfinite()) {
                throw AnimationError.InvalidScaleValue(scale, "$name cannot be infinite")
            }
            if (scale < 0.0f || scale > 10.0f) {
                throw AnimationError.InvalidScaleValue(scale, "$name must be between 0.0x and 10.0x")
            }
        }
    }
}
