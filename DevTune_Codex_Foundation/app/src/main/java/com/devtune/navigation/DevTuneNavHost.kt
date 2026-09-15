package com.devtune.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.devtune.ui.screens.home.HomeRoute
import com.devtune.ui.screens.onboarding.OnboardingRoute
import com.devtune.ui.screens.settings.SettingsRoute

@Composable
fun DevTuneNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = DevTuneDestination.Onboarding,
        modifier = modifier,
    ) {
        composable<DevTuneDestination.Onboarding> {
            OnboardingRoute(
                onOnboardingComplete = {
                    navController.navigate(DevTuneDestination.Home) {
                        popUpTo(DevTuneDestination.Onboarding) { inclusive = true }
                    }
                }
            )
        }
        composable<DevTuneDestination.Home> {
            HomeRoute(
                onNavigateToSettings = {
                    navController.navigate(DevTuneDestination.Settings)
                }
            )
        }
        composable<DevTuneDestination.Settings> {
            SettingsRoute(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
