package com.smartboard.data.di

import android.content.Context
import androidx.room.Room
import com.smartboard.data.db.SmartBoardDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): SmartBoardDatabase =
        Room.databaseBuilder(
            context,
            SmartBoardDatabase::class.java,
            "smartboard_database.db",
        ).fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideClipboardDao(db: SmartBoardDatabase) = db.clipboardDao()

    @Provides
    fun providePinnedDao(db: SmartBoardDatabase) = db.pinnedSnippetDao()
}
