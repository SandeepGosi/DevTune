package com.devtune.domain.repository

import com.devtune.domain.model.UserPreferences
import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    val userPreferences: Flow<UserPreferences>
    suspend fun setOnboardingCompleted(completed: Boolean)
    suspend fun setThemeMode(themeMode: String)
    suspend fun recordAnimationWriteVerification(backend: String, androidVersion: Int, success: Boolean)
}
