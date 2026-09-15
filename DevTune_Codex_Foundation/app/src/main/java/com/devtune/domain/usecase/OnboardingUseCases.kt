package com.devtune.domain.usecase

import com.devtune.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetOnboardingStatusUseCase @Inject constructor(
    private val repository: UserPreferencesRepository
) {
    operator fun invoke(): Flow<Boolean> = repository.userPreferences.map { it.isOnboardingCompleted }
}

class CompleteOnboardingUseCase @Inject constructor(
    private val repository: UserPreferencesRepository
) {
    suspend operator fun invoke() {
        repository.setOnboardingCompleted(true)
    }
}
