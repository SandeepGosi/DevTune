package com.devtune.data.repository

import com.devtune.domain.model.ShizukuState
import com.devtune.domain.repository.ShizukuRepository
import com.devtune.platform.shizuku.ShizukuProvider
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShizukuRepositoryImpl @Inject constructor(
    private val provider: ShizukuProvider
) : ShizukuRepository {
    override val state: Flow<ShizukuState> = provider.state
    
    override suspend fun requestPermission() {
        provider.requestPermission()
    }
}
