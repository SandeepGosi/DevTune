package com.devtune.platform.shizuku

import com.devtune.domain.model.ShizukuState
import kotlinx.coroutines.flow.Flow

interface ShizukuProvider {
    val state: Flow<ShizukuState>
    fun requestPermission()
}
