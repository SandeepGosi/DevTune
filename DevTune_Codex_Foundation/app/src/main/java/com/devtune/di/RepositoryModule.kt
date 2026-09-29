package com.devtune.di

import com.devtune.data.repository.AnimationRepositoryImpl
import com.devtune.data.repository.TemporarySessionRepositoryImpl
import com.devtune.data.repository.UserPreferencesRepositoryImpl
import com.devtune.domain.repository.AnimationRepository
import com.devtune.domain.repository.TemporarySessionRepository
import com.devtune.domain.repository.UserPreferencesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindUserPreferencesRepository(
        impl: UserPreferencesRepositoryImpl
    ): UserPreferencesRepository

    @Binds
    @Singleton
    abstract fun bindAnimationRepository(
        impl: AnimationRepositoryImpl
    ): AnimationRepository

    @Binds
    @Singleton
    abstract fun bindDisplayDensityRepository(
        impl: com.devtune.data.repository.DisplayDensityRepositoryImpl
    ): com.devtune.domain.repository.DisplayDensityRepository

    @Binds
    @Singleton
    abstract fun bindTemporarySessionRepository(
        impl: TemporarySessionRepositoryImpl
    ): TemporarySessionRepository
}
