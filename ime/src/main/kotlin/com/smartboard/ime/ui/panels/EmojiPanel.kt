package com.smartboard.ime.ui.panels

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartboard.model.KeyboardSettings

private val SAMPLE_EMOJIS = listOf(
    "😀", "😂", "🥰", "😎", "🤔", "👍", "🙏", "🔥", "🎉", "❤️",
    "😭", "🤣", "✨", "🥳", "💯", "📌", "🧠", "🛠️", "🚀", "🌟",
)

@Composable
fun EmojiPanel(
    settings: KeyboardSettings?,
    onPick: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(8),
        modifier = modifier.heightIn(max = 280.dp),
    ) {
        items(SAMPLE_EMOJIS, key = { it }) { emoji ->
            Text(
                text = emoji,
                modifier = Modifier.clickable { onPick(emoji) },
                fontSize = 24.sp,
            )
        }
    }
}
