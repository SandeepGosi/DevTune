package com.devtune.domain.model

data class AnimationScales(
    val windowScale: Float,
    val transitionScale: Float,
    val animatorScale: Float
) {
    companion object {
        val DEFAULT = AnimationScales(1.0f, 1.0f, 1.0f)
    }
}
