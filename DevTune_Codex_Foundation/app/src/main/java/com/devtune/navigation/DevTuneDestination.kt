package com.devtune.navigation

import kotlinx.serialization.Serializable

sealed interface DevTuneDestination {
    @Serializable
    data object Onboarding : DevTuneDestination

    @Serializable
    data object Home : DevTuneDestination

    @Serializable
    data object Settings : DevTuneDestination
}
