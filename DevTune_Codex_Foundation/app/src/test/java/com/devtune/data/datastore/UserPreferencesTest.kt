package com.devtune.data.datastore

import com.devtune.domain.model.ThemeMode
import org.junit.Assert.assertEquals
import org.junit.Test

class UserPreferencesTest {
    @Test
    fun `default preference model represents system theme and dynamic color`() {
        val preferences = UserPreferences(
            themeMode = ThemeMode.SYSTEM.name,
            useDynamicColor = true,
            isOnboardingCompleted = false,
            isAnimationWriteVerified = false
        )

        assertEquals(ThemeMode.SYSTEM.name, preferences.themeMode)
        assertEquals(true, preferences.useDynamicColor)
        assertEquals(false, preferences.isOnboardingCompleted)
    }
}
