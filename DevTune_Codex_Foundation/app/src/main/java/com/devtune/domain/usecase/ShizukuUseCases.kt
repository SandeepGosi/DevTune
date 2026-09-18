package com.devtune.domain.usecase

import com.devtune.domain.model.ShizukuState
import com.devtune.domain.repository.ShizukuRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetShizukuStateUseCase @Inject constructor(
    private val repository: ShizukuRepository
) {
    operator fun invoke(): Flow<ShizukuState> = repository.state
}

class RequestShizukuPermissionUseCase @Inject constructor(
    private val repository: ShizukuRepository
) {
    suspend operator fun invoke() {
        repository.requestPermission()
    }
}
