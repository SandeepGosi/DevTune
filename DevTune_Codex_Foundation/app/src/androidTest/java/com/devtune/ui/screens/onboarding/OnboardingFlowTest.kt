package com.devtune.ui.screens.onboarding

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.devtune.MainActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class OnboardingFlowTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun onboardingFlow_completesAndNavigatesToHome() {
        // Step 1
        composeTestRule.onNodeWithText("Welcome to DevTune").assertIsDisplayed()
        composeTestRule.onNodeWithText("Next").performClick()

        // Step 2
        composeTestRule.onNodeWithText("Safe & Transparent").assertIsDisplayed()
        composeTestRule.onNodeWithText("Next").performClick()

        // Step 3
        composeTestRule.onNodeWithText("Privacy First").assertIsDisplayed()
        composeTestRule.onNodeWithText("Get Started").performClick()

        // Home
        composeTestRule.onNodeWithText("DevTune").assertIsDisplayed()
    }
}
