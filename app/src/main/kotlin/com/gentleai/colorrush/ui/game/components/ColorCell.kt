package com.gentleai.colorrush.ui.game.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.gentleai.colorrush.domain.model.CellColor
import com.gentleai.colorrush.ui.theme.CellGray
import com.gentleai.colorrush.ui.theme.CellGreen
import com.gentleai.colorrush.ui.theme.CellRed
import com.gentleai.colorrush.ui.theme.CellYellow

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
 * Returns a glow halo color (low alpha) for the given cell color.
 */
private fun CellColor.glowColor(): Color = when (this) {
    CellColor.GREEN -> CellGreen.copy(alpha = 0.4f)
    CellColor.RED -> CellRed.copy(alpha = 0.4f)
    CellColor.YELLOW -> CellYellow.copy(alpha = 0.4f)
    CellColor.GRAY -> Color.Transparent
}

/**
 * A single cell in the 3×3 game grid with neon arcade styling.
 *
 * Features:
 * - Smooth color transition via [animateColorAsState] with a 300ms tween.
 * - Scale-up tap feedback (1.0 → 1.2 → 1.0) driven by [animateFloatAsState].
 * - Subtle pulsing animation for active cells (1.0 → 1.05 loop).
 * - Neon glow effect using [drawBehind] with blurred halo matching cell color.
 * - Inner gradient overlay for visual depth.
 * - Rounded corners (20.dp) with matching border glow.
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
    val tapScale by animateFloatAsState(
        targetValue = if (isPressed) 1.2f else 1.0f,
        animationSpec = tween(durationMillis = 150),
        label = "cellTapScale",
    )

    // ── Pulse animation for active cells ───────────────────────────────────
    val infiniteTransition = rememberInfiniteTransition(label = "cellPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "cellPulseScale",
    )

    // Combine tap and pulse scale: only pulse when not pressed and enabled
    val combinedScale = if (isPressed || !enabled) tapScale else tapScale * pulseScale

    val cornerRadius = 20.dp
    val glowAlpha = if (enabled) color.glowColor() else Color.Transparent

    Box(
        modifier = modifier
            .padding(6.dp)
            .aspectRatio(1f)
            .scale(combinedScale)
            .clip(RoundedCornerShape(cornerRadius))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        animatedColor,
                        animatedColor.copy(alpha = 0.85f),
                    ),
                ),
                shape = RoundedCornerShape(cornerRadius),
            )
            .drawBehind {
                // Outer glow halo
                if (glowAlpha.alpha > 0f) {
                    drawRoundRect(
                        color = glowAlpha,
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(
                            cornerRadius.toPx(),
                            cornerRadius.toPx(),
                        ),
                        style = Stroke(width = 12f * density),
                        size = size,
                    )
                    // Border glow
                    drawRoundRect(
                        color = animatedColor.copy(alpha = 0.6f),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(
                            cornerRadius.toPx(),
                            cornerRadius.toPx(),
                        ),
                        style = Stroke(width = 2.5f * density),
                        size = size,
                    )
                }
            }
            .then(
                if (enabled) {
                    Modifier.pointerInput(key1 = Unit) {
                        detectTapGestures(
                            onPress = {
                                isPressed = true
                                tryAwaitRelease()
                                isPressed = false
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
        // Inner highlight overlay for depth
        Box(
            modifier = Modifier
                .matchParentSize()
                .padding(3.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.20f),
                            Color.Transparent,
                        ),
                    ),
                    shape = RoundedCornerShape(cornerRadius - 3.dp),
                ),
        )
    }
}
