package com.devtune.di

import com.devtune.data.repository.PlatformRepositoryImpl
import com.devtune.domain.repository.PlatformRepository
import com.devtune.platform.AndroidSystemControlProvider
import com.devtune.platform.SystemControlProvider
import com.devtune.data.repository.ShizukuRepositoryImpl
import com.devtune.domain.repository.ShizukuRepository
import com.devtune.platform.shizuku.AndroidShizukuProvider
import com.devtune.platform.shizuku.PrivilegedSystemBridge
import com.devtune.platform.shizuku.ShizukuPrivilegedBridge
import com.devtune.platform.shizuku.ShizukuProvider
import com.devtune.platform.TimeProvider
import com.devtune.platform.SystemTimeProvider
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

    @Binds
    @Singleton
    abstract fun bindShizukuProvider(
        impl: AndroidShizukuProvider
    ): ShizukuProvider

    @Binds
    @Singleton
    abstract fun bindShizukuRepository(
        impl: ShizukuRepositoryImpl
    ): ShizukuRepository

    @Binds
    @Singleton
    abstract fun bindDisplayDensityProvider(
        impl: com.devtune.platform.AndroidDisplayDensityProvider
    ): com.devtune.platform.DisplayDensityProvider

    @Binds
    @Singleton
    abstract fun bindPrivilegedSystemBridge(
        impl: ShizukuPrivilegedBridge
    ): PrivilegedSystemBridge

    @Binds
    @Singleton
    abstract fun bindTimeProvider(
        impl: SystemTimeProvider
    ): TimeProvider
}
