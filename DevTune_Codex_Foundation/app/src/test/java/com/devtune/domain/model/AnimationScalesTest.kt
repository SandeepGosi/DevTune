package com.devtune.domain.model

import org.junit.Assert.*
import org.junit.Test

class AnimationScalesTest {

    @Test
    fun `valid animation scales initialize properly`() {
        val scales = AnimationScales(0.5f, 1.0f, 1.5f)
        assertEquals(0.5f, scales.windowScale)
        assertEquals(1.0f, scales.transitionScale)
        assertEquals(1.5f, scales.animatorScale)
    }

    @Test(expected = AnimationError.InvalidScaleValue::class)
    fun `negative scale throws InvalidScaleValue`() {
        AnimationScales(-0.1f, 1.0f, 1.0f)
    }

    @Test(expected = AnimationError.InvalidScaleValue::class)
    fun `excessive scale throws InvalidScaleValue`() {
        AnimationScales(1.0f, 10.5f, 1.0f)
    }

    @Test(expected = AnimationError.InvalidScaleValue::class)
    fun `NaN scale throws InvalidScaleValue`() {
        AnimationScales(Float.NaN, 1.0f, 1.0f)
    }

    @Test(expected = AnimationError.InvalidScaleValue::class)
    fun `infinite scale throws InvalidScaleValue`() {
        AnimationScales(1.0f, Float.POSITIVE_INFINITY, 1.0f)
    }
}
