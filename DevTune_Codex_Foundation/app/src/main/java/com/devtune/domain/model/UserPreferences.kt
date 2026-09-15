package com.devtune.domain.model

data class UserPreferences(
    val themeMode: ThemeMode,
    val useDynamicColor: Boolean,
    val isOnboardingCompleted: Boolean,
)
