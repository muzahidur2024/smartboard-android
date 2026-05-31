package com.smartboard.ime.ui.keys

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Language
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.smartboard.ime.layouts.KeyAction
import com.smartboard.ime.layouts.KeyDef
import com.smartboard.ime.layouts.KeyWidth
import com.smartboard.ime.layouts.KeyboardLayout
import com.smartboard.ime.layouts.englishNumbersRow
import com.smartboard.model.KeyboardSettings
import com.smartboard.ui.theme.SmartBoardThemeColors

@Composable
fun KeyboardGrid(
    layout: KeyboardLayout,
    settings: KeyboardSettings?,
    shiftPressed: Boolean,
    capsLock: Boolean,
    onKey: (KeyDef) -> Unit,
    onSpaceLongPress: () -> Unit,
    hapticEnabled: Boolean,
    onHapticKey: () -> Unit,
    onHapticSpecial: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = SmartBoardThemeColors.colors
    val ctx = LocalContext.current
    val showNumbers = settings?.numberRowEnabled == true && layout.hasNumberRowInPack
    val numberKeys = when {
        showNumbers && layout.numberRow != null -> layout.numberRow
        showNumbers -> englishNumbersRow()
        else -> null
    }
    val multiLang = (settings?.activeLanguages?.size ?: 0) >= 2
    val spaceLabel = if (multiLang) {
        layout.nativeDisplayName.ifBlank { layout.name }
    } else {
        ctx.getString(com.smartboard.ime.R.string.keyboard_brand_label)
    }
    Column(modifier = modifier.fillMaxWidth()) {
        if (numberKeys != null) {
            Row(Modifier.fillMaxWidth()) {
                numberKeys.forEach { key ->
                    KeyboardKey(
                        label = key.primary,
                        secondaryLabel = key.secondary,
                        onClick = { onKey(key.copy(action = KeyAction.CHARACTER)) },
                        modifier = Modifier.weight(1f),
                        emphasized = false,
                        hapticEnabled = hapticEnabled,
                        onHapticKey = onHapticKey,
                        onHapticSpecial = onHapticSpecial,
                        background = colors.keySurface,
                        contentColor = colors.keyText,
                    )
                    Spacer(Modifier.width(4.dp))
                }
            }
            Spacer(Modifier.height(6.dp))
        }
        layout.rows.forEach { row ->
            Row(Modifier.fillMaxWidth()) {
                row.keys.forEach { key ->
                    val weight = when (key.width) {
                        KeyWidth.NARROW -> 0.7f
                        KeyWidth.NORMAL -> 1f
                        KeyWidth.WIDE -> 1.45f
                        KeyWidth.SPACE -> 4.2f
                        KeyWidth.FULL -> 12f
                    }
                    val label = when (key.action) {
                        KeyAction.CHARACTER -> {
                            if (shiftPressed && layout.hasShiftVariants && key.primaryShift != null) {
                                key.primaryShift!!
                            } else {
                                key.primary
                            }
                        }
                        KeyAction.SHIFT -> if (capsLock) "⇪" else key.primary
                        KeyAction.SPACE -> spaceLabel
                        KeyAction.SWITCH_LANGUAGE -> ""
                        else -> key.primary
                    }
                    val icon = when (key.action) {
                        KeyAction.SWITCH_LANGUAGE -> Icons.Rounded.Language
                        else -> null
                    }
                    val longPress = when (key.action) {
                        KeyAction.SPACE -> onSpaceLongPress
                        KeyAction.SWITCH_LANGUAGE -> onSpaceLongPress
                        else -> null
                    }
                    KeyboardKey(
                        label = label,
                        secondaryLabel = if (key.action == KeyAction.CHARACTER) key.secondary else null,
                        onClick = { onKey(key) },
                        modifier = Modifier.weight(weight),
                        icon = icon,
        emphasized = key.action == KeyAction.SPACE ||
                            key.action == KeyAction.ENTER ||
                            key.action == KeyAction.BACKSPACE ||
                            key.action == KeyAction.SHIFT ||
                            key.action == KeyAction.SWITCH_LANGUAGE,
                        hapticEnabled = hapticEnabled,
                        onHapticKey = onHapticKey,
                        onHapticSpecial = onHapticSpecial,
                        background = when (key.action) {
                            KeyAction.SPACE, KeyAction.BACKSPACE, KeyAction.ENTER, KeyAction.SHIFT -> colors.keySurfaceSpecial
                            else -> colors.keySurface
                        },
                        contentColor = colors.keyText,
                        onLongClick = longPress,
                    )
                    Spacer(Modifier.width(4.dp))
                }
            }
            Spacer(Modifier.height(6.dp))
        }
    }
}
