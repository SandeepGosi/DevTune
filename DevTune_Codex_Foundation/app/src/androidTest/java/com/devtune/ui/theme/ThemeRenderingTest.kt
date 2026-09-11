package com.devtune.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test

class ThemeRenderingTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun theme_rendersContentWithTypography() {
        composeTestRule.setContent {
            DevTuneTheme {
                Text(
                    text = "Theme Test",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        composeTestRule.onNodeWithText("Theme Test").assertExists()
    }
}
