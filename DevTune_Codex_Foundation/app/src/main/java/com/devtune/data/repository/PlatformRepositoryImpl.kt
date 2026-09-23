package com.devtune.data.repository

import android.os.Build
import com.devtune.domain.model.CapabilityState
import com.devtune.domain.model.PlatformCapability
import com.devtune.domain.model.ShizukuState
import com.devtune.domain.model.SystemState
import com.devtune.domain.repository.PlatformRepository
import com.devtune.domain.repository.ShizukuRepository
import com.devtune.domain.repository.UserPreferencesRepository
import com.devtune.platform.SystemControlProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlatformRepositoryImpl @Inject constructor(
    private val systemControlProvider: SystemControlProvider,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val shizukuRepository: ShizukuRepository
) : PlatformRepository {

    override fun getCapabilityState(capability: PlatformCapability): CapabilityState {
        if (capability == PlatformCapability.WRITE_ANIMATION_SETTINGS) {
            val prefs = runBlocking { userPreferencesRepository.userPreferences.first() }
            val currentShizukuState = runBlocking { shizukuRepository.state.first() }

            if (currentShizukuState is ShizukuState.Authorized && prefs.lastVerifiedSuccess) {
                val currentBackend = currentShizukuState.backend.name
                val currentSdk = Build.VERSION.SDK_INT
                if (prefs.lastVerifiedBackend == currentBackend && prefs.lastVerifiedAndroidVersion == currentSdk) {
                    return CapabilityState.AVAILABLE
                }
            }
            return CapabilityState.RESTRICTED
        }
        return systemControlProvider.getCapabilityState(capability)
    }

    override fun getSystemState(): Flow<SystemState> = flow {
        while (true) {
            emit(
                SystemState(
                    isDeveloperOptionsEnabled = systemControlProvider.isDeveloperOptionsEnabled(),
                    displayDensityDpi = systemControlProvider.getDisplayDensityDpi(),
                    animationSettings = systemControlProvider.getAnimationSettings()
                )
            )
            delay(2000) // Poll every 2 seconds for read-only M3
        }
    }

    override fun openDeveloperOptions(): Boolean {
        return systemControlProvider.openDeveloperOptions()
    }

    override fun openDeviceInfoSettings(): Boolean {
        return systemControlProvider.openDeviceInfoSettings()
    }

    override fun openDisplaySettings(): Boolean {
        return systemControlProvider.openDisplaySettings()
    }
}
