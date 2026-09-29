package com.devtune.domain.repository

import com.devtune.domain.model.AnimationScales
import kotlinx.coroutines.flow.Flow

interface AnimationRepository {
    fun getAnimationScales(): Flow<AnimationScales>
    suspend fun getAnimationScalesOnce(): Result<AnimationScales>
    suspend fun setAnimationScale(which: Int, scale: Float): Result<Unit>
    suspend fun applyAnimationScales(scales: AnimationScales): Result<Unit>
    suspend fun verifyAnimationWriteCapability(): Result<Unit>
}
