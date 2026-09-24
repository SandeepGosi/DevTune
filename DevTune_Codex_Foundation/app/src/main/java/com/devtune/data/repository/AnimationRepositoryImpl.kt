package com.devtune.data.repository

import com.devtune.domain.model.AnimationError
import com.devtune.domain.model.AnimationScales
import com.devtune.domain.repository.AnimationRepository
import com.devtune.platform.SystemControlProvider
import com.devtune.platform.shizuku.PrivilegedSystemBridge
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

@Singleton
class AnimationRepositoryImpl @Inject constructor(
    private val systemControlProvider: SystemControlProvider,
    private val privilegedBridge: PrivilegedSystemBridge
) : AnimationRepository {

    override fun getAnimationScales(): Flow<AnimationScales> = flow {
        while (true) {
            val settings = systemControlProvider.getAnimationSettings()
            val window = settings?.windowScale ?: 1.0f
            val transition = settings?.transitionScale ?: 1.0f
            val animator = settings?.animatorDurationScale ?: 1.0f
            emit(AnimationScales(window, transition, animator))
            delay(1000)
        }
    }

    override suspend fun setAnimationScale(which: Int, scale: Float): Result<Unit> {
        try {
            AnimationScales.validateScale(scale, "which=$which")
        } catch (e: Exception) {
            return Result.failure(e)
        }

        if (!privilegedBridge.isShizukuReady()) {
            return Result.failure(AnimationError.ShizukuUnavailable)
        }

        return privilegedBridge.setAnimationScale(which, scale)
    }

    override suspend fun applyAnimationScales(scales: AnimationScales): Result<Unit> {
        try {
            AnimationScales.validateScale(scales.windowScale, "windowScale")
            AnimationScales.validateScale(scales.transitionScale, "transitionScale")
            AnimationScales.validateScale(scales.animatorScale, "animatorScale")
        } catch (e: Exception) {
            return Result.failure(e)
        }

        if (!privilegedBridge.isShizukuReady()) {
            return Result.failure(AnimationError.ShizukuUnavailable)
        }

        // 1. Capture original scales for rollback protection
        val origSettings = systemControlProvider.getAnimationSettings()
        val origWin = origSettings?.windowScale ?: 1.0f
        val origTrans = origSettings?.transitionScale ?: 1.0f
        val origAnim = origSettings?.animatorDurationScale ?: 1.0f

        // 2. Write all requested scales individually
        val winResult = privilegedBridge.setAnimationScale(0, scales.windowScale)
        if (winResult.isFailure) {
            rollback(origWin, origTrans, origAnim)
            return Result.failure(AnimationError.WriteFailed(0, winResult.exceptionOrNull()))
        }

        val transResult = privilegedBridge.setAnimationScale(1, scales.transitionScale)
        if (transResult.isFailure) {
            rollback(origWin, origTrans, origAnim)
            return Result.failure(AnimationError.WriteFailed(1, transResult.exceptionOrNull()))
        }

        val animResult = privilegedBridge.setAnimationScale(2, scales.animatorScale)
        if (animResult.isFailure) {
            rollback(origWin, origTrans, origAnim)
            return Result.failure(AnimationError.WriteFailed(2, animResult.exceptionOrNull()))
        }

        // 3. Read back and verify requested scales
        val readSettings = systemControlProvider.getAnimationSettings()
        val readWin = readSettings?.windowScale ?: 1.0f
        val readTrans = readSettings?.transitionScale ?: 1.0f
        val readAnim = readSettings?.animatorDurationScale ?: 1.0f

        if (abs(readWin - scales.windowScale) > 0.01f ||
            abs(readTrans - scales.transitionScale) > 0.01f ||
            abs(readAnim - scales.animatorScale) > 0.01f
        ) {
            rollback(origWin, origTrans, origAnim)
            return Result.failure(AnimationError.ReadBackFailed(scales.windowScale, readWin))
        }

        return Result.success(Unit)
    }

    override suspend fun verifyAnimationWriteCapability(): Result<Unit> {
        if (!privilegedBridge.isShizukuReady()) {
            return Result.failure(AnimationError.ShizukuUnavailable)
        }

        val origSettings = systemControlProvider.getAnimationSettings()
        val origWin = origSettings?.windowScale ?: 1.0f
        val origTrans = origSettings?.transitionScale ?: 1.0f
        val origAnim = origSettings?.animatorDurationScale ?: 1.0f

        val testScale = 0.5f
        var verificationSuccess = false

        try {
            val writeResult = privilegedBridge.setAnimationScale(0, testScale)
            if (writeResult.isSuccess) {
                val readWin = systemControlProvider.getAnimationSettings()?.windowScale ?: 1.0f
                if (abs(readWin - testScale) < 0.01f) {
                    verificationSuccess = true
                }
            }
        } finally {
            val restoreResult = rollback(origWin, origTrans, origAnim)
            val finalWin = systemControlProvider.getAnimationSettings()?.windowScale ?: 1.0f
            if (restoreResult.isFailure || abs(finalWin - origWin) > 0.01f) {
                verificationSuccess = false
            }
        }

        return if (verificationSuccess) {
            Result.success(Unit)
        } else {
            Result.failure(AnimationError.WriteFailed(0, null))
        }
    }

    private suspend fun rollback(origWin: Float, origTrans: Float, origAnim: Float): Result<Unit> {
        return privilegedBridge.setAnimationScales(origWin, origTrans, origAnim)
    }
}
