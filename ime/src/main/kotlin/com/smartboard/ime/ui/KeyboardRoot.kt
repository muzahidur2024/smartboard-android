package com.smartboard.ime.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.smartboard.clipboardui.ClipboardPanel
import com.smartboard.ime.layouts.json.LanguageMeta
import com.smartboard.ime.ui.keys.KeyboardGrid
import com.smartboard.ime.ui.panels.EmojiPanel
import com.smartboard.ime.ui.panels.GifPanelPlaceholder
import com.smartboard.ime.ui.pinnedbar.PinnedBar
import com.smartboard.ime.ui.suggestions.SuggestionStrip
import com.smartboard.model.KeyboardSettings
import com.smartboard.ui.HapticsManager
import com.smartboard.ui.theme.SmartBoardTheme
import com.smartboard.ui.theme.SmartBoardThemeColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KeyboardRoot(
    controller: KeyboardController,
    modifier: Modifier = Modifier,
) {
    val state by controller.uiState.collectAsState()
    val catalog by controller.languageCatalog.collectAsState()
    val settings = state.settings ?: return
    val ctx = LocalContext.current
    val dark = (LocalConfiguration.current.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) ==
        android.content.res.Configuration.UI_MODE_NIGHT_YES
    var showClipboardSearch by remember { mutableStateOf(false) }

    SmartBoardTheme(
        themeMode = settings.themeMode,
        isSystemDark = dark,
        accent = settings.themeAccent,
    ) {
        val colors = SmartBoardThemeColors.colors
        val baseHeight = (220 * settings.keyboardHeightScale).dp
        val haptic = settings.hapticEnabled
        val isRtl = state.layout.direction == "rtl"

        if (state.showLanguagePicker) {
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ModalBottomSheet(
                onDismissRequest = { controller.dismissLanguagePicker() },
                sheetState = sheetState,
            ) {
                LanguagePickerSheet(
                    catalog = catalog,
                    settings = settings,
                    onPick = { controller.selectLanguageFromPicker(it) },
                    modifier = Modifier.padding(bottom = 24.dp),
                )
            }
        }

        CompositionLocalProvider(LocalLayoutDirection provides if (isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr) {
            Column(
                modifier = modifier
                    .fillMaxWidth()
                    .height(baseHeight)
                    .background(colors.keyboardBackground, RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp)),
            ) {
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(colors.divider),
                )
                if (state.activePanel == ImePanel.None) {
                    SuggestionStrip(
                        suggestions = state.suggestions,
                        onSuggestion = { controller.applySuggestion(it) },
                        wordmark = ctx.getString(com.smartboard.ime.R.string.app_wordmark),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(4.dp))
                    PinnedBar(
                        visible = settings.pinnedBarVisible,
                        pins = state.pins,
                        onSelect = { controller.selectPinned(it) },
                        onAdd = {
                            controller.addQuickPin(
                                title = ctx.getString(com.smartboard.ime.R.string.quick_pin_title),
                                body = ctx.getString(com.smartboard.ime.R.string.quick_pin_body),
                            )
                        },
                    )
                }
                when (state.activePanel) {
                    ImePanel.Clipboard -> ClipboardPanel(
                        items = state.clipboardItems,
                        searchQuery = state.clipboardSearch,
                        onSearchChange = { controller.setClipboardSearch(it) },
                        showSearch = showClipboardSearch,
                        onToggleSearch = { showClipboardSearch = !showClipboardSearch },
                        onClearAll = { controller.clearClipboard() },
                        onClose = {
                            showClipboardSearch = false
                            controller.closePanel()
                        },
                        onCategorySelected = { controller.setCategoryFilter(it) },
                        selectedCategory = state.clipboardCategoryFilter,
                        onItemClick = { controller.pasteFromClipboardItem(it) },
                        onDeleteItem = { controller.deleteClipboardItem(it) },
                        onPinToggle = { id, pinned -> controller.pinClipboardItem(id, pinned) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                    )

                    ImePanel.Emoji -> EmojiPanel(
                        settings = settings,
                        onPick = { controller.pasteText(it) },
                        onClose = { controller.closePanel() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                    )

                    ImePanel.Gif -> Box(
                        Modifier
                            .fillMaxWidth()
                            .weight(1f),
                    ) {
                        GifPanelPlaceholder()
                    }

                    ImePanel.None -> AnimatedContent(
                        targetState = state.layout,
                        transitionSpec = {
                            val langs = settings.activeLanguages
                            val forward = if (langs.isNotEmpty()) {
                                val ti = langs.indexOf(targetState.locale).takeIf { it >= 0 } ?: 0
                                val ii = langs.indexOf(initialState.locale).takeIf { it >= 0 } ?: 0
                                ti >= ii
                            } else {
                                state.languageSwitchForward
                            }
                            slideInHorizontally(
                                animationSpec = tween(200, easing = FastOutSlowInEasing),
                                initialOffsetX = { if (forward) it else -it },
                            ) togetherWith slideOutHorizontally(
                                animationSpec = tween(200, easing = FastOutSlowInEasing),
                                targetOffsetX = { if (forward) -it else it },
                            )
                        },
                        label = "keyboard_layout",
                    ) { layout ->
                        KeyboardGrid(
                            layout = layout,
                            settings = settings,
                            shiftPressed = state.shiftPressed,
                            onKey = { controller.onKey(it) },
                            onSpaceLongPress = { controller.openLanguagePicker() },
                            hapticEnabled = haptic,
                            onHapticKey = { HapticsManager.keyTap(ctx, haptic) },
                            onHapticSpecial = { HapticsManager.specialKey(ctx, haptic) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LanguagePickerSheet(
    catalog: List<LanguageMeta>,
    settings: KeyboardSettings,
    onPick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var query by remember { mutableStateOf("") }
    val q = query.trim().lowercase()
    val filtered = remember(catalog, q) {
        if (q.isEmpty()) catalog
        else catalog.filter {
            it.displayName.lowercase().contains(q) ||
                it.nativeDisplayName.lowercase().contains(q) ||
                it.locale.lowercase().contains(q)
        }
    }
    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            placeholder = { Text(stringResource(com.smartboard.ime.R.string.language_picker_search)) },
            singleLine = true,
        )
        HorizontalDivider()
        LazyColumn {
            items(filtered, key = { it.locale }) { meta ->
                val isCurrent = settings.currentLocale() == meta.locale
                ListItem(
                    headlineContent = { Text(meta.nativeDisplayName) },
                    supportingContent = { Text("${meta.displayName} · ${meta.locale}") },
                    leadingContent = { Text(meta.flagEmoji.ifBlank { "·" }) },
                    trailingContent = {
                        if (isCurrent) {
                            Text("✓")
                        } else {
                            TextButton(onClick = { onPick(meta.locale) }) {
                                Text("Select")
                            }
                        }
                    },
                )
            }
        }
    }
}
