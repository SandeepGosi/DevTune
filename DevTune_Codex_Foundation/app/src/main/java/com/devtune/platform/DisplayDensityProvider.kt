package com.devtune.platform

import com.devtune.domain.model.DisplayDensityInfo

/**
 * Platform provider interface for reading display density metrics using public Android SDK APIs.
 */
interface DisplayDensityProvider {
    fun getDisplayDensityInfo(): DisplayDensityInfo
}
