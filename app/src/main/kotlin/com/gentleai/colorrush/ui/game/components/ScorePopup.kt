package com.gentleai.colorrush.ui.game.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.offset
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gentleai.colorrush.ui.game.ScoreEffect
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

/**
 * Floating score popup animation that appears when a cell is tapped.
 *
 * Features Mario Bros coin-style animation:
 * - Spawns from the tapped cell position
 * - Slides upward with fade-out
 * - Smaller, compact text size
 * - Clock icon for time bonuses
 *
 * The popup auto-dismisses after animation completes.
 *
 * @param effect The current [ScoreEffect] to display, or null to hide.
 * @param cellSize Size of each cell in the grid (in dp).
 * @param gridWidth Total width of the grid (in dp).
 * @param gridTopOffset Vertical offset from top to the grid start (in dp).
 * @param modifier Optional [Modifier] applied to the root box.
 */
@Composable
fun ScorePopup(
    effect: ScoreEffect?,
    cellSize: Float,
    gridWidth: Float,
    gridTopOffset: Float,
    modifier: Modifier = Modifier,
) {
    var isVisible by remember { mutableStateOf(false) }
    var currentPoints by remember { mutableStateOf(0) }
    var currentTimeBonus by remember { mutableStateOf(0f) }
    var cellIndex by remember { mutableStateOf(0) }

    // Animation for upward movement
    val offsetY = remember { Animatable(0f) }
    
    LaunchedEffect(effect) {
        if (effect != null) {
            currentPoints = effect.points
            currentTimeBonus = effect.timeBonus
            cellIndex = effect.cellIndex
            isVisible = true
            
            // Reset and animate upward
            offsetY.snapTo(0f)
            offsetY.animateTo(
                targetValue = -80f, // Move up 80dp
                animationSpec = tween(durationMillis = 600)
            )
            
            // Wait for remaining display time before hiding
            delay(400L)
            isVisible = false
        } else {
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

    // Calculate cell position based on index (0-8)
    val row = cellIndex / 3
    val col = cellIndex % 3
    
    // Calculate offsets
    val gridLeftOffset = (gridWidth - (cellSize * 3)) / 2
    val cellX = gridLeftOffset + (col * cellSize) + (cellSize / 2)
    val cellY = gridTopOffset + (row * cellSize) + (cellSize / 2)

    val density = LocalDensity.current

    Box(modifier = modifier) {
        if (isVisible) {
            Box(
                modifier = Modifier.offset {
                    with(density) {
                        IntOffset(
                            x = cellX.roundToInt().dp.roundToPx(),
                            y = (cellY + offsetY.value).roundToInt().dp.roundToPx()
                        )
                    }
                },
            ) {
                if (isTimeBonus) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = text,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = textColor,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Filled.Timer,
                            contentDescription = "Time bonus",
                            tint = textColor,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                } else {
                    Text(
                        text = text,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = textColor,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}
