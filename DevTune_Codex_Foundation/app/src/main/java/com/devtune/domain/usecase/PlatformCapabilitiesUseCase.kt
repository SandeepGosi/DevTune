package com.devtune.domain.usecase

import com.devtune.domain.model.CapabilityState
import com.devtune.domain.model.PlatformCapability
import com.devtune.domain.model.SystemState
import com.devtune.domain.repository.PlatformRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCapabilityStateUseCase @Inject constructor(
    private val repository: PlatformRepository
) {
    operator fun invoke(capability: PlatformCapability): CapabilityState {
        return repository.getCapabilityState(capability)
    }
}

class GetSystemStateUseCase @Inject constructor(
    private val repository: PlatformRepository
) {
    operator fun invoke(): Flow<SystemState> = repository.getSystemState()
}

class OpenDeveloperOptionsUseCase @Inject constructor(
    private val repository: PlatformRepository
) {
    operator fun invoke(): Boolean = repository.openDeveloperOptions()
}
