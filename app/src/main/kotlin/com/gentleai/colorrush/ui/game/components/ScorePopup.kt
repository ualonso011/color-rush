package com.gentleai.colorrush.ui.game.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.gentleai.colorrush.ui.game.ScoreEffect
import kotlinx.coroutines.delay

/**
 * Floating score popup animation that appears when a cell is tapped.
 *
 * Shows "+1" (green), "-1" (red), or "+3" (yellow) with a fade-in,
 * upward-slide, and fade-out animation.
 *
 * The popup auto-dismisses after [DURATION_MS] milliseconds.
 *
 * @param effect The current [ScoreEffect] to display, or null to hide.
 * @param modifier Optional [Modifier] applied to the root box.
 */
@Composable
fun ScorePopup(
    effect: ScoreEffect?,
    modifier: Modifier = Modifier,
) {
    // Track visibility state manually to control the show duration
    var isVisible by remember { mutableStateOf(false) }
    var currentPoints by remember { mutableStateOf(0) }

    LaunchedEffect(effect) {
        if (effect != null) {
            currentPoints = effect.points
            isVisible = true
            delay(800L)
            isVisible = false
        }
    }

    val text = when {
        currentPoints > 0 -> "+${currentPoints}"
        else -> "$currentPoints"
    }

    val textColor = when {
        currentPoints >= 3 -> Color(0xFFFFEB3B) // Yellow/gold
        currentPoints > 0 -> Color(0xFF4CAF50)  // Green
        else -> Color(0xFFF44336)                // Red
    }

    Box(modifier = modifier) {
        AnimatedVisibility(
            visible = isVisible,
            enter = slideInVertically(
                animationSpec = tween(durationMillis = 300),
                initialOffsetY = { it / 2 },
            ),
            exit = fadeOut(animationSpec = tween(durationMillis = 500)),
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Black,
                color = textColor,
                textAlign = TextAlign.Center,
            )
        }
    }
}
