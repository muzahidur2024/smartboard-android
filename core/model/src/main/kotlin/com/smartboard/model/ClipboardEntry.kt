package com.smartboard.model

enum class ClipboardCategory {
    LINK,
    EMAIL,
    PHONE,
    PLAIN,
    OTHER,
}

data class ClipboardEntry(
    val id: Long,
    val contentText: String,
    val contentHash: String,
    val category: ClipboardCategory,
    val isPinned: Boolean,
    val isFavorite: Boolean,
    val createdAtEpochMs: Long,
    val lastUsedAtEpochMs: Long,
    val usageCount: Int,
)
