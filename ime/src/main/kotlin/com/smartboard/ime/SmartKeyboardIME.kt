package com.smartboard.ime

import android.content.ClipboardManager
import android.content.Context
import android.inputmethodservice.InputMethodService
import android.os.Build
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodSubtype
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.AbstractComposeView
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
 * SmartBoard input method service.
 *
 * Hosting Jetpack Compose inside an [InputMethodService] is tricky because:
 * 1. The service is NOT a [LifecycleOwner], [ViewModelStoreOwner], or [SavedStateRegistryOwner].
 * 2. When the ComposeView is added to the IME window by the framework (in `setInputView`), Compose
 *    walks up the view tree looking for a LifecycleOwner on the window root. Since the IME window
 *    is an internal LinearLayout (`parentPanel`) with no owners, it crashes with
 *    "ViewTreeLifecycleOwner not found".
 * 3. Setting owners on the decor view doesn't reliably work because the framework may add our view
 *    before the decor view is set up, or to a different parent.
 *
 * The solution: use a custom [AbstractComposeView] subclass that, in [onAttachedToWindow], walks
 * up the **entire** ancestor chain and sets the lifecycle/viewmodel/savedstate owners on every
 * view all the way to the root. This guarantees that no matter where Compose resolves from, it
 * will find valid owners.
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
        val controller = keyboardController
        val ime = this
        return ImeComposeView(this, ime) {
            KeyboardRoot(controller)
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

/**
 * A [AbstractComposeView] subclass that, before calling super.onAttachedToWindow (which triggers
 * the WindowRecomposer creation), stamps the LifecycleOwner/ViewModelStoreOwner/SavedStateRegistryOwner
 * on itself AND on every ancestor view up to the root. This is the only reliable way to ensure
 * Compose's `findViewTreeLifecycleOwner()` walk-up succeeds inside the IME window, whose internal
 * `parentPanel` LinearLayout is not under our control.
 */
private class ImeComposeView(
    context: Context,
    private val owner: SmartKeyboardIME,
    private val content: @Composable () -> Unit,
) : AbstractComposeView(context) {

    init {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool)
    }

    @Composable
    override fun Content() {
        content()
    }

    override fun onAttachedToWindow() {
        // Stamp the owners on this view.
        setViewTreeLifecycleOwner(owner)
        setViewTreeViewModelStoreOwner(owner)
        setViewTreeSavedStateRegistryOwner(owner)

        // Walk up to root and stamp on every ancestor. This is what makes the
        // WindowRecomposer's findViewTreeLifecycleOwner() succeed.
        var current: View? = this.parent as? View
        while (current != null) {
            current.setViewTreeLifecycleOwner(owner)
            current.setViewTreeViewModelStoreOwner(owner)
            current.setViewTreeSavedStateRegistryOwner(owner)
            current = current.parent as? View
        }

        super.onAttachedToWindow()
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
