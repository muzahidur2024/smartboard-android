package com.smartboard.ime

import android.os.Build
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodSubtype
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.content.getSystemService
import com.smartboard.ime.ui.KeyboardController
import com.smartboard.ime.ui.KeyboardRoot
import com.smartboard.model.ClipboardReadMode
import android.content.ClipboardManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SmartKeyboardIME : android.inputmethodservice.InputMethodService() {

    @Inject
    lateinit var keyboardController: KeyboardController

    private val clipListener = ClipboardManager.OnPrimaryClipChangedListener {
        val cm = getSystemService(ClipboardManager::class.java) ?: return@OnPrimaryClipChangedListener
        val text = cm.primaryClip?.getItemAt(0)?.coerceToText(this@SmartKeyboardIME)?.toString()
            ?: return@OnPrimaryClipChangedListener
        val mode = keyboardController.uiState.value.settings?.clipboardReadMode
            ?: ClipboardReadMode.BACKGROUND
        keyboardController.onNewClipboardText(text, mode)
    }

    override fun onCreate() {
        super.onCreate()
        keyboardController.readSystemClipboard = {
            getSystemService(ClipboardManager::class.java)
                ?.primaryClip?.getItemAt(0)?.coerceToText(this)?.toString()
        }
    }

    override fun onCreateInputView(): View {
        return ComposeView(this).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
            setContent {
                KeyboardRoot(keyboardController)
            }
        }
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        keyboardController.bindInputConnection(currentInputConnection)
    }

    override fun onCurrentInputMethodSubtypeChanged(newSubtype: InputMethodSubtype) {
        super.onCurrentInputMethodSubtypeChanged(newSubtype)
        keyboardController.applyImeSubtypeLayoutLocale(layoutLocaleFromSubtype(newSubtype))
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        keyboardController.bindInputConnection(null)
        super.onFinishInputView(finishingInput)
    }

    override fun onWindowShown() {
        super.onWindowShown()
        getSystemService(ClipboardManager::class.java)?.addPrimaryClipChangedListener(clipListener)
    }

    override fun onWindowHidden() {
        keyboardController.onWindowHidden()
        getSystemService(ClipboardManager::class.java)?.removePrimaryClipChangedListener(clipListener)
        super.onWindowHidden()
    }
}

private fun parseKeyboardLayoutSet(extra: String?): String? {
    if (extra.isNullOrBlank()) return null
    for (segment in extra.split(',')) {
        val parts = segment.split('=', limit = 2).map { it.trim() }
        if (parts.size == 2 &&
            parts[0].equals("KeyboardLayoutSet", ignoreCase = true) &&
            parts[1].isNotEmpty()
        ) {
            return parts[1]
        }
    }
    return null
}

private fun layoutLocaleFromSubtype(subtype: InputMethodSubtype): String? {
    parseKeyboardLayoutSet(subtype.extraValue)?.let { return it.replace('-', '_') }
    if (Build.VERSION.SDK_INT >= 24) {
        val tag = subtype.languageTag
        if (tag.isNotBlank()) return tag.replace('-', '_')
    }
    @Suppress("DEPRECATION")
    val loc = subtype.locale
    return loc?.replace('-', '_')?.takeIf { it.isNotBlank() }
}
