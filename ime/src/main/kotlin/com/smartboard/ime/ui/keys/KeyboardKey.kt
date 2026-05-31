package com.smartboard.ime.ui.keys

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * A single keyboard key styled to match Gboard's proportions:
 * - 52dp row height (Gboard uses 52-58dp)
 * - 22sp letter font size (Gboard uses ~22sp for single characters)
 * - 12dp corner radius, subtle elevation
 * - 3dp horizontal padding between keys (handled by grid spacer)
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun KeyboardKey(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    secondaryLabel: String? = null,
    emphasized: Boolean = false,
    hapticEnabled: Boolean,
    onHapticKey: () -> Unit,
    onHapticSpecial: () -> Unit,
    background: Color,
    contentColor: Color,
    icon: ImageVector? = null,
    onLongClick: (() -> Unit)? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale = if (pressed) 0.95f else 1f

    Box(
        modifier = modifier
            .height(52.dp)
            .padding(horizontal = 2.dp, vertical = 3.dp)
            .scale(scale)
            .shadow(1.dp, RoundedCornerShape(12.dp), clip = false)
            .clip(RoundedCornerShape(12.dp))
            .background(background)
            .combinedClickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                onClick = {
                    if (emphasized) onHapticSpecial() else onHapticKey()
                    onClick()
                },
                onLongClick = onLongClick?.let {
                    {
                        onHapticSpecial()
                        it()
                    }
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (secondaryLabel != null) {
            Text(
                text = secondaryLabel,
                fontSize = 10.sp,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(end = 4.dp, top = 2.dp),
                color = contentColor.copy(alpha = 0.5f),
            )
        }
        if (icon != null) {
            Icon(
                icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(24.dp),
            )
        } else {
            Text(
                text = label,
                fontSize = when {
                    // Single letter keys (a-z, A-Z, digits) - large like Gboard
                    label.length == 1 -> 22.sp
                    // Short labels (2-3 chars like "?!" or shift symbols)
                    label.length <= 3 -> 18.sp
                    // Medium labels like "space", "ABC", "?123"
                    label.length <= 6 -> 14.sp
                    // Long labels (language names on space bar)
                    label.length <= 12 -> 12.sp
                    else -> 11.sp
                },
                fontWeight = if (label.length == 1) FontWeight.Normal else FontWeight.Medium,
                color = contentColor,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Clip,
            )
        }
    }
}
