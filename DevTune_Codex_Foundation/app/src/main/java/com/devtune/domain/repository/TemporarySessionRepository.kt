package com.devtune.domain.repository

import com.devtune.domain.model.SessionStatus
import com.devtune.domain.model.TemporaryAnimationSession
import kotlinx.coroutines.flow.Flow

interface TemporarySessionRepository {
    val activeSession: Flow<TemporaryAnimationSession?>
    suspend fun getActiveSessionOnce(): TemporaryAnimationSession?
    suspend fun saveSession(session: TemporaryAnimationSession)
    suspend fun updateStatus(status: SessionStatus)
    suspend fun clearSession()
}
