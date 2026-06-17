package com.gentleai.colorrush.ui.game

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.gentleai.colorrush.domain.model.GamePhase
import com.gentleai.colorrush.ui.game.components.ColorCell
import com.gentleai.colorrush.ui.game.components.ScorePopup
import com.gentleai.colorrush.ui.game.components.TimerBar
import com.gentleai.colorrush.ui.theme.NeonCyan
import kotlinx.coroutines.delay

/**
 * Main game screen composable with immersive arcade styling.
 *
 * Layout (top to bottom):
 * 1. Score display (large neon text)
 * 2. Timer bar (neon animated progress + countdown)
 * 3. 3×3 game grid (tappable neon color cells)
 * 4. Score popup overlay (floating +1/-1/+3 animation)
 *
 * Background features a gradient with subtle grid pattern.
 *
 * @param onGameOver Callback with the final score when the game ends.
 * @param onBackToMain Callback when the user wants to return to the main menu.
 * @param modifier Optional [Modifier] applied to the root container.
 * @param viewModel Injected [GameViewModel] via Hilt.
 */
@Composable
fun GameScreen(
    onGameOver: (Int) -> Unit,
    onBackToMain: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: GameViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    // ── Lifecycle observer for audio ────────────────────────────────────────
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> viewModel.resumeAudio()
                Lifecycle.Event.ON_PAUSE -> viewModel.pauseAudio()
                else -> { /* no-op */ }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // ── Navigation on game over ─────────────────────────────────────────────
    LaunchedEffect(state.phase) {
        if (state.phase == GamePhase.GAME_OVER) {
            delay(200L)
            onGameOver(state.score)
        }
    }

    // ── Back-press handling ─────────────────────────────────────────────────
    BackHandler {
        viewModel.stopGame()
        onBackToMain()
    }

    // ── Score popup effect collection ───────────────────────────────────────
    var currentScoreEffect by remember { mutableStateOf<ScoreEffect?>(null) }
    LaunchedEffect(Unit) {
        viewModel.scoreEffect.collect { effect ->
            currentScoreEffect = effect
            delay(800L)
            currentScoreEffect = null
        }
    }

    // ── UI Layout ───────────────────────────────────────────────────────────
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0A0E27),
                        Color(0xFF0D1117),
                        Color(0xFF0A0E27),
                    ),
                ),
            )
            .drawBehind {
                // Subtle grid pattern
                val gridSpacing = 60f * density
                val gridColor = Color(0x08FFFFFF)
                var x = 0f
                while (x < size.width) {
                    drawLine(gridColor, Offset(x, 0f), Offset(x, size.height))
                    x += gridSpacing
                }
                var y = 0f
                while (y < size.height) {
                    drawLine(gridColor, Offset(0f, y), Offset(size.width, y))
                    y += gridSpacing
                }
            },
    ) {
        // Calculate grid dimensions for popup positioning
        val screenWidth = LocalConfiguration.current.screenWidthDp.toFloat()
        val gridWidth = screenWidth * 0.8f // Grid uses 80% of screen width
        val cellSize = gridWidth / 3f
        val gridTopOffset = 8f + 4f + 20f + 32f + 24f // Score label + spacing + timer + spacing + top padding

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // ── Score display (prominent neon style) ──────────────────────
            Text(
                text = androidx.compose.ui.res.stringResource(
                    com.gentleai.colorrush.R.string.score_label
                ),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 4.sp,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${state.score}",
                fontSize = 48.sp,
                fontWeight = FontWeight.Black,
                color = NeonCyan,
                textAlign = TextAlign.Center,
                letterSpacing = 2.sp,
            )

            Spacer(modifier = Modifier.height(20.dp))

            // ── Timer bar ─────────────────────────────────────────────────
            TimerBar(
                timeRemaining = state.timeRemaining,
                totalTime = state.totalTime,
            )

            Spacer(modifier = Modifier.height(32.dp))

            // ── 3×3 Game grid ─────────────────────────────────────────────
            GameGrid(
                grid = state.grid,
                onCellTap = { index -> viewModel.onCellTap(index) },
                enabled = state.phase == GamePhase.PLAYING,
            )

            Spacer(modifier = Modifier.weight(1f))

            // ── Phase indicator ───────────────────────────────────────────
            if (state.phase != GamePhase.PLAYING) {
                Text(
                    text = when (state.phase) {
                        GamePhase.MENU -> ""
                        GamePhase.GAME_OVER -> androidx.compose.ui.res.stringResource(
                            com.gentleai.colorrush.R.string.game_over_title
                        )
                        else -> ""
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }

        // ── Score popup overlay (Mario Bros coin style) ────────────────────
        ScorePopup(
            effect = currentScoreEffect,
            cellSize = cellSize,
            gridWidth = gridWidth,
            gridTopOffset = gridTopOffset,
            modifier = Modifier.align(Alignment.TopStart),
        )
    }
}

/**
 * Renders a 3×3 grid of [ColorCell] composables.
 *
 * Uses two [Row] wrappers because the grid is fixed-size (3×3).
 * Each cell is equally weighted and maintains a 1:1 aspect ratio.
 *
 * @param grid The 9-element list of [CellState] (row-major order).
 * @param onCellTap Callback with the cell index (0..8).
 * @param enabled Whether cells respond to taps.
 */
@Composable
private fun GameGrid(
    grid: List<com.gentleai.colorrush.domain.model.CellState>,
    onCellTap: (Int) -> Unit,
    enabled: Boolean,
) {
    if (grid.size != 9) return

    Column(
        modifier = Modifier
            .fillMaxWidth(0.8f),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        for (row in 0..2) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(0.dp),
            ) {
                for (col in 0..2) {
                    val index = row * 3 + col
                    val cell = grid[index]
                    ColorCell(
                        color = cell.color,
                        onTap = { onCellTap(index) },
                        enabled = enabled,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}
