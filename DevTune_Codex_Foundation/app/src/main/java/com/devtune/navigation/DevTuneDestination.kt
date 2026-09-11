package com.devtune.navigation

sealed interface DevTuneDestination {
    val route: String

    data object Home : DevTuneDestination {
        override val route = "home"
    }
}
