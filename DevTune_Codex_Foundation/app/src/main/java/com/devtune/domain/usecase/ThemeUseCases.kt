package com.devtune.domain.usecase

import com.devtune.domain.model.ThemeMode
import com.devtune.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetThemeModeUseCase @Inject constructor(
    private val repository: UserPreferencesRepository
) {
    operator fun invoke(): Flow<ThemeMode> = repository.userPreferences.map { it.themeMode }
}

class SetThemeModeUseCase @Inject constructor(
    private val repository: UserPreferencesRepository
) {
    suspend operator fun invoke(themeMode: ThemeMode) {
        repository.setThemeMode(themeMode.name)
    }
}
