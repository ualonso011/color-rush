package com.gentleai.colorrush.domain.engine

import com.gentleai.colorrush.domain.model.CellColor
import com.gentleai.colorrush.domain.model.CellState
import com.gentleai.colorrush.domain.model.GamePhase
import com.gentleai.colorrush.domain.model.GameState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

/**
 * Pure Kotlin game engine — no Android dependencies.
 *
 * Drives the game via a reactive [StateFlow]<[GameState]> updated by:
 * - [startGame] — initialises grid, score, and timer.
 * - [tapCell]   — processes a cell tap with scoring and timer bonuses.
 * - [runGameLoop] — coroutine-based tick loop that decrements the timer.
 * - [reset]     — returns to initial [GamePhase.MENU] state.
 *
 * The game loop is a suspend function meant to be launched from a [CoroutineScope]
 * (e.g. `viewModelScope`), allowing virtual time advancement with `StandardTestDispatcher`
 * during unit tests.
 */
class GameEngine @Inject constructor(
    private val colorSpawner: ColorSpawner,
) {
    // ── Constants ──────────────────────────────────────────────────────────

    companion object {
        /** Number of cells in the 3×3 grid. */
        const val GRID_SIZE = 9

        /** Starting countdown duration in seconds. */
        const val INITIAL_TIME = 30f

        /** Maximum possible countdown value (yellow taps cannot exceed this). */
        const val MAX_TIME = 60f

        /** Tick interval of the game loop in milliseconds. */
        const val TICK_INTERVAL_MS = 100L

        /** Time decremented per tick, in seconds. */
        const val TICK_TIME_DECREMENT = 0.1f

        // Scoring values
        const val GREEN_POINTS = 1
        const val RED_POINTS = -1
        const val YELLOW_POINTS = 3

        /** Extra seconds awarded for tapping a YELLOW cell. */
        const val YELLOW_TIME_BONUS = 3f
    }

    // ── Reactive state ────────────────────────────────────────────────────

    private val _state = MutableStateFlow(GameState())
    val state: StateFlow<GameState> = _state.asStateFlow()

    // ── Public API ─────────────────────────────────────────────────────────

    /**
     * Starts a new game: sets phase to [GamePhase.PLAYING], populates the full
     * 3×3 grid with random colours, resets score to 0 and timer to [INITIAL_TIME].
     */
    fun startGame() {
        _state.value = GameState(
            phase = GamePhase.PLAYING,
            grid = List(GRID_SIZE) { index -> CellState(index, colorSpawner.spawn()) },
            score = 0,
            timeRemaining = INITIAL_TIME,
            totalTime = INITIAL_TIME,
        )
    }

    /**
     * Processes a cell tap at the given [index].
     *
     * - If the game is not in [GamePhase.PLAYING], returns [TapResult.Missed].
     * - If the cell is [CellColor.GRAY], returns [TapResult.Missed].
     * - Otherwise computes score delta and optional time bonus, updates state,
     *   spawns a new colour for the tapped cell, and returns [TapResult.Scored].
     *
     * @param index Cell position (0..8, row-major order).
     * @throws IllegalArgumentException if [index] is out of bounds.
     */
    fun tapCell(index: Int): TapResult {
        val current = _state.value
        if (current.phase != GamePhase.PLAYING) return TapResult.Missed
        require(index in 0 until GRID_SIZE) { "Cell index out of bounds: $index (grid size: $GRID_SIZE)" }

        val cell = current.grid[index]
        if (cell.color == CellColor.GRAY) return TapResult.Missed

        val points = when (cell.color) {
            CellColor.GREEN -> GREEN_POINTS
            CellColor.RED -> RED_POINTS
            CellColor.YELLOW -> YELLOW_POINTS
            CellColor.GRAY -> 0 // handled above, keeps the when exhaustive
        }
        val timeBonus = if (cell.color == CellColor.YELLOW) YELLOW_TIME_BONUS else 0f

        val newScore = maxOf(0, current.score + points)
        val newTime = minOf(MAX_TIME, current.timeRemaining + timeBonus)
        val newGrid = current.grid.map { cs ->
            if (cs.index == index) CellState(index, colorSpawner.spawn()) else cs
        }

        _state.value = current.copy(
            score = newScore,
            timeRemaining = newTime,
            grid = newGrid,
        )

        return TapResult.Scored(points = points, timeBonus = timeBonus)
    }

    /**
     * Resets the engine to its initial [GamePhase.MENU] state.
     */
    fun reset() {
        _state.value = GameState()
    }

    // ── Game loop (internal — called from ViewModel/Test scope) ────────────

    /**
     * Coroutine-based game loop. Decrements the timer every [TICK_INTERVAL_MS]
     * and transitions to [GamePhase.GAME_OVER] when the timer reaches zero.
     *
     * Must be launched from a [CoroutineScope]; the loop automatically
     * terminates when the phase changes away from [GamePhase.PLAYING].
     */
    internal suspend fun runGameLoop() {
        while (_state.value.phase == GamePhase.PLAYING) {
            delay(TICK_INTERVAL_MS)
            tick()
        }
    }

    // ── Internal ──────────────────────────────────────────────────────────

    /**
     * Processes a single tick: decrements the countdown timer by
     * [TICK_TIME_DECREMENT] and transitions to GAME_OVER if time runs out.
     */
    private fun tick() {
        _state.update { current ->
            if (current.phase != GamePhase.PLAYING) return@update current

            val newTime = current.timeRemaining - TICK_TIME_DECREMENT
            if (newTime <= 0f) {
                current.copy(timeRemaining = 0f, phase = GamePhase.GAME_OVER)
            } else {
                current.copy(timeRemaining = newTime)
            }
        }
    }
}
