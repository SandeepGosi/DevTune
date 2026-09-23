package com.devtune.domain.usecase

import com.devtune.domain.model.DisplayDensityInfo
import com.devtune.domain.repository.DisplayDensityRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetDisplayDensityInfoUseCase @Inject constructor(
    private val repository: DisplayDensityRepository
) {
    operator fun invoke(): Flow<DisplayDensityInfo> {
        return repository.getDisplayDensityInfo()
    }
}
