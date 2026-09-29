package com.devtune.platform

import javax.inject.Inject
import javax.inject.Singleton

interface TimeProvider {
    fun currentTimeMillis(): Long
}

@Singleton
class SystemTimeProvider @Inject constructor() : TimeProvider {
    override fun currentTimeMillis(): Long = System.currentTimeMillis()
}
