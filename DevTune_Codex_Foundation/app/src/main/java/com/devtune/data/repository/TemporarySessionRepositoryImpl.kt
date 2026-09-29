package com.devtune.data.repository

import com.devtune.data.datastore.TemporarySessionDataSource
import com.devtune.domain.model.SessionStatus
import com.devtune.domain.model.TemporaryAnimationSession
import com.devtune.domain.repository.TemporarySessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TemporarySessionRepositoryImpl @Inject constructor(
    private val dataSource: TemporarySessionDataSource
) : TemporarySessionRepository {

    override val activeSession: Flow<TemporaryAnimationSession?> = dataSource.activeSession

    override suspend fun getActiveSessionOnce(): TemporaryAnimationSession? {
        return dataSource.activeSession.firstOrNull()
    }

    override suspend fun saveSession(session: TemporaryAnimationSession) {
        dataSource.saveSession(session)
    }

    override suspend fun updateStatus(status: SessionStatus) {
        dataSource.updateStatus(status)
    }

    override suspend fun clearSession() {
        dataSource.clearSession()
    }
}
