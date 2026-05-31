package com.smartboard.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.smartboard.data.db.entities.ClipboardEntryEntity
import com.smartboard.data.db.entities.ClipboardFtsEntity
import com.smartboard.data.db.entities.PinnedSnippetEntity

@Database(
    entities = [
        ClipboardEntryEntity::class,
        ClipboardFtsEntity::class,
        PinnedSnippetEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class SmartBoardDatabase : RoomDatabase() {
    abstract fun clipboardDao(): ClipboardDao
    abstract fun pinnedSnippetDao(): PinnedSnippetDao
}
