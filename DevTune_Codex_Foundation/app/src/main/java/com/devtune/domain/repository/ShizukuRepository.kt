package com.devtune.domain.repository

import com.devtune.domain.model.ShizukuState
import kotlinx.coroutines.flow.Flow

interface ShizukuRepository {
    val state: Flow<ShizukuState>
    suspend fun requestPermission()
}
