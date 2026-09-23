package com.devtune.domain.model

data class UserPreferences(
    val themeMode: ThemeMode,
    val useDynamicColor: Boolean,
    val isOnboardingCompleted: Boolean,
    val lastVerifiedBackend: String = "NONE",
    val lastVerifiedAndroidVersion: Int = 0,
    val lastVerifiedAt: Long = 0L,
    val lastVerifiedSuccess: Boolean = false
)
