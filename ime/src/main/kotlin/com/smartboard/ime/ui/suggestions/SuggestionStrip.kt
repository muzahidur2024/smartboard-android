package com.smartboard.ime.ui.suggestions

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.smartboard.ui.theme.SmartBoardThemeColors

@Composable
fun SuggestionStrip(
    suggestions: List<String>,
    onSuggestion: (String) -> Unit,
    wordmark: String,
    modifier: Modifier = Modifier,
) {
    val colors = SmartBoardThemeColors.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (suggestions.isEmpty()) {
            Text(
                text = wordmark,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                modifier = Modifier.fillMaxWidth(),
            )
        } else {
            suggestions.take(3).forEachIndexed { index, s ->
                val weight = when (index) {
                    1 -> 1.2f
                    else -> 1f
                }
                Text(
                    text = s,
                    modifier = Modifier
                        .weight(weight)
                        .clickable { onSuggestion(s) }
                        .padding(4.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (index == 1) FontWeight.SemiBold else FontWeight.Normal,
                    color = colors.keyText,
                )
            }
        }
    }
}
