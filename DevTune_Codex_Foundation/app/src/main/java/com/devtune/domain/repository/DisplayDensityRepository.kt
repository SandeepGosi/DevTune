package com.devtune.domain.repository

import com.devtune.domain.model.DisplayDensityInfo
import kotlinx.coroutines.flow.Flow

/**
 * Domain boundary for inspecting read-only display density metrics.
 */
interface DisplayDensityRepository {
    fun getDisplayDensityInfo(): Flow<DisplayDensityInfo>
}
