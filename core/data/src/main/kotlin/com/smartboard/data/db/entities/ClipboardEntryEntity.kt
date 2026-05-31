package com.smartboard.data.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "clipboard_entries",
    indices = [
        Index(value = ["created_at"]),
        Index(value = ["category"]),
    ],
)
data class ClipboardEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "content_text") val contentText: String,
    @ColumnInfo(name = "content_hash") val contentHash: String,
    @ColumnInfo(name = "category") val category: String,
    @ColumnInfo(name = "is_pinned") val isPinned: Boolean,
    @ColumnInfo(name = "is_favorite") val isFavorite: Boolean,
    @ColumnInfo(name = "created_at") val createdAtEpochMs: Long,
    @ColumnInfo(name = "last_used_at") val lastUsedAtEpochMs: Long,
    @ColumnInfo(name = "usage_count") val usageCount: Int,
)
