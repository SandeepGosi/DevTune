package com.devtune.di

import com.devtune.domain.repository.AnimationRepository
import com.devtune.domain.repository.ShizukuRepository
import com.devtune.domain.repository.TemporarySessionRepository
import com.devtune.domain.usecase.ToggleQuickAnimationProfileUseCase
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface QuickSettingsEntryPoint {
    fun animationRepository(): AnimationRepository
    fun temporarySessionRepository(): TemporarySessionRepository
    fun shizukuRepository(): ShizukuRepository
    fun toggleQuickAnimationProfileUseCase(): ToggleQuickAnimationProfileUseCase
}
