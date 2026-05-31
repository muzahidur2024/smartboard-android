package com.smartboard.ime.ui

import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import com.smartboard.domain.clipboard.ClearClipboardHistoryUseCase
import com.smartboard.domain.clipboard.DeleteClipboardEntryUseCase
import com.smartboard.domain.clipboard.ObserveClipboardUseCase
import com.smartboard.domain.clipboard.PinClipboardEntryUseCase
import com.smartboard.domain.clipboard.RecordClipboardUsageUseCase
import com.smartboard.domain.clipboard.SaveClipboardEntryUseCase
import com.smartboard.domain.clipboard.SearchClipboardUseCase
import com.smartboard.domain.pins.DeletePinUseCase
import com.smartboard.domain.pins.MovePinToFirstUseCase
import com.smartboard.domain.pins.ObservePinsUseCase
import com.smartboard.domain.pins.SavePinUseCase
import com.smartboard.domain.settings.ObserveSettingsUseCase
import com.smartboard.domain.settings.UpdateSettingUseCase
import com.smartboard.ime.layouts.AvroStyleComposer
import com.smartboard.ime.layouts.KeyAction
import com.smartboard.ime.layouts.KeyDef
import com.smartboard.ime.layouts.KeyboardLayout
import com.smartboard.ime.layouts.json.LanguageMeta
import com.smartboard.ime.layouts.json.LanguagePackLoader
import com.smartboard.ime.layouts.placeholderKeyboardLayout
import com.smartboard.model.ClipboardCategory
import com.smartboard.model.ClipboardEntry
import com.smartboard.model.ClipboardReadMode
import com.smartboard.model.KeyboardSettings
import com.smartboard.model.PinnedSnippet
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@OptIn(ExperimentalCoroutinesApi::class)
class KeyboardController @Inject constructor(
    private val observeSettings: ObserveSettingsUseCase,
    private val updateSetting: UpdateSettingUseCase,
    private val observePins: ObservePinsUseCase,
    private val observeClipboard: ObserveClipboardUseCase,
    private val searchClipboard: SearchClipboardUseCase,
    private val saveClipboardEntry: SaveClipboardEntryUseCase,
    private val deleteClipboardEntry: DeleteClipboardEntryUseCase,
    private val clearClipboardHistory: ClearClipboardHistoryUseCase,
    private val pinClipboardEntryUseCase: PinClipboardEntryUseCase,
    private val recordClipboardUsage: RecordClipboardUsageUseCase,
    private val savePinUseCase: SavePinUseCase,
    private val deletePinUseCase: DeletePinUseCase,
    private val movePinFirst: MovePinToFirstUseCase,
    private val languagePackLoader: LanguagePackLoader,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var inputConnection: InputConnection? = null
    private var layoutLoadJob: Job? = null
    private var lastShiftTapTime = 0L

    private val categoryFilter = MutableStateFlow<ClipboardCategory?>(null)
    private val searchQuery = MutableStateFlow("")

    private val _catalogState = MutableStateFlow<List<LanguageMeta>>(emptyList())
    val languageCatalog: StateFlow<List<LanguageMeta>> = _catalogState.asStateFlow()

    private val _uiState = MutableStateFlow(KeyboardUiState(layout = placeholderKeyboardLayout()))
    val uiState: StateFlow<KeyboardUiState> = _uiState

    init {
        scope.launch(Dispatchers.IO) {
            _catalogState.value = languagePackLoader.loadAllMeta()
        }

        observeSettings()
            .onEach { settings ->
                _uiState.update { it.copy(settings = settings) }
                requestLayoutReload(settings)
            }
            .launchIn(scope)

        observePins()
            .onEach { pins -> _uiState.update { it.copy(pins = pins) } }
            .launchIn(scope)

        combine(observeClipboard(), searchQuery, categoryFilter) { all, query, cat ->
            Triple(all, query, cat)
        }
            .onEach { (all, query, cat) ->
                if (query.isNotBlank()) return@onEach
                val list = if (cat == null) all else all.filter {
                    if (cat == ClipboardCategory.OTHER) true else it.category == cat
                }
                _uiState.update { it.copy(clipboardItems = list, clipboardCategoryFilter = cat) }
            }
            .launchIn(scope)

        searchQuery
            .flatMapLatest { q ->
                if (q.isBlank()) flowOf(emptyList()) else searchClipboard(q)
            }
            .onEach { results ->
                if (searchQuery.value.isNotBlank()) {
                    _uiState.update { it.copy(clipboardItems = results) }
                }
            }
            .launchIn(scope)
    }

    fun bindInputConnection(ic: InputConnection?) {
        inputConnection = ic
    }

    /**
     * Called when a new editor starts. Resets transient typing state so the keyboard does not
     * carry over shift/composer/panel state between unrelated text fields.
     */
    fun onStartInput(info: EditorInfo?) {
        _uiState.update {
            it.copy(
                activePanel = ImePanel.None,
                composerBuffer = "",
                shiftPressed = false,
                capsLock = false,
                suggestions = emptyList(),
            )
        }
    }

    fun onWindowHidden() {
        inputConnection = null
        _uiState.update { it.copy(activePanel = ImePanel.None, composerBuffer = "") }
    }

    fun onNewClipboardText(text: String, readMode: ClipboardReadMode) {
        if (readMode == ClipboardReadMode.ON_OPEN) return
        scope.launch(Dispatchers.IO) {
            saveClipboardEntry(text)
        }
    }

    fun setClipboardSearch(query: String) {
        searchQuery.value = query
        _uiState.update { it.copy(clipboardSearch = query) }
    }

    fun setCategoryFilter(category: ClipboardCategory?) {
        categoryFilter.value = category
        _uiState.update { it.copy(clipboardCategoryFilter = category) }
    }

    var readSystemClipboard: (() -> String?)? = null

    fun openPanel(panel: ImePanel) {
        val previous = _uiState.value.activePanel
        _uiState.update {
            val next = if (it.activePanel == panel) ImePanel.None else panel
            it.copy(activePanel = next)
        }
        val now = _uiState.value.activePanel
        if (previous != ImePanel.Clipboard && now == ImePanel.Clipboard) {
            val readMode = _uiState.value.settings?.clipboardReadMode
            if (readMode == ClipboardReadMode.ON_OPEN) {
                readSystemClipboard?.invoke()?.let { t ->
                    if (t.isNotBlank()) {
                        scope.launch(Dispatchers.IO) { saveClipboardEntry(t) }
                    }
                }
            }
        }
    }

    fun closePanel() {
        _uiState.update { it.copy(activePanel = ImePanel.None) }
    }

    fun toggleShift() {
        val now = System.currentTimeMillis()
        _uiState.update { st ->
            when {
                // A tap while caps lock is engaged turns everything off.
                st.capsLock -> st.copy(capsLock = false, shiftPressed = false)
                // Two quick taps engage caps lock.
                now - lastShiftTapTime <= DOUBLE_TAP_WINDOW_MS && st.shiftPressed ->
                    st.copy(capsLock = true, shiftPressed = true)
                else -> st.copy(shiftPressed = !st.shiftPressed)
            }
        }
        lastShiftTapTime = now
    }

    fun toggleSymbols() {
        _uiState.update { it.copy(symbolsMode = !it.symbolsMode) }
        requestLayoutReload()
    }

    fun switchLanguage(forward: Boolean = true) {
        val s = _uiState.value.settings ?: return
        val list = s.activeLanguages
        if (list.size < 2) return
        _uiState.update { it.copy(languageSwitchForward = forward, composerBuffer = "") }
        val n = list.size
        val nextIdx = (s.currentLanguageIndex + if (forward) 1 else n - 1).floorMod(n)
        scope.launch(Dispatchers.IO) {
            val lang = list[nextIdx]
            updateSetting { it.copy(currentLanguageIndex = nextIdx, activeLanguage = lang) }
        }
    }

    fun openLanguagePicker() {
        _uiState.update { it.copy(showLanguagePicker = true) }
    }

    fun dismissLanguagePicker() {
        _uiState.update { it.copy(showLanguagePicker = false) }
    }

    fun selectLanguageFromPicker(locale: String) {
        _uiState.update { it.copy(composerBuffer = "") }
        scope.launch(Dispatchers.IO) {
            updateSetting { s ->
                val idx = s.activeLanguages.indexOf(locale)
                when {
                    idx >= 0 -> s.copy(currentLanguageIndex = idx, activeLanguage = s.activeLanguages[idx])
                    else -> s
                }
            }
        }
        dismissLanguagePicker()
    }

    fun applyImeSubtypeLayoutLocale(layoutLocale: String?) {
        val loc = layoutLocale?.trim()?.replace('-', '_')?.takeIf { it.isNotEmpty() } ?: return
        scope.launch(Dispatchers.IO) {
            updateSetting { s ->
                val list = s.activeLanguages.toMutableList()
                val idx = list.indexOf(loc)
                if (idx >= 0) {
                    s.copy(currentLanguageIndex = idx, activeLanguage = list[idx])
                } else {
                    val newList = list + loc
                    s.copy(
                        activeLanguages = newList,
                        currentLanguageIndex = newList.lastIndex,
                        activeLanguage = loc,
                    )
                }
            }
        }
    }

    private fun requestLayoutReload(settings: KeyboardSettings? = null) {
        val st = settings ?: _uiState.value.settings ?: return
        layoutLoadJob?.cancel()
        layoutLoadJob = scope.launch {
            val sym = _uiState.value.symbolsMode
            val includeGlobe = st.activeLanguages.size >= 2
            val locale = st.currentLocale()
            val layout = loadLayoutSafe(locale, sym, includeGlobe)
            _uiState.update { it.copy(layout = layout) }
        }
    }

    private suspend fun loadLayoutSafe(locale: String, symbols: Boolean, includeGlobe: Boolean): KeyboardLayout {
        try {
            return languagePackLoader.loadKeyboard(locale, symbols, includeGlobe)
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            try {
                return languagePackLoader.loadKeyboard("en_US", symbols, includeGlobe)
            } catch (e2: CancellationException) {
                throw e2
            } catch (_: Exception) {
                return placeholderKeyboardLayout()
            }
        }
    }

    fun onKey(def: KeyDef) {
        when (def.action) {
            KeyAction.CHARACTER -> handleCharacter(def)
            KeyAction.BACKSPACE -> handleBackspace()
            KeyAction.SHIFT -> toggleShift()
            KeyAction.ENTER -> {
                clearComposer()
                inputConnection?.commitText("\n", 1)
            }
            KeyAction.SPACE -> {
                clearComposer()
                inputConnection?.commitText(" ", 1)
                if (_uiState.value.shiftPressed && !_uiState.value.capsLock) {
                    _uiState.update { it.copy(shiftPressed = false) }
                }
            }
            KeyAction.SWITCH_LANGUAGE -> switchLanguage(_uiState.value.languageSwitchForward)
            KeyAction.EMOJI -> openPanel(ImePanel.Emoji)
            KeyAction.CLIPBOARD -> openPanel(ImePanel.Clipboard)
            KeyAction.SYMBOLS -> toggleSymbols()
            KeyAction.GIF -> openPanel(ImePanel.Gif)
            else -> handleCharacter(def)
        }
    }

    private fun clearComposer() {
        if (_uiState.value.composerBuffer.isNotEmpty()) {
            _uiState.update { it.copy(composerBuffer = "") }
        }
    }

    private fun handleCharacter(def: KeyDef) {
        val ic = inputConnection ?: return
        val layout = _uiState.value.layout
        val shiftOn = _uiState.value.shiftPressed || _uiState.value.capsLock
        val raw = when {
            shiftOn && !def.primaryShift.isNullOrEmpty() -> def.primaryShift!!
            shiftOn && def.primary.length == 1 && def.primary[0] in 'a'..'z' -> def.primary.uppercase()
            else -> def.primary
        }
        if (raw.isEmpty()) return
        val useAvro = layout.locale == "bn_BD" ||
            layout.family.equals("bengali", ignoreCase = true)
        if (useAvro) {
            val ch = raw.first()
            val isLatin = raw.length == 1 && ch.isLetter() &&
                Character.UnicodeBlock.of(ch) == Character.UnicodeBlock.BASIC_LATIN
            if (isLatin) {
                val (buf, out) = AvroStyleComposer.commitKey(_uiState.value.composerBuffer, raw)
                _uiState.update { it.copy(composerBuffer = buf) }
                if (out.isNotEmpty()) {
                    ic.commitText(out, 1)
                }
                // A single shift press only affects the next character.
                if (_uiState.value.shiftPressed && !_uiState.value.capsLock) {
                    _uiState.update { it.copy(shiftPressed = false) }
                }
                return
            }
        }
        // A single shift press only affects the next character; caps lock persists.
        if (_uiState.value.shiftPressed && !_uiState.value.capsLock) {
            _uiState.update { it.copy(shiftPressed = false) }
        }
        ic.commitText(raw, 1)
    }

    private fun handleBackspace() {
        val ic = inputConnection ?: return
        // Consume any pending transliteration buffer first.
        val buffer = _uiState.value.composerBuffer
        if (buffer.isNotEmpty()) {
            _uiState.update { it.copy(composerBuffer = buffer.dropLast(1)) }
        }
        // If there is a selection, delete it.
        val selected = ic.getSelectedText(0)
        if (!selected.isNullOrEmpty()) {
            ic.commitText("", 1)
            return
        }
        // Delete a whole code point so emoji / surrogate pairs are removed cleanly.
        val before = ic.getTextBeforeCursor(2, 0)
        val len = before?.length ?: 0
        val count = if (len >= 2 && Character.isSurrogatePair(before!![len - 2], before[len - 1])) 2 else 1
        ic.deleteSurroundingText(count, 0)
    }

    fun pasteFromClipboardItem(entry: ClipboardEntry) {
        scope.launch(Dispatchers.IO) {
            recordClipboardUsage(entry.id)
        }
        inputConnection?.commitText(entry.contentText, 1)
        closePanel()
    }

    fun pasteText(text: String) {
        inputConnection?.commitText(text, 1)
    }

    fun deleteClipboardItem(id: Long) {
        scope.launch(Dispatchers.IO) { deleteClipboardEntry(id) }
    }

    fun clearClipboard() {
        scope.launch(Dispatchers.IO) { clearClipboardHistory() }
    }

    fun pinClipboardItem(id: Long, pinned: Boolean) {
        scope.launch(Dispatchers.IO) { pinClipboardEntryUseCase(id, pinned) }
    }

    fun addQuickPin(title: String, body: String) {
        scope.launch(Dispatchers.IO) { savePinUseCase(title, body, null) }
    }

    fun deletePin(id: Long) {
        scope.launch(Dispatchers.IO) { deletePinUseCase(id) }
    }

    fun movePinToFirst(id: Long) {
        scope.launch(Dispatchers.IO) { movePinFirst(id) }
    }

    fun setSuggestions(words: List<String>) {
        _uiState.update { it.copy(suggestions = words) }
    }

    fun applySuggestion(word: String) {
        inputConnection?.commitText("$word ", 1)
        _uiState.update { it.copy(suggestions = emptyList()) }
    }

    fun selectPinned(snippet: PinnedSnippet) {
        _uiState.update { it.copy(lastPinnedSelectionId = snippet.id) }
        pasteText(snippet.body)
    }
}

private fun Int.floorMod(n: Int): Int = ((this % n) + n) % n

private const val DOUBLE_TAP_WINDOW_MS = 350L
