package com.devtune.data.repository

import android.provider.Settings
import com.devtune.domain.model.AnimationScales
import com.devtune.domain.repository.AnimationRepository
import com.devtune.platform.shizuku.ShizukuAnimationProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnimationRepositoryImpl @Inject constructor(
    private val provider: ShizukuAnimationProvider
) : AnimationRepository {

    override fun getAnimationScales(): Flow<AnimationScales> = flow {
        while (true) {
            emit(provider.readScales())
            delay(1000) // Poll for changes as system settings don't always notify
        }
    }

    override suspend fun writeWindowAnimationScale(scale: Float): Boolean {
        return provider.writeScale(Settings.Global.WINDOW_ANIMATION_SCALE, scale)
    }

    override suspend fun restoreAnimationScales(scales: AnimationScales): Boolean {
        val w = provider.writeScale(Settings.Global.WINDOW_ANIMATION_SCALE, scales.windowScale)
        val t = provider.writeScale(Settings.Global.TRANSITION_ANIMATION_SCALE, scales.transitionScale)
        val a = provider.writeScale(Settings.Global.ANIMATOR_DURATION_SCALE, scales.animatorScale)
        return w && t && a
    }
}
