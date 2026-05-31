package com.smartboard.data.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.FtsOptions
import androidx.room.PrimaryKey

@Entity(tableName = "clipboard_fts")
@Fts4(
    contentEntity = ClipboardEntryEntity::class,
    tokenizer = FtsOptions.TOKENIZER_UNICODE61,
)
data class ClipboardFtsEntity(
    @PrimaryKey @ColumnInfo(name = "rowid") val rowid: Long,
    @ColumnInfo(name = "content_text") val contentText: String,
)
