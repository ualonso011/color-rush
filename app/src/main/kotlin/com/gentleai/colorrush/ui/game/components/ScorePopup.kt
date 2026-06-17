package com.gentleai.colorrush.ui.game.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gentleai.colorrush.ui.game.ScoreEffect
import kotlinx.coroutines.delay

/**
 * Floating score popup animation that appears when a cell is tapped.
 *
 * Features dramatic arcade-style animations:
 * - Overshoot spring bounce-in scale
 * - Large neon text with glow effect
 * - Clock icon with neon halo for time bonuses
 * - Upward slide with fade-out
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
    var isVisible by remember { mutableStateOf(false) }
    var currentPoints by remember { mutableStateOf(0) }
    var currentTimeBonus by remember { mutableStateOf(0f) }

    LaunchedEffect(effect) {
        if (effect != null) {
            currentPoints = effect.points
            currentTimeBonus = effect.timeBonus
            isVisible = true
            delay(1000L)
            isVisible = false
        }
    }

    val isTimeBonus = currentTimeBonus > 0f

    val text = when {
        isTimeBonus -> "${currentPoints}"
        currentPoints > 0 -> "+${currentPoints}"
        else -> "$currentPoints"
    }

    val textColor = when {
        isTimeBonus -> Color(0xFFFFEA00) // Neon yellow
        currentPoints > 0 -> Color(0xFF00E676) // Neon green
        else -> Color(0xFFFF1744) // Neon red
    }

    // Glow color matching the text
    val glowColor = textColor.copy(alpha = 0.25f)

    Box(modifier = modifier) {
        AnimatedVisibility(
            visible = isVisible,
            enter = slideInVertically(
                animationSpec = tween(durationMillis = 400),
                initialOffsetY = { it },
            ) + fadeIn(
                animationSpec = tween(durationMillis = 200),
            ) + scaleIn(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow,
                ),
                initialScale = 0.3f,
            ),
            exit = fadeOut(animationSpec = tween(durationMillis = 500)),
        ) {
            if (isTimeBonus) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        // Glow behind text
                        Text(
                            text = text,
                            fontSize = 72.sp,
                            fontWeight = FontWeight.Black,
                            color = glowColor,
                            textAlign = TextAlign.Center,
                        )
                        Text(
                            text = text,
                            style = androidx.compose.material3.MaterialTheme.typography.displayLarge,
                            fontWeight = FontWeight.Black,
                            color = textColor,
                            textAlign = TextAlign.Center,
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    // Icon with glow
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.Timer,
                            contentDescription = "Time bonus",
                            tint = glowColor,
                            modifier = Modifier.size(80.dp),
                        )
                        Icon(
                            imageVector = Icons.Filled.Timer,
                            contentDescription = "Time bonus",
                            tint = textColor,
                            modifier = Modifier.size(56.dp),
                        )
                    }
                }
            } else {
                Box(contentAlignment = Alignment.Center) {
                    // Glow layer
                    Text(
                        text = text,
                        fontSize = 72.sp,
                        fontWeight = FontWeight.Black,
                        color = glowColor,
                        textAlign = TextAlign.Center,
                    )
                    // Foreground text
                    Text(
                        text = text,
                        style = androidx.compose.material3.MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.Black,
                        color = textColor,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}
