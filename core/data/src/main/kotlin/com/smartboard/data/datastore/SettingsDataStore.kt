package com.smartboard.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.smartboard.model.ClipboardReadMode
import com.smartboard.model.KeyboardSettings
import com.smartboard.model.OneHandMode
import com.smartboard.model.ThemeAccent
import com.smartboard.model.ThemeMode
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "settings_preferences",
)

@Singleton
class SettingsDataStore @Inject constructor(
    @ApplicationContext context: Context,
    private val json: Json,
) {
    private val dataStore = context.settingsDataStore

    val settingsFlow: Flow<KeyboardSettings> = dataStore.data.map { p -> toSettings(p) }

    suspend fun update(transform: (KeyboardSettings) -> KeyboardSettings) {
        dataStore.edit { prefs ->
            val current = toSettings(prefs)
            val next = transform(current)
            writePrefs(prefs, next)
        }
    }

    private fun toSettings(p: Preferences): KeyboardSettings {
        val legacy = normalizeLegacyLocale(p[Keys.active_language])
        val langs = decodeLangList(p[Keys.active_languages_json]).ifEmpty { listOf(legacy) }
        val idx = p[Keys.current_language_index] ?: 0
        val current = langs.getOrElse(idx.coerceIn(0, langs.lastIndex.coerceAtLeast(0))) { legacy }
        return KeyboardSettings(
            clipboardEnabled = p[Keys.clipboard_enabled] ?: true,
            clipboardMaxEntries = (p[Keys.clipboard_max_entries] ?: 200).coerceIn(1, 200),
            clipboardReadMode = p[Keys.clipboard_read_mode]?.let {
                ClipboardReadMode.entries.firstOrNull { e -> e.name == it }
            } ?: ClipboardReadMode.ON_OPEN,
            pinnedBarVisible = p[Keys.pinned_bar_visible] ?: true,
            themeMode = p[Keys.theme_mode]?.let {
                ThemeMode.entries.firstOrNull { e -> e.name == it }
            } ?: ThemeMode.SYSTEM,
            themeAccent = p[Keys.theme_accent]?.let {
                ThemeAccent.entries.firstOrNull { e -> e.name == it }
            } ?: ThemeAccent.DEFAULT,
            keyboardHeightScale = p[Keys.keyboard_height_scale] ?: 1f,
            numberRowEnabled = p[Keys.number_row_enabled] ?: false,
            hapticEnabled = p[Keys.haptic_enabled] ?: true,
            soundEnabled = p[Keys.sound_enabled] ?: false,
            oneHandMode = p[Keys.one_hand_mode]?.let {
                OneHandMode.entries.firstOrNull { e -> e.name == it }
            } ?: OneHandMode.OFF,
            activeLanguage = current,
            activeLanguages = langs,
            currentLanguageIndex = idx.coerceIn(0, (langs.size - 1).coerceAtLeast(0)),
            languageInputPrefsJson = p[Keys.language_input_prefs_json] ?: "{}",
            networkGifEnabled = p[Keys.network_gif_enabled] ?: true,
            onboardingComplete = p[Keys.onboarding_complete] ?: false,
            autoCategorize = p[Keys.auto_categorize] ?: true,
            keyBorderOutlined = p[Keys.key_border_outlined] ?: false,
            wordSuggestionsEnabled = p[Keys.word_suggestions_enabled] ?: true,
            autocorrectEnabled = p[Keys.autocorrect_enabled] ?: true,
            oneHandOffsetFraction = p[Keys.one_hand_offset_fraction] ?: 0.18f,
            customKeyboardColorArgb = p[Keys.custom_keyboard_color_argb],
            recentEmojisJson = p[Keys.recent_emojis_json] ?: "[]",
            favoriteEmojisJson = p[Keys.favorite_emojis_json] ?: "[]",
            hapticIntensity = p[Keys.haptic_intensity] ?: 1f,
            soundVolume = p[Keys.sound_volume] ?: 0.5f,
        )
    }

    private fun writePrefs(prefs: MutablePreferences, next: KeyboardSettings) {
        prefs[Keys.clipboard_enabled] = next.clipboardEnabled
        prefs[Keys.clipboard_max_entries] = next.clipboardMaxEntries.coerceIn(1, 200)
        prefs[Keys.clipboard_read_mode] = next.clipboardReadMode.name
        prefs[Keys.pinned_bar_visible] = next.pinnedBarVisible
        prefs[Keys.theme_mode] = next.themeMode.name
        prefs[Keys.theme_accent] = next.themeAccent.name
        prefs[Keys.keyboard_height_scale] = next.keyboardHeightScale
        prefs[Keys.number_row_enabled] = next.numberRowEnabled
        prefs[Keys.haptic_enabled] = next.hapticEnabled
        prefs[Keys.sound_enabled] = next.soundEnabled
        prefs[Keys.one_hand_mode] = next.oneHandMode.name
        val synced = next.copy(
            activeLanguage = next.currentLocale(),
            activeLanguages = next.activeLanguages.ifEmpty { listOf(next.currentLocale()) },
            currentLanguageIndex = next.currentLanguageIndex.coerceIn(
                0,
                (next.activeLanguages.size - 1).coerceAtLeast(0),
            ),
        )
        prefs[Keys.active_language] = synced.activeLanguage
        prefs[Keys.active_languages_json] = encodeLangList(synced.activeLanguages)
        prefs[Keys.current_language_index] = synced.currentLanguageIndex
        prefs[Keys.language_input_prefs_json] = synced.languageInputPrefsJson
        prefs[Keys.network_gif_enabled] = next.networkGifEnabled
        prefs[Keys.onboarding_complete] = next.onboardingComplete
        prefs[Keys.auto_categorize] = next.autoCategorize
        prefs[Keys.key_border_outlined] = next.keyBorderOutlined
        prefs[Keys.word_suggestions_enabled] = next.wordSuggestionsEnabled
        prefs[Keys.autocorrect_enabled] = next.autocorrectEnabled
        prefs[Keys.one_hand_offset_fraction] = next.oneHandOffsetFraction
        next.customKeyboardColorArgb?.let { prefs[Keys.custom_keyboard_color_argb] = it }
            ?: prefs.remove(Keys.custom_keyboard_color_argb)
        prefs[Keys.recent_emojis_json] = next.recentEmojisJson
        prefs[Keys.favorite_emojis_json] = next.favoriteEmojisJson
        prefs[Keys.haptic_intensity] = next.hapticIntensity
        prefs[Keys.sound_volume] = next.soundVolume
    }

    private fun decodeLangList(raw: String?): List<String> = if (raw.isNullOrBlank()) {
        emptyList()
    } else {
        runCatching {
            json.decodeFromString(ListSerializer(String.serializer()), raw)
        }.getOrElse { emptyList() }
    }

    private fun encodeLangList(list: List<String>): String =
        json.encodeToString(ListSerializer(String.serializer()), list)

    private fun normalizeLegacyLocale(id: String?): String = when (id) {
        null, "", "en" -> "en_US"
        "bn" -> "bn_BD"
        else -> id
    }

    private object Keys {
        val clipboard_enabled = booleanPreferencesKey("clipboard_enabled")
        val clipboard_max_entries = intPreferencesKey("clipboard_max_entries")
        val clipboard_read_mode = stringPreferencesKey("clipboard_read_mode")
        val pinned_bar_visible = booleanPreferencesKey("pinned_bar_visible")
        val theme_mode = stringPreferencesKey("theme_mode")
        val theme_accent = stringPreferencesKey("theme_accent")
        val keyboard_height_scale = floatPreferencesKey("keyboard_height_scale")
        val number_row_enabled = booleanPreferencesKey("number_row_enabled")
        val haptic_enabled = booleanPreferencesKey("haptic_enabled")
        val sound_enabled = booleanPreferencesKey("sound_enabled")
        val one_hand_mode = stringPreferencesKey("one_hand_mode")
        val active_language = stringPreferencesKey("active_language")
        val active_languages_json = stringPreferencesKey("active_languages_json")
        val current_language_index = intPreferencesKey("current_language_index")
        val language_input_prefs_json = stringPreferencesKey("language_input_prefs_json")
        val network_gif_enabled = booleanPreferencesKey("network_gif_enabled")
        val onboarding_complete = booleanPreferencesKey("onboarding_complete")
        val auto_categorize = booleanPreferencesKey("auto_categorize")
        val key_border_outlined = booleanPreferencesKey("key_border_outlined")
        val word_suggestions_enabled = booleanPreferencesKey("word_suggestions_enabled")
        val autocorrect_enabled = booleanPreferencesKey("autocorrect_enabled")
        val one_hand_offset_fraction = floatPreferencesKey("one_hand_offset_fraction")
        val custom_keyboard_color_argb = intPreferencesKey("custom_keyboard_color_argb")
        val recent_emojis_json = stringPreferencesKey("recent_emojis_json")
        val favorite_emojis_json = stringPreferencesKey("favorite_emojis_json")
        val haptic_intensity = floatPreferencesKey("haptic_intensity")
        val sound_volume = floatPreferencesKey("sound_volume")
    }
}
