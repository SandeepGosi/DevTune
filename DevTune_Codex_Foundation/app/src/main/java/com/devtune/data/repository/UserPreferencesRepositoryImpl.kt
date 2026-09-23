package com.devtune.data.repository

import com.devtune.data.datastore.UserPreferencesDataSource
import com.devtune.domain.model.ThemeMode
import com.devtune.domain.model.UserPreferences
import com.devtune.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPreferencesRepositoryImpl @Inject constructor(
    private val dataSource: UserPreferencesDataSource
) : UserPreferencesRepository {

    override val userPreferences: Flow<UserPreferences> = dataSource.preferences.map { data ->
        UserPreferences(
            themeMode = try {
                ThemeMode.valueOf(data.themeMode)
            } catch (e: IllegalArgumentException) {
                ThemeMode.SYSTEM
            },
            useDynamicColor = data.useDynamicColor,
            isOnboardingCompleted = data.isOnboardingCompleted,
            lastVerifiedBackend = data.lastVerifiedBackend,
            lastVerifiedAndroidVersion = data.lastVerifiedAndroidVersion,
            lastVerifiedAt = data.lastVerifiedAt,
            lastVerifiedSuccess = data.lastVerifiedSuccess
        )
    }

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        dataSource.setOnboardingCompleted(completed)
    }

    override suspend fun setThemeMode(themeMode: String) {
        dataSource.setThemeMode(themeMode)
    }

    override suspend fun recordAnimationWriteVerification(
        backend: String,
        androidVersion: Int,
        success: Boolean
    ) {
        dataSource.recordAnimationWriteVerification(backend, androidVersion, success)
    }
}
