package com.smartboard.ime.ui.keys

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
    val scale = if (pressed) 0.94f else 1f
    Box(
        modifier = modifier
            .height(46.dp)
            .scale(scale)
            .shadow(2.dp, RoundedCornerShape(8.dp), clip = false)
            .clip(RoundedCornerShape(8.dp))
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
                fontSize = 9.sp,
                modifier = Modifier.align(Alignment.TopEnd),
                color = contentColor.copy(alpha = 0.55f),
            )
        }
        if (icon != null) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(22.dp))
                if (label.isNotBlank()) {
                    Text(
                        text = label,
                        fontSize = 10.sp,
                        color = contentColor.copy(alpha = 0.85f),
                    )
                }
            }
        } else {
            Text(
                text = label,
                fontSize = when {
                    label.length > 14 -> 11.sp
                    label.length > 1 && label.all { !it.isLetterOrDigit() } -> 14.sp
                    else -> 16.sp
                },
                fontWeight = FontWeight.Normal,
                color = contentColor,
            )
        }
    }
}
