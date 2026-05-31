package com.smartboard.data.di

import com.smartboard.common.TimeProvider
import com.smartboard.data.repository.ClipboardRepositoryImpl
import com.smartboard.data.repository.PinsRepositoryImpl
import com.smartboard.data.repository.SettingsRepositoryImpl
import com.smartboard.data.time.SystemTimeProvider
import com.smartboard.domain.repository.ClipboardRepository
import com.smartboard.domain.repository.PinsRepository
import com.smartboard.domain.repository.SettingsRepository
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
    abstract fun bindClipboard(r: ClipboardRepositoryImpl): ClipboardRepository

    @Binds
    @Singleton
    abstract fun bindPins(r: PinsRepositoryImpl): PinsRepository

    @Binds
    @Singleton
    abstract fun bindSettings(r: SettingsRepositoryImpl): SettingsRepository

    @Binds
    @Singleton
    abstract fun bindTime(p: SystemTimeProvider): TimeProvider
}
