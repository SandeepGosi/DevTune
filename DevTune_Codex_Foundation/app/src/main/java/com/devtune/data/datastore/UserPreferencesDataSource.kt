package com.devtune.data.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.core.edit
import com.devtune.domain.model.ThemeMode
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "devtune_preferences")

@Singleton
class UserPreferencesDataSource @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    val preferences: Flow<UserPreferences> = context.dataStore.data.map { values ->
        UserPreferences(
            themeMode = values[ThemeModeKey] ?: ThemeMode.SYSTEM.name,
            useDynamicColor = values[DynamicColorKey] ?: true,
            isOnboardingCompleted = values[OnboardingCompletedKey] ?: false,
            lastVerifiedBackend = values[LastVerifiedBackendKey] ?: "NONE",
            lastVerifiedAndroidVersion = values[LastVerifiedAndroidVersionKey] ?: 0,
            lastVerifiedAt = values[LastVerifiedAtKey] ?: 0L,
            lastVerifiedSuccess = values[LastVerifiedSuccessKey] ?: false
        )
    }

    suspend fun setThemeMode(themeMode: String) {
        context.dataStore.edit { it[ThemeModeKey] = themeMode }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { it[OnboardingCompletedKey] = completed }
    }

    suspend fun recordAnimationWriteVerification(
        backend: String,
        androidVersion: Int,
        success: Boolean
    ) {
        context.dataStore.edit {
            it[LastVerifiedBackendKey] = backend
            it[LastVerifiedAndroidVersionKey] = androidVersion
            it[LastVerifiedAtKey] = System.currentTimeMillis()
            it[LastVerifiedSuccessKey] = success
        }
    }

    companion object {
        private val ThemeModeKey = stringPreferencesKey("theme_mode")
        private val DynamicColorKey = booleanPreferencesKey("dynamic_color")
        private val OnboardingCompletedKey = booleanPreferencesKey("onboarding_completed")
        private val LastVerifiedBackendKey = stringPreferencesKey("last_verified_backend")
        private val LastVerifiedAndroidVersionKey = androidx.datastore.preferences.core.intPreferencesKey("last_verified_android_version")
        private val LastVerifiedAtKey = androidx.datastore.preferences.core.longPreferencesKey("last_verified_at")
        private val LastVerifiedSuccessKey = booleanPreferencesKey("last_verified_success")
    }
}

data class UserPreferences(
    val themeMode: String,
    val useDynamicColor: Boolean,
    val isOnboardingCompleted: Boolean,
    val lastVerifiedBackend: String,
    val lastVerifiedAndroidVersion: Int,
    val lastVerifiedAt: Long,
    val lastVerifiedSuccess: Boolean
)
