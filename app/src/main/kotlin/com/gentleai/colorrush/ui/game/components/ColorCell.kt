package com.gentleai.colorrush.ui.game.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.gentleai.colorrush.domain.model.CellColor
import com.gentleai.colorrush.ui.theme.CellGray
import com.gentleai.colorrush.ui.theme.CellGreen
import com.gentleai.colorrush.ui.theme.CellRed
import com.gentleai.colorrush.ui.theme.CellYellow
import kotlinx.coroutines.delay

/**
 * Maps a domain [CellColor] to the corresponding Compose [Color].
 */
private fun CellColor.toComposeColor(): Color = when (this) {
    CellColor.GREEN -> CellGreen
    CellColor.RED -> CellRed
    CellColor.YELLOW -> CellYellow
    CellColor.GRAY -> CellGray
}

/**
 * A single cell in the 3×3 game grid.
 *
 * Features:
 * - Smooth color transition via [animateColorAsState] with a 300ms tween.
 * - Scale-up tap feedback (1.0 → 1.2 → 1.0) driven by [animateFloatAsState].
 * - Rounded corners with shadow elevation for depth.
 * - Non-interactive when [enabled] is false.
 *
 * @param color The current [CellColor] to display.
 * @param onTap Callback invoked when the cell is tapped (only if [enabled]).
 * @param enabled Whether the cell responds to taps.
 * @param modifier Optional [Modifier] applied to the cell container.
 */
@Composable
fun ColorCell(
    color: CellColor,
    onTap: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    // ── Color animation ────────────────────────────────────────────────────
    val targetColor = color.toComposeColor()
    val animatedColor by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(durationMillis = 300),
        label = "cellColor",
    )

    // ── Tap scale animation ────────────────────────────────────────────────
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 1.2f else 1.0f,
        animationSpec = tween(durationMillis = 150),
        label = "cellScale",
    )

    // Reset press state after the scale animation completes
    LaunchedEffect(isPressed) {
        if (isPressed) {
            delay(150L)
            isPressed = false
        }
    }

    Box(
        modifier = modifier
            .padding(4.dp)
            .aspectRatio(1f)
            .clip(RoundedCornerShape(16.dp))
            .scale(scale)
            .graphicsLayer {
                shadowElevation = if (enabled) 6f else 2f
                shape = RoundedCornerShape(16.dp)
                clip = true
            }
            .background(
                color = animatedColor,
                shape = RoundedCornerShape(16.dp),
            )
            .then(
                if (enabled) {
                    Modifier.pointerInput(key1 = Unit) {
                        detectTapGestures(
                            onPress = {
                                isPressed = true
                                tryAwaitRelease()
                            },
                            onTap = { onTap() },
                        )
                    }
                } else {
                    Modifier
                }
            ),
        contentAlignment = Alignment.Center,
    ) {
        // Inner highlight for visual depth
        Box(
            modifier = Modifier
                .matchParentSize()
                .padding(2.dp)
                .background(
                    color = Color.White.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(14.dp),
                ),
        )
    }
}
