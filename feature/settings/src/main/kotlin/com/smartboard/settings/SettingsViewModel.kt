package com.smartboard.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartboard.domain.backup.ExportPinsUseCase
import com.smartboard.domain.settings.ObserveSettingsUseCase
import com.smartboard.domain.settings.UpdateSettingUseCase
import com.smartboard.ime.layouts.json.LanguageMeta
import com.smartboard.ime.layouts.json.LanguagePackLoader
import com.smartboard.model.KeyboardSettings
import com.smartboard.model.ThemeAccent
import com.smartboard.model.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    observeSettings: ObserveSettingsUseCase,
    private val updateSetting: UpdateSettingUseCase,
    val exportPins: ExportPinsUseCase,
    private val languagePackLoader: LanguagePackLoader,
) : ViewModel() {

    val settings: StateFlow<KeyboardSettings?> = observeSettings()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _languageCatalog = MutableStateFlow<List<LanguageMeta>>(emptyList())
    val languageCatalog: StateFlow<List<LanguageMeta>> = _languageCatalog.asStateFlow()

    init {
        viewModelScope.launch {
            _languageCatalog.value = languagePackLoader.loadAllMeta()
        }
    }

    fun setClipboardEnabled(v: Boolean) = update { it.copy(clipboardEnabled = v) }
    fun setPinnedBarVisible(v: Boolean) = update { it.copy(pinnedBarVisible = v) }
    fun setGifNetwork(v: Boolean) = update { it.copy(networkGifEnabled = v) }
    fun setNumberRow(v: Boolean) = update { it.copy(numberRowEnabled = v) }
    fun setHaptics(v: Boolean) = update { it.copy(hapticEnabled = v) }
    fun setSound(v: Boolean) = update { it.copy(soundEnabled = v) }
    fun setWordSuggestions(v: Boolean) = update { it.copy(wordSuggestionsEnabled = v) }
    fun setAutocorrect(v: Boolean) = update { it.copy(autocorrectEnabled = v) }
    fun setAutoCategorize(v: Boolean) = update { it.copy(autoCategorize = v) }
    fun setKeyBorderOutlined(v: Boolean) = update { it.copy(keyBorderOutlined = v) }
    fun setThemeMode(mode: ThemeMode) = update { it.copy(themeMode = mode) }
    fun setThemeAccent(accent: ThemeAccent) = update { it.copy(themeAccent = accent) }
    fun setKeyboardHeight(scale: Float) =
        update { it.copy(keyboardHeightScale = scale.coerceIn(0.7f, 1.4f)) }

    fun moveActiveLanguageUp(index: Int) {
        update { s ->
            if (index <= 0 || index !in s.activeLanguages.indices) return@update s
            val list = s.activeLanguages.toMutableList()
            val t = list[index - 1]
            list[index - 1] = list[index]
            list[index] = t
            val newCur = when (s.currentLanguageIndex) {
                index -> index - 1
                index - 1 -> index
                else -> s.currentLanguageIndex
            }
            s.copy(activeLanguages = list, currentLanguageIndex = newCur)
        }
    }

    fun moveActiveLanguageDown(index: Int) {
        update { s ->
            if (index !in s.activeLanguages.indices || index >= s.activeLanguages.lastIndex) return@update s
            val list = s.activeLanguages.toMutableList()
            val t = list[index + 1]
            list[index + 1] = list[index]
            list[index] = t
            val newCur = when (s.currentLanguageIndex) {
                index -> index + 1
                index + 1 -> index
                else -> s.currentLanguageIndex
            }
            s.copy(activeLanguages = list, currentLanguageIndex = newCur)
        }
    }

    fun removeActiveLanguage(locale: String) {
        update { s ->
            val list = s.activeLanguages.toMutableList()
            val rm = list.indexOf(locale)
            if (rm < 0) return@update s
            list.removeAt(rm)
            if (list.isEmpty()) {
                return@update s.copy(
                    activeLanguages = listOf("en_US"),
                    currentLanguageIndex = 0,
                    activeLanguage = "en_US",
                )
            }
            val newIdx = when {
                s.currentLanguageIndex == rm -> rm.coerceAtMost(list.lastIndex)
                s.currentLanguageIndex > rm -> s.currentLanguageIndex - 1
                else -> s.currentLanguageIndex
            }.coerceIn(0, list.lastIndex)
            s.copy(activeLanguages = list, currentLanguageIndex = newIdx, activeLanguage = list[newIdx])
        }
    }

    fun addActiveLanguage(locale: String) {
        update { s ->
            if (s.activeLanguages.contains(locale)) return@update s
            val list = s.activeLanguages + locale
            s.copy(activeLanguages = list, currentLanguageIndex = list.lastIndex, activeLanguage = locale)
        }
    }

    private fun update(block: (KeyboardSettings) -> KeyboardSettings) {
        viewModelScope.launch { updateSetting(block) }
    }
}
