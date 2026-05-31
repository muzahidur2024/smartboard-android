package com.smartboard.clipboardui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ClearAll
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Pin
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.smartboard.model.ClipboardCategory
import com.smartboard.model.ClipboardEntry
import com.smartboard.ui.components.CategoryFilterChip
import com.smartboard.ui.theme.PanelTopShape
import com.smartboard.ui.theme.SmartBoardThemeColors

@Composable
fun ClipboardPanel(
    items: List<ClipboardEntry>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    showSearch: Boolean,
    onToggleSearch: () -> Unit,
    onClearAll: () -> Unit,
    onClose: () -> Unit,
    onCategorySelected: (ClipboardCategory?) -> Unit,
    selectedCategory: ClipboardCategory?,
    onItemClick: (ClipboardEntry) -> Unit,
    onDeleteItem: (Long) -> Unit,
    onPinToggle: (Long, Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = SmartBoardThemeColors.colors
    val ctx = LocalContext.current
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.clipboardBg, PanelTopShape),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = ctx.getString(R.string.clipboard_title),
                style = MaterialTheme.typography.titleMedium,
            )
            Row {
                IconButton(onClick = onToggleSearch) {
                    Icon(Icons.Rounded.Search, contentDescription = null)
                }
                IconButton(onClick = onClearAll) {
                    Icon(Icons.Rounded.ClearAll, contentDescription = null)
                }
                IconButton(onClick = onClose) {
                    Icon(Icons.Rounded.Close, contentDescription = null)
                }
            }
        }
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(colors.divider),
        )
        if (showSearch) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = {
                    Text(ctx.getString(R.string.clipboard_search_placeholder))
                },
                singleLine = true,
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            CategoryFilterChip(
                label = ctx.getString(R.string.category_all),
                selected = selectedCategory == null,
                onClick = { onCategorySelected(null) },
            )
            ClipboardCategory.entries.filter { it != ClipboardCategory.OTHER }.forEach { cat ->
                CategoryFilterChip(
                    label = cat.name.lowercase().replaceFirstChar { it.uppercase() },
                    selected = selectedCategory == cat,
                    onClick = { onCategorySelected(cat) },
                )
            }
        }
        if (items.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = ctx.getString(R.string.clipboard_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            ) {
                items(items, key = { it.id }) { entry ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onItemClick(entry) }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            Icons.Rounded.Pin,
                            contentDescription = null,
                            tint = if (entry.isPinned) colors.categoryChipText else colors.divider,
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = entry.contentText,
                                maxLines = 2,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                        IconButton(onClick = { onPinToggle(entry.id, !entry.isPinned) }) {
                            Icon(Icons.Rounded.Pin, contentDescription = null)
                        }
                        IconButton(onClick = { onDeleteItem(entry.id) }) {
                            Icon(Icons.Rounded.Delete, contentDescription = null)
                        }
                    }
                    Spacer(
                        Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(colors.divider),
                    )
                }
            }
        }
    }
}
