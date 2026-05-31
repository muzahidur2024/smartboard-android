package com.smartboard.ime.ui.toolbar

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContentPaste
import androidx.compose.material.icons.rounded.EmojiEmotions
import androidx.compose.material.icons.rounded.GifBox
import androidx.compose.material.icons.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.smartboard.ui.theme.SmartBoardThemeColors

/**
 * A Gboard-style action toolbar shown above the keys when there are no word suggestions.
 * Every icon performs a real action.
 */
@Composable
fun KeyboardToolbar(
    showLanguage: Boolean,
    onClipboard: () -> Unit,
    onEmoji: () -> Unit,
    onGif: () -> Unit,
    onLanguage: () -> Unit,
    onVoice: () -> Unit,
    onSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = SmartBoardThemeColors.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .padding(horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ToolbarIcon(Icons.Rounded.ContentPaste, "Clipboard", colors.keyText, onClipboard)
        ToolbarIcon(Icons.Rounded.EmojiEmotions, "Emoji", colors.keyText, onEmoji)
        ToolbarIcon(Icons.Rounded.GifBox, "GIF", colors.keyText, onGif)
        if (showLanguage) {
            ToolbarIcon(Icons.Rounded.Language, "Switch language", colors.keyText, onLanguage)
        }
        ToolbarIcon(Icons.Rounded.Mic, "Voice", colors.keyText, onVoice)
        Spacer(Modifier.weight(1f))
        ToolbarIcon(Icons.Rounded.Settings, "Settings", colors.keyText, onSettings)
        Box(
            modifier = Modifier.size(40.dp),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = colors.keyText.copy(alpha = 0.35f),
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

@Composable
private fun ToolbarIcon(
    icon: ImageVector,
    description: String,
    tint: Color,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = description,
            tint = tint.copy(alpha = 0.85f),
            modifier = Modifier.size(22.dp),
        )
    }
}
