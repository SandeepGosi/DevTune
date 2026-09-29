package com.devtune.domain.model

/**
 * Strongly typed domain/application errors for privileged animation operations.
 */
sealed class AnimationError : Exception() {
    object ShizukuUnavailable : AnimationError() {
        override val message: String = "Shizuku service is not running or unavailable."
    }
    object ShizukuPermissionRequired : AnimationError() {
        override val message: String = "Shizuku authorization is required for advanced controls."
    }
    object BinderUnavailable : AnimationError() {
        override val message: String = "WindowManager Binder service could not be acquired."
    }
    object ReadFailed : AnimationError() {
        override val message: String = "Failed to read system animation scales."
    }
    data class InvalidScaleValue(val value: Float, val reason: String) : AnimationError() {
        override val message: String = "Invalid animation scale value ($value): $reason"
    }
    data class WriteFailed(val which: Int, override val cause: Throwable?) : AnimationError() {
        override val message: String = "Failed to write animation scale $which: ${cause?.message ?: "Unknown error"}"
    }
    data class ReadBackFailed(val expected: Float, val actual: Float) : AnimationError() {
        override val message: String = "Read-back verification failed. Expected: $expected, actual: $actual"
    }
    data class RestorationFailed(override val cause: Throwable?) : AnimationError() {
        override val message: String = "Failed to restore original animation scales: ${cause?.message ?: "Unknown error"}"
    }
}
