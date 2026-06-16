package com.gentleai.colorrush.domain.model

/**
 * Immutable snapshot of the full game state, emitted via [StateFlow] by the game engine.
 *
 * @property phase Current game phase (MENU / PLAYING / GAME_OVER).
 * @property grid 9-element list representing the 3×3 game grid.
 * @property score Current player score (floored at 0).
 * @property timeRemaining Seconds remaining in the countdown (decremented every tick).
 * @property totalTime The starting total time for this game — used to compute timer bar percentage.
 */
data class GameState(
    val phase: GamePhase = GamePhase.MENU,
    val grid: List<CellState> = List(9) { CellState(it, CellColor.GRAY) },
    val score: Int = 0,
    val timeRemaining: Float = 30f,
    val totalTime: Float = 30f,
)
