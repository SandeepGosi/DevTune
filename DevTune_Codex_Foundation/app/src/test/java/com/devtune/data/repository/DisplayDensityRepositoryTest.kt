package com.devtune.data.repository

import com.devtune.domain.model.DisplayDensityInfo
import com.devtune.platform.DisplayDensityProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class DisplayDensityRepositoryTest {

    @Test
    fun `getDisplayDensityInfo emits metrics from provider`() = runBlocking {
        val fakeInfo = DisplayDensityInfo(
            currentDensityDpi = 420,
            widthPx = 1080,
            heightPx = 2424,
            configurationDensityDpi = 420,
            displayMetricsDensityDpi = 420,
            smallestWidthDpFromConfig = 411
        )

        val provider = object : DisplayDensityProvider {
            override fun getDisplayDensityInfo(): DisplayDensityInfo = fakeInfo
        }

        val repository = DisplayDensityRepositoryImpl(provider)
        val emitted = repository.getDisplayDensityInfo().first()

        assertEquals(420, emitted.currentDensityDpi)
        assertEquals(1080, emitted.widthPx)
        assertEquals(2424, emitted.heightPx)
        assertEquals(411, emitted.smallestWidthDp)
    }
}
