package com.smartboard.ime.ui.panels

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Backspace
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartboard.model.KeyboardSettings
import com.smartboard.ui.theme.SmartBoardThemeColors

private data class EmojiCategory(val label: String, val emojis: List<String>)

private val EMOJI_CATEGORIES = listOf(
    EmojiCategory(
        "😀",
        listOf(
            "😀", "😃", "😄", "😁", "😆", "😅", "🤣", "😂", "🙂", "🙃",
            "😉", "😊", "😇", "🥰", "😍", "🤩", "😘", "😗", "😚", "😙",
            "😋", "😛", "😜", "🤪", "😝", "🤔", "🤨", "😐", "😑", "😶",
            "😏", "😒", "🙄", "😬", "😮‍💨", "🤥", "😌", "😔", "😴", "😪",
        ),
    ),
    EmojiCategory(
        "👍",
        listOf(
            "👍", "👎", "👌", "🤌", "✌️", "🤞", "🤟", "🤙", "👏", "🙌",
            "🙏", "💪", "👀", "🫶", "🤝", "✊", "👊", "👋", "🖐️", "✋",
        ),
    ),
    EmojiCategory(
        "❤️",
        listOf(
            "❤️", "🧡", "💛", "💚", "💙", "💜", "🖤", "🤍", "💖", "💗",
            "💓", "💞", "💕", "💔", "❣️", "💟", "💯", "✨", "🔥", "🎉",
        ),
    ),
    EmojiCategory(
        "🐶",
        listOf(
            "🐶", "🐱", "🐭", "🐹", "🐰", "🦊", "🐻", "🐼", "🐨", "🐯",
            "🦁", "🐮", "🐷", "🐸", "🐵", "🐔", "🦄", "🐝", "🦋", "🌟",
        ),
    ),
    EmojiCategory(
        "🍔",
        listOf(
            "🍏", "🍎", "🍐", "🍊", "🍋", "🍌", "🍉", "🍇", "🍓", "🫐",
            "🍔", "🍟", "🍕", "🌮", "🍿", "🍩", "🍪", "🎂", "☕", "🍻",
        ),
    ),
    EmojiCategory(
        "⚽",
        listOf(
            "⚽", "🏀", "🏈", "⚾", "🎾", "🏐", "🚀", "✈️", "🚗", "🏆",
            "🎮", "🎧", "📱", "💻", "📌", "🧠", "🛠️", "🌈", "🌍", "⭐",
        ),
    ),
)

@Composable
fun EmojiPanel(
    settings: KeyboardSettings?,
    onPick: (String) -> Unit,
    onBackspace: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = SmartBoardThemeColors.colors
    var selectedCategory by remember { mutableStateOf(0) }
    val emojis = EMOJI_CATEGORIES[selectedCategory].emojis

    Column(modifier = modifier.background(colors.clipboardBg)) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 40.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) {
            items(emojis, key = { it }) { emoji ->
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clickable { onPick(emoji) },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(text = emoji, fontSize = 24.sp)
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(width = 56.dp, height = 38.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(colors.keySurfaceSpecial)
                    .clickable { onClose() },
                contentAlignment = Alignment.Center,
            ) {
                Text("ABC", fontWeight = FontWeight.Medium, color = colors.keyText)
            }
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.Center,
            ) {
                EMOJI_CATEGORIES.forEachIndexed { index, category ->
                    val selected = index == selectedCategory
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 2.dp)
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (selected) colors.categoryChipBg else colors.clipboardBg)
                            .clickable { selectedCategory = index },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(category.label, fontSize = 18.sp)
                    }
                }
            }
            IconButton(onClick = onBackspace) {
                Icon(
                    Icons.Rounded.Backspace,
                    contentDescription = "Backspace",
                    tint = colors.keyText,
                )
            }
        }
    }
}
