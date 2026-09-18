package com.devtune.domain.repository

import com.devtune.domain.model.AnimationScales
import kotlinx.coroutines.flow.Flow

interface AnimationRepository {
    fun getAnimationScales(): Flow<AnimationScales>
    suspend fun writeWindowAnimationScale(scale: Float): Boolean
    suspend fun restoreAnimationScales(scales: AnimationScales): Boolean
}
