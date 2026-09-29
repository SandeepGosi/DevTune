package com.devtune.domain.model

enum class TemporaryDuration(val durationMinutes: Long, val label: String) {
    FIVE_MINUTES(5L, "5 min"),
    FIFTEEN_MINUTES(15L, "15 min"),
    THIRTY_MINUTES(30L, "30 min"),
    ONE_HOUR(60L, "1 hour"),
    TWO_HOURS(120L, "2 hours"),
    FOUR_HOURS(240L, "4 hours")
}

enum class SessionStatus {
    IDLE,
    APPLYING,
    ACTIVE,
    RESTORING,
    RESTORE_PENDING,
    COMPLETED,
    FAILED_TO_APPLY,
    FAILED_TO_RESTORE,
    CANCELLED
}

data class TemporaryAnimationSession(
    val sessionId: String,
    val startedAtEpochMillis: Long,
    val expiresAtEpochMillis: Long,
    val originalScales: AnimationScales,
    val targetScales: AnimationScales,
    val status: SessionStatus
) {
    fun remainingMillis(currentTimeMillis: Long): Long {
        return (expiresAtEpochMillis - currentTimeMillis).coerceAtLeast(0L)
    }

    fun isExpired(currentTimeMillis: Long): Boolean {
        return currentTimeMillis >= expiresAtEpochMillis
    }
}
