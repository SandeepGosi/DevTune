package com.devtune.di

import com.devtune.data.repository.PlatformRepositoryImpl
import com.devtune.domain.repository.PlatformRepository
import com.devtune.platform.AndroidSystemControlProvider
import com.devtune.platform.SystemControlProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PlatformModule {

    @Binds
    @Singleton
    abstract fun bindSystemControlProvider(
        impl: AndroidSystemControlProvider
    ): SystemControlProvider

    @Binds
    @Singleton
    abstract fun bindPlatformRepository(
        impl: PlatformRepositoryImpl
    ): PlatformRepository
}
