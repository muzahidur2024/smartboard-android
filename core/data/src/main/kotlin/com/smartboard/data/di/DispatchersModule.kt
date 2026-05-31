package com.smartboard.data.di

import com.smartboard.common.SmartBoardDispatchers
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DispatchersModule {
    @Binds
    @Singleton
    abstract fun bindDispatchers(impl: DefaultSmartBoardDispatchers): SmartBoardDispatchers
}
