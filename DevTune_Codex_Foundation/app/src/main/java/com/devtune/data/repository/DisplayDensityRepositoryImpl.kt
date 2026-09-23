package com.devtune.data.repository

import com.devtune.domain.model.DisplayDensityInfo
import com.devtune.domain.repository.DisplayDensityRepository
import com.devtune.platform.DisplayDensityProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DisplayDensityRepositoryImpl @Inject constructor(
    private val displayDensityProvider: DisplayDensityProvider
) : DisplayDensityRepository {

    override fun getDisplayDensityInfo(): Flow<DisplayDensityInfo> = flow {
        while (true) {
            emit(displayDensityProvider.getDisplayDensityInfo())
            delay(3000) // Poll every 3 seconds to observe configuration changes
        }
    }
}
