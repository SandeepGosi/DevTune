package com.devtune.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.devtune.ui.screens.home.HomeRoute

@Composable
fun DevTuneNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = DevTuneDestination.Home.route,
        modifier = modifier,
    ) {
        composable(DevTuneDestination.Home.route) {
            HomeRoute()
        }
    }
}
