package com.gentleai.colorrush.ui.game

import com.gentleai.colorrush.domain.model.CellState
import com.gentleai.colorrush.domain.model.GamePhase

/**
 * UI-ready snapshot of the game state consumed by [GameScreen].
 *
 * Maps directly from [com.gentleai.colorrush.domain.model.GameState] but lives
 * in the UI layer to avoid domain → UI coupling and to allow UI-only fields.
 *
 * @property phase Current game phase (determines interactivity).
 * @property grid 9-element list for the 3×3 game grid.
 * @property score Current player score (floored at 0).
 * @property timeRemaining Seconds remaining in the countdown.
 * @property totalTime Total seconds at game start (for timer bar percentage).
 */
data class GameUiState(
    val phase: GamePhase = GamePhase.MENU,
    val grid: List<CellState> = emptyList(),
    val score: Int = 0,
    val timeRemaining: Float = 30f,
    val totalTime: Float = 30f,
)

/**
 * One-shot effect emitted by [GameViewModel] when the player taps a cell.
 *
 * Used to trigger the [ScorePopup] animation in the UI.
 *
 * @property points Points awarded (positive) or deducted (negative).
 * @property cellIndex Index of the tapped cell.
 */
data class ScoreEffect(
    val points: Int,
    val cellIndex: Int,
)
