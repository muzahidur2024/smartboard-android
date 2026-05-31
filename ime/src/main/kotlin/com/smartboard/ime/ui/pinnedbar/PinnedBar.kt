package com.smartboard.ime.ui.pinnedbar

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.smartboard.model.PinnedSnippet
import com.smartboard.ui.theme.SmartBoardThemeColors

@Composable
fun PinnedBar(
    visible: Boolean,
    pins: List<PinnedSnippet>,
    onSelect: (PinnedSnippet) -> Unit,
    onAdd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (!visible) return
    val colors = SmartBoardThemeColors.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(Icons.Rounded.PushPin, contentDescription = null, tint = colors.categoryChipText)
        Row(
            modifier = Modifier
                .weight(1f)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            pins.forEach { pin ->
                AssistChip(
                    onClick = { onSelect(pin) },
                    label = {
                        Text(
                            text = pin.title.ifBlank { pin.body.take(20) },
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.labelMedium,
                        )
                    },
                )
            }
        }
        IconButton(onClick = onAdd, modifier = Modifier.height(36.dp)) {
            Icon(Icons.Rounded.Add, contentDescription = null)
        }
    }
}
