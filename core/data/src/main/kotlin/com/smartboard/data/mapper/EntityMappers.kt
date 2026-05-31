package com.smartboard.data.mapper

import com.smartboard.data.db.entities.ClipboardEntryEntity
import com.smartboard.data.db.entities.PinnedSnippetEntity
import com.smartboard.model.ClipboardCategory
import com.smartboard.model.ClipboardEntry
import com.smartboard.model.PinnedSnippet

fun ClipboardEntryEntity.toDomain(): ClipboardEntry = ClipboardEntry(
    id = id,
    contentText = contentText,
    contentHash = contentHash,
    category = ClipboardCategory.entries.firstOrNull { it.name == category }
        ?: ClipboardCategory.PLAIN,
    isPinned = isPinned,
    isFavorite = isFavorite,
    createdAtEpochMs = createdAtEpochMs,
    lastUsedAtEpochMs = lastUsedAtEpochMs,
    usageCount = usageCount,
)

fun PinnedSnippetEntity.toDomain(): PinnedSnippet = PinnedSnippet(
    id = id,
    title = title,
    body = body,
    sortOrder = sortOrder,
    createdAtEpochMs = createdAtEpochMs,
    updatedAtEpochMs = updatedAtEpochMs,
)
