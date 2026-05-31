package com.smartboard.domain.backup

import com.smartboard.model.KeyboardSettings
import com.smartboard.model.PinnedSnippet
import kotlinx.serialization.Serializable

@Serializable
data class PinnedSnippetExport(
    val title: String,
    val body: String,
    val sortOrder: Int,
    val createdAtEpochMs: Long,
    val updatedAtEpochMs: Long,
)

@Serializable
data class SettingsExport(
    val themeMode: String,
    val keyboardHeightScale: Float,
    val numberRowEnabled: Boolean,
)

@Serializable
data class SmartBoardBackup(
    val version: Int = 1,
    val exportedAtEpochMs: Long,
    val pinnedSnippets: List<PinnedSnippetExport>,
    val settings: SettingsExport? = null,
)

fun PinnedSnippet.toExport(): PinnedSnippetExport = PinnedSnippetExport(
    title = title,
    body = body,
    sortOrder = sortOrder,
    createdAtEpochMs = createdAtEpochMs,
    updatedAtEpochMs = updatedAtEpochMs,
)

fun KeyboardSettings.toExport(): SettingsExport = SettingsExport(
    themeMode = themeMode.name,
    keyboardHeightScale = keyboardHeightScale,
    numberRowEnabled = numberRowEnabled,
)
