package com.smartboard.data.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "pinned_snippets",
    indices = [Index(value = ["sort_order"])],
)
data class PinnedSnippetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "body") val body: String,
    @ColumnInfo(name = "sort_order") val sortOrder: Int,
    @ColumnInfo(name = "created_at") val createdAtEpochMs: Long,
    @ColumnInfo(name = "updated_at") val updatedAtEpochMs: Long,
)
