package com.gentleai.colorrush.ui.game.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gentleai.colorrush.ui.theme.TimerDanger
import com.gentleai.colorrush.ui.theme.TimerNormal
import com.gentleai.colorrush.ui.theme.TimerWarning

/**
 * Animated neon timer bar displayed at the top of the game screen.
 *
 * Uses custom [Canvas] drawing instead of [LinearProgressIndicator] for full
 * control over glow effects, color transitions, and urgency animations.
 *
 * Visual states:
 * - Above 50% : cyan neon glow
 * - 20%–50%   : yellow neon glow
 * - Below 20% : red neon glow + pulsing urgency effect
 *
 * @param timeRemaining Current seconds remaining (0f..[totalTime]).
 * @param totalTime Total seconds at game start (used to compute progress).
 * @param modifier Optional [Modifier] applied to the root column.
 */
@Composable
fun TimerBar(
    timeRemaining: Float,
    totalTime: Float,
    modifier: Modifier = Modifier,
) {
    val progress = if (totalTime > 0f) {
        (timeRemaining / totalTime).coerceIn(0f, 1f)
    } else {
        0f
    }

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 200),
        label = "timerProgress",
    )

    val displaySeconds = timeRemaining.toInt()

    // ── Timer color based on urgency ──────────────────────────────────────
    val timerColor = when {
        animatedProgress > 0.5f -> TimerNormal
        animatedProgress > 0.2f -> TimerWarning
        else -> TimerDanger
    }

    // ── Urgency pulse (only active when time < 10 seconds) ────────────────
    val isUrgent = timeRemaining < 10f
    val infiniteTransition = rememberInfiniteTransition(label = "timerPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (isUrgent) 400 else 1000,
                easing = LinearEasing,
            ),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "timerPulseAlpha",
    )

    // ── Glow halo alpha ───────────────────────────────────────────────────
    val glowAlpha = if (isUrgent) 0.3f * pulseAlpha else 0.2f

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // ── Numeric countdown with glow halo ──────────────────────────────
        Box(contentAlignment = Alignment.Center) {
            // Glow halo behind the number
            Text(
                text = "$displaySeconds",
                fontSize = 64.sp,
                fontWeight = FontWeight.Black,
                color = timerColor.copy(alpha = glowAlpha * 0.5f),
                textAlign = TextAlign.Center,
                modifier = Modifier.size(120.dp),
            )
            // Actual number
            Text(
                text = "$displaySeconds",
                fontSize = 48.sp,
                fontWeight = FontWeight.Black,
                color = timerColor,
                textAlign = TextAlign.Center,
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // ── Custom Canvas progress bar with neon glow ─────────────────────
        Canvas(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(16.dp),
        ) {
            val barHeight = size.height
            val barWidth = size.width
            val cornerR = barHeight / 2f

            // ── Track background ────────────────────────────────────────
            drawRoundRect(
                color = Color(0xFF1A1F3A),
                cornerRadius = CornerRadius(cornerR, cornerR),
                size = Size(barWidth, barHeight),
            )

            // ── Progress fill ───────────────────────────────────────────
            val fillWidth = barWidth * animatedProgress
            if (fillWidth > 0f) {
                // Gradient along the bar
                drawRoundRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            timerColor.copy(alpha = 0.8f),
                            timerColor,
                        ),
                    ),
                    cornerRadius = CornerRadius(cornerR, cornerR),
                    size = Size(fillWidth, barHeight),
                )

                // ── Inner glow (brighter center line) ─────────────────
                drawRoundRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            timerColor.copy(alpha = 0.3f),
                            timerColor.copy(alpha = 0.6f),
                        ),
                    ),
                    cornerRadius = CornerRadius(cornerR, cornerR),
                    size = Size(fillWidth, barHeight * 0.5f),
                    topLeft = Offset(0f, barHeight * 0.25f),
                )

                // ── Outer glow halo ────────────────────────────────────
                drawRoundRect(
                    color = timerColor.copy(alpha = glowAlpha),
                    cornerRadius = CornerRadius(cornerR, cornerR),
                    size = Size(fillWidth + 8f, barHeight + 8f),
                    topLeft = Offset(-4f, -4f),
                    style = Stroke(width = 4f),
                )
            }
        }
    }
}
