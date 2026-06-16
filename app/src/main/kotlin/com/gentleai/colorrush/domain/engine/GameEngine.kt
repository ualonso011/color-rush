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
import kotlin.random.Random
import javax.inject.Inject

/**
 * Pure Kotlin game engine — no Android dependencies.
 *
 * Drives the game via a reactive [StateFlow]<[GameState]> updated by:
 * - [startGame] — initialises grid (all GRAY), score, and timer.
 * - [tapCell]   — processes a cell tap with scoring and timer bonuses.
 * - [runGameLoop] — coroutine-based tick loop that decrements the timer,
 *   spawns new colors, and expires old ones.
 * - [reset]     — returns to initial [GamePhase.MENU] state.
 *
 * Game mechanics:
 * - Grid starts with all cells GRAY (inactive).
 * - Colors spawn dynamically (max 1-2 at a time) in random positions.
 * - Each color has a random lifetime before it auto-disappears.
 * - Tapping a colored cell scores points and returns it to GRAY.
 * - Color spawn probabilities: Green 45%, Red 35%, Yellow 20%.
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

        // Color spawning mechanics
        /** Maximum number of active colors at any time. */
        const val MAX_ACTIVE_COLORS = 2

        /** Minimum lifetime for a color in milliseconds. */
        const val MIN_LIFETIME_MS = 1500L

        /** Maximum lifetime for a color in milliseconds. */
        const val MAX_LIFETIME_MS = 3500L

        /** Probability of spawning a new color per tick (when under max). */
        const val SPAWN_PROBABILITY = 0.03f // ~3% per tick = ~1 spawn every 3.3 seconds
    }

    // ── Reactive state ────────────────────────────────────────────────────

    private val _state = MutableStateFlow(GameState())
    val state: StateFlow<GameState> = _state.asStateFlow()

    private val random = Random.Default

    // ── Public API ─────────────────────────────────────────────────────────

    /**
     * Starts a new game: sets phase to [GamePhase.PLAYING], initialises all
     * cells to GRAY (inactive), resets score to 0 and timer to [INITIAL_TIME].
     */
    fun startGame() {
        _state.value = GameState(
            phase = GamePhase.PLAYING,
            grid = List(GRID_SIZE) { index -> CellState(index, CellColor.GRAY) },
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
     *   and returns the cell to GRAY (no immediate spawn).
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
            if (cs.index == index) CellState(index, CellColor.GRAY) else cs
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
     * Coroutine-based game loop. Decrements the timer every [TICK_INTERVAL_MS],
     * spawns new colors, expires old ones, and transitions to [GamePhase.GAME_OVER]
     * when the timer reaches zero.
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
     * Processes a single tick:
     * 1. Decrements the countdown timer by [TICK_TIME_DECREMENT].
     * 2. Expires colors that exceeded their lifetime.
     * 3. Spawns new colors if under [MAX_ACTIVE_COLORS] and random chance hits.
     * 4. Transitions to GAME_OVER if time runs out.
     */
    private fun tick() {
        _state.update { current ->
            if (current.phase != GamePhase.PLAYING) return@update current

            // 1. Decrement timer
            val newTime = current.timeRemaining - TICK_TIME_DECREMENT
            if (newTime <= 0f) {
                return@update current.copy(timeRemaining = 0f, phase = GamePhase.GAME_OVER)
            }

            // 2. Update lifetimes and expire old colors
            var grid = current.grid.map { cell ->
                if (cell.color == CellColor.GRAY) {
                    cell
                } else {
                    val newLifetime = cell.lifetimeMs + TICK_INTERVAL_MS
                    if (newLifetime >= cell.maxLifetimeMs) {
                        // Color expired — return to GRAY
                        CellState(cell.index, CellColor.GRAY)
                    } else {
                        cell.copy(lifetimeMs = newLifetime)
                    }
                }
            }

            // 3. Spawn new colors if under max
            val activeCount = grid.count { it.color != CellColor.GRAY }
            if (activeCount < MAX_ACTIVE_COLORS && random.nextFloat() < SPAWN_PROBABILITY) {
                val grayCells = grid.filter { it.color == CellColor.GRAY }
                if (grayCells.isNotEmpty()) {
                    val targetCell = grayCells[random.nextInt(grayCells.size)]
                    val newColor = colorSpawner.spawn()
                    val lifetime = random.nextLong(MIN_LIFETIME_MS, MAX_LIFETIME_MS + 1)
                    grid = grid.map { cell ->
                        if (cell.index == targetCell.index) {
                            CellState(cell.index, newColor, 0L, lifetime)
                        } else {
                            cell
                        }
                    }
                }
            }

            current.copy(timeRemaining = newTime, grid = grid)
        }
    }
}
