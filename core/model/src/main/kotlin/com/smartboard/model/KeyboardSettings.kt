package com.smartboard.model

enum class ClipboardReadMode {
    ON_OPEN,
    BACKGROUND,
}

enum class ThemeMode {
    LIGHT,
    DARK,
    AMOLED,
    SYSTEM,
}

enum class ThemeAccent {
    DEFAULT,
    BLUE,
    PURPLE,
    GREEN,
}

enum class OneHandMode {
    OFF,
    LEFT,
    RIGHT,
}

data class KeyboardSettings(
    val clipboardEnabled: Boolean,
    val clipboardMaxEntries: Int,
    val clipboardReadMode: ClipboardReadMode,
    val pinnedBarVisible: Boolean,
    val themeMode: ThemeMode,
    val themeAccent: ThemeAccent,
    val keyboardHeightScale: Float,
    val numberRowEnabled: Boolean,
    val hapticEnabled: Boolean,
    val soundEnabled: Boolean,
    val oneHandMode: OneHandMode,
    /** Legacy single-locale id kept in sync with [currentLocale]. */
    val activeLanguage: String,
    val activeLanguages: List<String>,
    val currentLanguageIndex: Int,
    val languageInputPrefsJson: String,
    val networkGifEnabled: Boolean,
    val onboardingComplete: Boolean,
    val autoCategorize: Boolean,
    val keyBorderOutlined: Boolean,
    val wordSuggestionsEnabled: Boolean,
    val autocorrectEnabled: Boolean,
    val oneHandOffsetFraction: Float,
    val customKeyboardColorArgb: Int?,
    val recentEmojisJson: String,
    val favoriteEmojisJson: String,
    val hapticIntensity: Float,
    val soundVolume: Float,
) {
    fun currentLocale(): String {
        if (activeLanguages.isEmpty()) return activeLanguage.ifBlank { "en_US" }
        val idx = currentLanguageIndex.coerceIn(0, activeLanguages.lastIndex)
        return activeLanguages[idx]
    }
}
