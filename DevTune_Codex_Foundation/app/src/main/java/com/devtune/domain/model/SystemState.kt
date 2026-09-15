package com.devtune.domain.model

data class SystemState(
    val isDeveloperOptionsEnabled: Boolean,
    val displayDensityDpi: Int,
    val animationSettings: AnimationSettings? = null
)
