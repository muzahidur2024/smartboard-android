package com.smartboard.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.smartboard.ui.theme.SmartBoardThemeColors

@Composable
fun CategoryFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = SmartBoardThemeColors.colors
    AssistChip(
        onClick = onClick,
        label = { Text(label, style = MaterialTheme.typography.labelMedium) },
        modifier = modifier.padding(end = 6.dp),
        colors = AssistChipDefaults.assistChipColors(
            containerColor = if (selected) c.categoryChipBg else MaterialTheme.colorScheme.surface,
            labelColor = if (selected) c.categoryChipText else MaterialTheme.colorScheme.onSurface,
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) c.categoryChipText else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
        ),
    )
}
