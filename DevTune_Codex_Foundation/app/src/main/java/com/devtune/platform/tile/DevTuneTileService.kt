package com.devtune.platform.tile

import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.devtune.MainActivity
import com.devtune.di.QuickSettingsEntryPoint
import com.devtune.domain.model.AnimationScales
import com.devtune.domain.model.SessionStatus
import com.devtune.domain.model.ShizukuState
import com.devtune.domain.usecase.QuickTileResult
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first

class DevTuneTileService : TileService() {

    private var listeningJob: Job? = null
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private fun getEntryPoint(): QuickSettingsEntryPoint {
        return EntryPointAccessors.fromApplication(
            applicationContext,
            QuickSettingsEntryPoint::class.java
        )
    }

    override fun onStartListening() {
        super.onStartListening()
        listeningJob?.cancel()
        listeningJob = serviceScope.launch {
            updateTileState()
        }
    }

    override fun onStopListening() {
        listeningJob?.cancel()
        listeningJob = null
        super.onStopListening()
    }

    override fun onClick() {
        super.onClick()
        serviceScope.launch {
            val entryPoint = getEntryPoint()
            val shizukuReady = entryPoint.shizukuRepository().state.first() is ShizukuState.Authorized

            if (!shizukuReady) {
                launchAppAndCollapse()
                return@launch
            }

            val session = entryPoint.temporarySessionRepository().getActiveSessionOnce()
            if (session != null && (
                session.status == SessionStatus.ACTIVE ||
                session.status == SessionStatus.RESTORING ||
                session.status == SessionStatus.RESTORE_PENDING ||
                session.status == SessionStatus.FAILED_TO_RESTORE
            )) {
                launchAppAndCollapse()
                return@launch
            }

            val result = entryPoint.toggleQuickAnimationProfileUseCase()()
            when (result) {
                is QuickTileResult.Success -> {
                    updateTileState()
                }
                is QuickTileResult.OpenAppRequired -> {
                    launchAppAndCollapse()
                }
                is QuickTileResult.Failure -> {
                    updateTileState()
                }
            }
        }
    }

    private suspend fun updateTileState() {
        val tile = qsTile ?: return
        val entryPoint = getEntryPoint()

        val isAuthorized = entryPoint.shizukuRepository().state.first() is ShizukuState.Authorized
        if (!isAuthorized) {
            tile.state = Tile.STATE_UNAVAILABLE
            tile.label = "DevTune Fast"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                tile.subtitle = "Shizuku Required"
            }
            tile.updateTile()
            return
        }

        val session = entryPoint.temporarySessionRepository().getActiveSessionOnce()
        if (session != null && (
            session.status == SessionStatus.ACTIVE ||
            session.status == SessionStatus.RESTORING ||
            session.status == SessionStatus.RESTORE_PENDING ||
            session.status == SessionStatus.FAILED_TO_RESTORE
        )) {
            tile.state = if (session.status == SessionStatus.RESTORE_PENDING) Tile.STATE_UNAVAILABLE else Tile.STATE_ACTIVE
            tile.label = if (session.status == SessionStatus.RESTORE_PENDING) "DevTune Pending" else "DevTune Temp Active"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                tile.subtitle = "Tap to open DevTune"
            }
            tile.updateTile()
            return
        }

        val scalesResult = entryPoint.animationRepository().getAnimationScalesOnce()
        val currentScales = scalesResult.getOrNull() ?: AnimationScales.DEFAULT

        if (currentScales == AnimationScales.FAST) {
            tile.state = Tile.STATE_ACTIVE
            tile.label = "DevTune Fast"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                tile.subtitle = "Restore Standard 1.00x"
            }
        } else {
            tile.state = Tile.STATE_INACTIVE
            tile.label = "DevTune Fast"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                tile.subtitle = "Apply 0.50x"
            }
        }

        tile.updateTile()
    }

    private fun launchAppAndCollapse() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startActivityAndCollapse(pendingIntent)
        } else {
            @Suppress("DEPRECATION", "StartActivityAndCollapseDeprecated")
            startActivityAndCollapse(intent)
        }
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }
}
