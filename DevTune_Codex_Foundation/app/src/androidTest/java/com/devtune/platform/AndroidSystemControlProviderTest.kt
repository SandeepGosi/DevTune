package com.devtune.platform

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.devtune.domain.model.PlatformCapability
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AndroidSystemControlProviderTest {

    private lateinit var provider: AndroidSystemControlProvider

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        provider = AndroidSystemControlProvider(context)
    }

    @Test
    fun getDisplayDensityDpi_returnsPositiveValue() {
        val dpi = provider.getDisplayDensityDpi()
        assertTrue("DPI should be positive", dpi > 0)
    }

    @Test
    fun getCapabilityState_readDeveloperOptions_isAvailable() {
        val state = provider.getCapabilityState(PlatformCapability.READ_DEVELOPER_OPTIONS)
        assertNotNull(state)
    }
}
