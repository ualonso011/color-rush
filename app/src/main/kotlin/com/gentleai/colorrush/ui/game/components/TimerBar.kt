package com.gentleai.colorrush.ui.game.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Animated timer bar displayed at the top of the game screen.
 *
 * Shows a [LinearProgressIndicator] that smoothly animates from 1.0 → 0.0
 * as [timeRemaining] decreases, alongside a numeric countdown label.
 *
 * Visual states:
 * - Above 50% : green
 * - 20%–50%   : yellow/orange
 * - Below 20% : red (time is running out)
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

    // Smooth progress animation
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 200),
        label = "timerProgress",
    )

    val displaySeconds = timeRemaining.toInt()
    val timerColor = when {
        animatedProgress > 0.5f -> MaterialTheme.colorScheme.tertiary
        animatedProgress > 0.2f -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.error
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // ── Numeric countdown ─────────────────────────────────────────────
        Text(
            text = "$displaySeconds",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Black,
            color = timerColor,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(4.dp))

        // ── Progress bar ──────────────────────────────────────────────────
        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(12.dp)
                .clip(RoundedCornerShape(6.dp)),
            color = timerColor,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            strokeCap = StrokeCap.Round,
        )
    }
}
