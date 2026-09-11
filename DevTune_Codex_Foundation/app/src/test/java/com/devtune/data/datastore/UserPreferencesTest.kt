package com.devtune.data.datastore

import org.junit.Assert.assertEquals
import org.junit.Test

class UserPreferencesTest {
    @Test
    fun `default preference model represents system theme and dynamic color`() {
        val preferences = UserPreferences(themeMode = "SYSTEM", useDynamicColor = true)

        assertEquals("SYSTEM", preferences.themeMode)
        assertEquals(true, preferences.useDynamicColor)
    }
}
