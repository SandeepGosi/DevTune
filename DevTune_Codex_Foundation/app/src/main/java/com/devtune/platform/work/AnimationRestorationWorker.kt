package com.devtune.platform.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.devtune.domain.repository.TemporarySessionRepository
import com.devtune.domain.usecase.RestoreTemporarySessionUseCase
import com.devtune.platform.TimeProvider

class AnimationRestorationWorker(
    context: Context,
    params: WorkerParameters,
    private val temporarySessionRepository: TemporarySessionRepository,
    private val restoreTemporarySessionUseCase: RestoreTemporarySessionUseCase,
    private val timeProvider: TimeProvider
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val session = temporarySessionRepository.getActiveSessionOnce() ?: return Result.success()

        val now = timeProvider.currentTimeMillis()
        if (now < session.expiresAtEpochMillis) {
            return Result.retry()
        }

        val restoreResult = restoreTemporarySessionUseCase()
        return if (restoreResult.isSuccess) {
            Result.success()
        } else {
            Result.retry()
        }
    }
}
