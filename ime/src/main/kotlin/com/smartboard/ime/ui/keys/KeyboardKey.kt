package com.smartboard.ime.ui.keys

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * A single keyboard key styled to match Gboard's proportions.
 *
 * When [repeatable] is true (used for the backspace key), pressing and holding triggers
 * [onRepeat] continuously with an increasing tick counter, so callers can delete a character
 * per tick and switch to word-deletion once held long enough — clearing text quickly like Gboard.
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
    keyHeight: Dp = 52.dp,
    repeatable: Boolean = false,
    onRepeat: ((Int) -> Unit)? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    var heldDown by remember { mutableStateOf(false) }
    val scale = if (pressed || heldDown) 0.95f else 1f
    val scope = rememberCoroutineScope()

    val behaviorModifier = if (repeatable && onRepeat != null) {
        Modifier.pointerInput(Unit) {
            awaitEachGesture {
                awaitFirstDown(requireUnconsumed = false)
                heldDown = true
                val job = scope.launch {
                    var tick = 0
                    onHapticSpecial()
                    onRepeat(tick) // immediate first delete (also covers a quick tap)
                    tick++
                    delay(400) // initial hold delay before auto-repeat kicks in
                    var interval = 55L
                    while (isActive) {
                        onRepeat(tick)
                        tick++
                        // Accelerate over time so longer holds clear faster.
                        if (tick % 4 == 0 && interval > 16L) interval -= 6L
                        delay(interval)
                    }
                }
                waitForUpOrCancellation()
                job.cancel()
                heldDown = false
            }
        }
    } else {
        Modifier.combinedClickable(
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
        )
    }

    Box(
        modifier = modifier
            .height(keyHeight)
            .padding(horizontal = 2.dp, vertical = 3.dp)
            .scale(scale)
            .shadow(1.dp, RoundedCornerShape(12.dp), clip = false)
            .clip(RoundedCornerShape(12.dp))
            .background(background)
            .then(behaviorModifier),
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
                    label.length == 1 -> 22.sp
                    label.length <= 3 -> 18.sp
                    label.length <= 6 -> 14.sp
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
