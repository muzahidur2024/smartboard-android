package com.smartboard.ime.ui.pinnedbar

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartboard.model.PinnedSnippet
import com.smartboard.ui.theme.SmartBoardThemeColors

/**
 * A horizontal strip of pinned text snippets. Tap a chip to insert it; long-press to remove it.
 * The trailing "+" button pins a new snippet.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PinnedBar(
    visible: Boolean,
    pins: List<PinnedSnippet>,
    onSelect: (PinnedSnippet) -> Unit,
    onRemove: (Long) -> Unit,
    onAdd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (!visible) return
    val colors = SmartBoardThemeColors.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            Icons.Rounded.PushPin,
            contentDescription = null,
            tint = colors.categoryChipText,
            modifier = Modifier.size(18.dp),
        )
        Row(
            modifier = Modifier
                .weight(1f)
                .horizontalScroll(rememberScrollState()),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (pins.isEmpty()) {
                Text(
                    text = "Pin snippets for one-tap insert",
                    fontSize = 13.sp,
                    color = colors.keyText.copy(alpha = 0.45f),
                )
            } else {
                pins.forEach { pin ->
                    Box(
                        modifier = Modifier
                            .height(28.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(colors.pinnedChipBg)
                            .combinedClickable(
                                onClick = { onSelect(pin) },
                                onLongClick = { onRemove(pin.id) },
                            )
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = pin.title.ifBlank { pin.body.take(20) },
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontSize = 13.sp,
                            color = colors.keyText,
                        )
                    }
                }
            }
        }
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(colors.pinnedChipBg)
                .clickable(onClick = onAdd),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Rounded.Add,
                contentDescription = "Pin new snippet",
                tint = colors.keyText,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}
