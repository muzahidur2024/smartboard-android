package com.smartboard.ime

import android.content.ClipboardManager
import android.inputmethodservice.InputMethodService
import android.os.Build
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodSubtype
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.smartboard.ime.ui.KeyboardController
import com.smartboard.ime.ui.KeyboardRoot
import com.smartboard.model.ClipboardReadMode
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * SmartBoard input method.
 *
 * Jetpack Compose can only be hosted inside a [ComposeView] when the view tree exposes a
 * [LifecycleOwner], a [ViewModelStoreOwner] and a [SavedStateRegistryOwner]. An
 * [InputMethodService] does not provide any of these by default, which is why hosting Compose
 * here previously crashed with "ViewTreeLifecycleOwner not found" the moment the keyboard was
 * shown. We therefore implement the three owners ourselves and drive the lifecycle from the IME
 * callbacks.
 */
@AndroidEntryPoint
class SmartKeyboardIME :
    InputMethodService(),
    LifecycleOwner,
    ViewModelStoreOwner,
    SavedStateRegistryOwner {

    @Inject
    lateinit var keyboardController: KeyboardController

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val store = ViewModelStore()
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val viewModelStore: ViewModelStore get() = store
    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    private val clipListener = ClipboardManager.OnPrimaryClipChangedListener {
        val cm = getSystemService(ClipboardManager::class.java)
            ?: return@OnPrimaryClipChangedListener
        val clip = cm.primaryClip ?: return@OnPrimaryClipChangedListener
        if (clip.itemCount == 0) return@OnPrimaryClipChangedListener
        val text = clip.getItemAt(0)?.coerceToText(this@SmartKeyboardIME)?.toString()
            ?: return@OnPrimaryClipChangedListener
        if (text.isBlank()) return@OnPrimaryClipChangedListener
        val mode = keyboardController.uiState.value.settings?.clipboardReadMode
            ?: ClipboardReadMode.BACKGROUND
        keyboardController.onNewClipboardText(text, mode)
    }

    override fun onCreate() {
        // performRestore must run before the lifecycle moves past INITIALIZED.
        savedStateRegistryController.performRestore(null)
        super.onCreate()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        keyboardController.readSystemClipboard = {
            runCatching {
                getSystemService(ClipboardManager::class.java)
                    ?.primaryClip
                    ?.takeIf { it.itemCount > 0 }
                    ?.getItemAt(0)
                    ?.coerceToText(this)
                    ?.toString()
            }.getOrNull()
        }
    }

    override fun onCreateInputView(): View {
        return ComposeView(this).apply {
            // Make the owners discoverable by Compose's ViewTree* lookups.
            setViewTreeLifecycleOwner(this@SmartKeyboardIME)
            setViewTreeViewModelStoreOwner(this@SmartKeyboardIME)
            setViewTreeSavedStateRegistryOwner(this@SmartKeyboardIME)
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                KeyboardRoot(keyboardController)
            }
        }
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        keyboardController.bindInputConnection(currentInputConnection)
        keyboardController.onStartInput(info)
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
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        runCatching {
            getSystemService(ClipboardManager::class.java)
                ?.addPrimaryClipChangedListener(clipListener)
        }
    }

    override fun onWindowHidden() {
        keyboardController.onWindowHidden()
        runCatching {
            getSystemService(ClipboardManager::class.java)
                ?.removePrimaryClipChangedListener(clipListener)
        }
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        super.onWindowHidden()
    }

    override fun onDestroy() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        store.clear()
        super.onDestroy()
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
