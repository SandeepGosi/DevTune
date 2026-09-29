package com.devtune.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.devtune.domain.model.AnimationScales
import com.devtune.domain.model.SessionStatus
import com.devtune.domain.model.TemporaryAnimationSession
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.sessionDataStore: DataStore<Preferences> by preferencesDataStore(name = "devtune_temporary_session")

@Singleton
class TemporarySessionDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {
    val activeSession: Flow<TemporaryAnimationSession?> = context.sessionDataStore.data.map { prefs ->
        val sessionId = prefs[SessionIdKey] ?: return@map null
        val startedAt = prefs[StartedAtKey] ?: 0L
        val expiresAt = prefs[ExpiresAtKey] ?: 0L

        val origWin = prefs[OrigWinKey] ?: 1.0f
        val origTrans = prefs[OrigTransKey] ?: 1.0f
        val origAnim = prefs[OrigAnimKey] ?: 1.0f

        val targetWin = prefs[TargetWinKey] ?: 1.0f
        val targetTrans = prefs[TargetTransKey] ?: 1.0f
        val targetAnim = prefs[TargetAnimKey] ?: 1.0f

        val statusName = prefs[StatusKey] ?: SessionStatus.IDLE.name
        val status = try {
            SessionStatus.valueOf(statusName)
        } catch (e: Exception) {
            SessionStatus.IDLE
        }

        TemporaryAnimationSession(
            sessionId = sessionId,
            startedAtEpochMillis = startedAt,
            expiresAtEpochMillis = expiresAt,
            originalScales = AnimationScales(origWin, origTrans, origAnim),
            targetScales = AnimationScales(targetWin, targetTrans, targetAnim),
            status = status
        )
    }

    suspend fun saveSession(session: TemporaryAnimationSession) {
        context.sessionDataStore.edit { prefs ->
            prefs[SessionIdKey] = session.sessionId
            prefs[StartedAtKey] = session.startedAtEpochMillis
            prefs[ExpiresAtKey] = session.expiresAtEpochMillis
            prefs[OrigWinKey] = session.originalScales.windowScale
            prefs[OrigTransKey] = session.originalScales.transitionScale
            prefs[OrigAnimKey] = session.originalScales.animatorScale
            prefs[TargetWinKey] = session.targetScales.windowScale
            prefs[TargetTransKey] = session.targetScales.transitionScale
            prefs[TargetAnimKey] = session.targetScales.animatorScale
            prefs[StatusKey] = session.status.name
        }
    }

    suspend fun updateStatus(status: SessionStatus) {
        context.sessionDataStore.edit { prefs ->
            prefs[StatusKey] = status.name
        }
    }

    suspend fun clearSession() {
        context.sessionDataStore.edit { prefs ->
            prefs.clear()
        }
    }

    companion object {
        private val SessionIdKey = stringPreferencesKey("session_id")
        private val StartedAtKey = longPreferencesKey("started_at_ms")
        private val ExpiresAtKey = longPreferencesKey("expires_at_ms")
        private val OrigWinKey = floatPreferencesKey("orig_win_scale")
        private val OrigTransKey = floatPreferencesKey("orig_trans_scale")
        private val OrigAnimKey = floatPreferencesKey("orig_anim_scale")
        private val TargetWinKey = floatPreferencesKey("target_win_scale")
        private val TargetTransKey = floatPreferencesKey("target_trans_scale")
        private val TargetAnimKey = floatPreferencesKey("target_anim_scale")
        private val StatusKey = stringPreferencesKey("session_status")
    }
}
