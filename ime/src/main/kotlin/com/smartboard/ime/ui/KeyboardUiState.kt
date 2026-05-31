package com.smartboard.ime.ui

import com.smartboard.ime.layouts.KeyboardLayout
import com.smartboard.model.ClipboardCategory
import com.smartboard.model.ClipboardEntry
import com.smartboard.model.KeyboardSettings
import com.smartboard.model.PinnedSnippet

enum class ImePanel {
    None,
    Clipboard,
    Emoji,
    Gif,
}

data class KeyboardUiState(
    val settings: KeyboardSettings? = null,
    val layout: KeyboardLayout,
    val shiftPressed: Boolean = false,
    val capsLock: Boolean = false,
    val symbolsMode: Boolean = false,
    val activePanel: ImePanel = ImePanel.None,
    val clipboardSearch: String = "",
    val clipboardCategoryFilter: ClipboardCategory? = null,
    val pins: List<PinnedSnippet> = emptyList(),
    val clipboardItems: List<ClipboardEntry> = emptyList(),
    val suggestions: List<String> = emptyList(),
    val composerBuffer: String = "",
    val lastPinnedSelectionId: Long? = null,
    val showLanguagePicker: Boolean = false,
    val languageSwitchForward: Boolean = true,
)
