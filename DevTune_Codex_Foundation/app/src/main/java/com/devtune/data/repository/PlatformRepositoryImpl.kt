package com.devtune.data.repository

import com.devtune.domain.model.CapabilityState
import com.devtune.domain.model.PlatformCapability
import com.devtune.domain.model.SystemState
import com.devtune.domain.repository.PlatformRepository
import com.devtune.platform.SystemControlProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlatformRepositoryImpl @Inject constructor(
    private val systemControlProvider: SystemControlProvider
) : PlatformRepository {

    override fun getCapabilityState(capability: PlatformCapability): CapabilityState {
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
