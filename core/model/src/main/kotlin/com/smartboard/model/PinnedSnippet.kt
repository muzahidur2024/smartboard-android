package com.smartboard.model

data class PinnedSnippet(
    val id: Long,
    val title: String,
    val body: String,
    val sortOrder: Int,
    val createdAtEpochMs: Long,
    val updatedAtEpochMs: Long,
)
