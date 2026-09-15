package com.devtune.domain.repository

import com.devtune.domain.model.CapabilityState
import com.devtune.domain.model.PlatformCapability
import com.devtune.domain.model.SystemState
import kotlinx.coroutines.flow.Flow

interface PlatformRepository {
    fun getCapabilityState(capability: PlatformCapability): CapabilityState
    fun getSystemState(): Flow<SystemState>
    fun openDeveloperOptions(): Boolean
}
