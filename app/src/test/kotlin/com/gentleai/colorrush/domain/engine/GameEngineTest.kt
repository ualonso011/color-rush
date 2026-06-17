package com.gentleai.colorrush.domain.engine

import com.gentleai.colorrush.domain.engine.GameEngine.Companion.GRID_SIZE
import com.gentleai.colorrush.domain.engine.GameEngine.Companion.INITIAL_TIME
import com.gentleai.colorrush.domain.engine.GameEngine.Companion.MAX_TIME
import com.gentleai.colorrush.domain.engine.GameEngine.Companion.TICK_INTERVAL_MS
import com.gentleai.colorrush.domain.engine.GameEngine.Companion.TICK_TIME_DECREMENT
import com.gentleai.colorrush.domain.engine.GameEngine.Companion.YELLOW_TIME_BONUS_MIN
import com.gentleai.colorrush.domain.engine.GameEngine.Companion.YELLOW_TIME_BONUS_MAX
import com.gentleai.colorrush.domain.model.CellColor
import com.gentleai.colorrush.domain.model.CellState
import com.gentleai.colorrush.domain.model.GamePhase
import com.gentleai.colorrush.domain.model.GameState
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * Unit tests for [GameEngine] — pure Kotlin game logic, no Android dependencies.
 *
 * Uses [StandardTestDispatcher] to advance virtual time without real delays.
 */
class GameEngineTest {

    private lateinit var engine: GameEngine
    private lateinit var testScope: TestScope

    /** A [ColorSpawner] that always returns GREEN for deterministic tests. */
    private val greenSpawner = ColorSpawner(
        greenProbability = 1.0f,
        redProbability = 0.0f,
        yellowProbability = 0.0f,
    )

    /** A [ColorSpawner] that always returns YELLOW. */
    private val yellowSpawner = ColorSpawner(
        greenProbability = 0.0f,
        redProbability = 0.0f,
        yellowProbability = 1.0f,
    )

    /** A [ColorSpawner] that always returns RED. */
    private val redSpawner = ColorSpawner(
        greenProbability = 0.0f,
        redProbability = 1.0f,
        yellowProbability = 0.0f,
    )

    @BeforeEach
    fun setUp() {
        testScope = TestScope()
        engine = GameEngine(greenSpawner)
    }

    /**
     * Helper to manually set a cell to a specific color for testing.
     * This bypasses the spawn system to allow deterministic scoring tests.
     */
    private fun GameEngine.setCellColor(index: Int, color: CellColor) {
        val current = state.value
        val newGrid = current.grid.map { cell ->
            if (cell.index == index) {
                CellState(index, color, 0L, 5000L) // 5 second lifetime
            } else {
                cell
            }
        }
        // Use reflection or direct state manipulation - for tests only
        val field = GameEngine::class.java.getDeclaredField("_state")
        field.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        val stateFlow = field.get(this) as kotlinx.coroutines.flow.MutableStateFlow<GameState>
        stateFlow.value = current.copy(grid = newGrid)
    }

    // ── startGame() ─────────────────────────────────────────────────────────

    @Nested
    inner class StartGame {

        @Test
        fun `startGame sets phase to PLAYING`() = testScope.runTest {
            engine.startGame()
            assertEquals(GamePhase.PLAYING, engine.state.value.phase)
        }

        @Test
        fun `startGame populates all 9 grid cells`() = testScope.runTest {
            engine.startGame()
            assertEquals(GRID_SIZE, engine.state.value.grid.size)
        }

        @Test
        fun `startGame initialises score to 0`() = testScope.runTest {
            engine.startGame()
            assertEquals(0, engine.state.value.score)
        }

        @Test
        fun `startGame initialises timer to 30 seconds`() = testScope.runTest {
            engine.startGame()
            assertEquals(INITIAL_TIME, engine.state.value.timeRemaining)
        }

        @Test
        fun `startGame grid cells all start as GRAY`() = testScope.runTest {
            engine.startGame()
            engine.state.value.grid.forEach { cell ->
                assertEquals(CellColor.GRAY, cell.color) {
                    "All cells should be GRAY at game start, got index=${cell.index}"
                }
            }
        }
    }

    // ── tapCell() Scoring ────────────────────────────────────────────────────

    @Nested
    inner class Scoring {

        @Test
        fun `tapping GREEN adds 1 point`() = testScope.runTest {
            val engine = GameEngine(greenSpawner)
            engine.startGame()
            engine.setCellColor(0, CellColor.GREEN)
            val result = engine.tapCell(0)
            assertEquals(1, engine.state.value.score)
            assertEquals(TapResult.Scored(1, 0f), result)
        }

        @Test
        fun `tapping GREEN returns cell to GRAY`() = testScope.runTest {
            val engine = GameEngine(greenSpawner)
            engine.startGame()
            engine.setCellColor(0, CellColor.GREEN)
            engine.tapCell(0)
            assertEquals(CellColor.GRAY, engine.state.value.grid[0].color)
        }

        @Test
        fun `tapping RED subtracts 1 point`() = testScope.runTest {
            val engine = GameEngine(redSpawner)
            engine.startGame()
            // First add some points
            engine.setCellColor(0, CellColor.GREEN)
            engine.tapCell(0) // score = 1
            engine.setCellColor(1, CellColor.GREEN)
            engine.tapCell(1) // score = 2
            engine.setCellColor(2, CellColor.GREEN)
            engine.tapCell(2) // score = 3
            engine.setCellColor(3, CellColor.GREEN)
            engine.tapCell(3) // score = 4
            engine.setCellColor(4, CellColor.GREEN)
            engine.tapCell(4) // score = 5

            // Now test RED scoring
            engine.setCellColor(5, CellColor.RED)
            val result = engine.tapCell(5)
            assertEquals(4, engine.state.value.score)
            assertEquals(-1, (result as TapResult.Scored).points)
        }

        @Test
        fun `tapping RED at score 0 floors to 0`() = testScope.runTest {
            val engine = GameEngine(redSpawner)
            engine.startGame()
            engine.setCellColor(0, CellColor.RED)
            engine.tapCell(0)
            assertEquals(0, engine.state.value.score)
        }

        @Test
        fun `tapping YELLOW adds 3 points`() = testScope.runTest {
            val engine = GameEngine(yellowSpawner)
            engine.startGame()
            engine.setCellColor(0, CellColor.YELLOW)
            val result = engine.tapCell(0)
            assertEquals(3, engine.state.value.score)
            assertInstanceOf(TapResult.Scored::class.java, result)
            val scored = result as TapResult.Scored
            assertEquals(3, scored.points)
            assertTrue(scored.timeBonus in YELLOW_TIME_BONUS_MIN..YELLOW_TIME_BONUS_MAX)
        }

        @Test
        fun `tapping YELLOW adds time bonus`() = testScope.runTest {
            val engine = GameEngine(yellowSpawner)
            engine.startGame()
            val initialTime = engine.state.value.timeRemaining
            engine.setCellColor(0, CellColor.YELLOW)
            engine.tapCell(0)
            val timeAdded = engine.state.value.timeRemaining - initialTime
            assertTrue(timeAdded in YELLOW_TIME_BONUS_MIN..YELLOW_TIME_BONUS_MAX)
        }

        @Test
        fun `score never goes below 0`() = testScope.runTest {
            val engine = GameEngine(redSpawner)
            engine.startGame()
            repeat(5) {
                engine.setCellColor(it, CellColor.RED)
                engine.tapCell(it)
            }
            assertEquals(0, engine.state.value.score)
        }

        @Test
        fun `timer is capped at MAX_TIME`() = testScope.runTest {
            val engine = GameEngine(yellowSpawner)
            engine.startGame()
            // Tap yellow 10 times to exceed MAX_TIME
            repeat(10) { i ->
                engine.setCellColor(i % GRID_SIZE, CellColor.YELLOW)
                engine.tapCell(i % GRID_SIZE)
            }
            assertEquals(MAX_TIME, engine.state.value.timeRemaining)
        }

        @Test
        fun `tapping during MENU returns Missed`() = testScope.runTest {
            val result = engine.tapCell(0)
            assertEquals(TapResult.Missed, result)
        }

        @Test
        fun `tapping during GAME_OVER returns Missed`() = testScope.runTest {
            engine.startGame()
            // Fast-forward through the game loop to trigger game over
            val job = testScope.backgroundScope.launch { engine.runGameLoop() }
            advanceTimeBy((INITIAL_TIME / TICK_TIME_DECREMENT * TICK_INTERVAL_MS).toLong() + 100)
            job.cancel()
            assertEquals(GamePhase.GAME_OVER, engine.state.value.phase)
            val result = engine.tapCell(0)
            assertEquals(TapResult.Missed, result)
        }

        @Test
        fun `tapping GRAY cell returns Missed`() = testScope.runTest {
            engine.startGame()
            // All cells start as GRAY
            val result = engine.tapCell(0)
            assertEquals(TapResult.Missed, result)
        }
    }

    // ── Game Loop and Timer ──────────────────────────────────────────────────

    @Nested
    inner class GameLoop {

        @Test
        fun `game loop decrements timer over time`() = testScope.runTest {
            engine.startGame()
            val job = launch { engine.runGameLoop() }
            // Advance 10 ticks = 1 second
            advanceTimeBy(TICK_INTERVAL_MS * 10)
            job.cancel()
            val expectedTime = INITIAL_TIME - TICK_TIME_DECREMENT * 10
            assertEquals(expectedTime, engine.state.value.timeRemaining, 0.01f)
        }

        @Test
        fun `game transitions to GAME_OVER when timer reaches 0`() = testScope.runTest {
            engine.startGame()
            val job = launch { engine.runGameLoop() }
            // Advance enough ticks to exhaust the timer
            val ticksToEmpty = (INITIAL_TIME / TICK_TIME_DECREMENT).toInt() + 1
            advanceTimeBy(TICK_INTERVAL_MS * ticksToEmpty)
            job.cancel()
            assertEquals(GamePhase.GAME_OVER, engine.state.value.phase)
            assertEquals(0f, engine.state.value.timeRemaining)
        }

        @Test
        fun `game loop stops when phase is no longer PLAYING`() = testScope.runTest {
            engine.startGame()
            val job = launch { engine.runGameLoop() }
            advanceTimeBy(TICK_INTERVAL_MS * 5)
            assertEquals(GamePhase.PLAYING, engine.state.value.phase)
            // Directly set phase to test loop termination
            engine.reset()
            // The loop should terminate on next iteration
            advanceTimeBy(TICK_INTERVAL_MS * 2)
            assertTrue(job.isCompleted)
        }

        @Test
        fun `timer decrements by correct amount per tick`() = testScope.runTest {
            engine.startGame()
            val job = launch { engine.runGameLoop() }
            // 1 tick
            advanceTimeBy(TICK_INTERVAL_MS)
            job.cancel()
            assertEquals(INITIAL_TIME - TICK_TIME_DECREMENT, engine.state.value.timeRemaining, 0.001f)
        }
    }

    // ── reset() ──────────────────────────────────────────────────────────────

    @Nested
    inner class Reset {

        @Test
        fun `reset returns to MENU phase`() = testScope.runTest {
            engine.startGame()
            engine.reset()
            assertEquals(GamePhase.MENU, engine.state.value.phase)
        }

        @Test
        fun `reset clears grid to default state`() = testScope.runTest {
            engine.startGame()
            engine.reset()
            // Default GameState has 9 GRAY cells
            assertEquals(9, engine.state.value.grid.size)
            engine.state.value.grid.forEach { cell ->
                assertEquals(CellColor.GRAY, cell.color)
            }
        }

        @Test
        fun `reset clears score`() = testScope.runTest {
            val eng = GameEngine(yellowSpawner)
            eng.startGame()
            eng.setCellColor(0, CellColor.YELLOW)
            eng.tapCell(0)
            eng.reset()
            assertEquals(0, eng.state.value.score)
        }
    }

    // ── Edge Cases ───────────────────────────────────────────────────────────

    @Nested
    inner class EdgeCases {

        @Test
        fun `tapping out of bounds throws`() = testScope.runTest {
            engine.startGame()
            val ex = org.junit.jupiter.api.assertThrows<IllegalArgumentException> {
                engine.tapCell(GRID_SIZE) // index 9 is out of bounds
            }
            assertTrue(ex.message!!.contains("index out of bounds"))
        }

        @Test
        fun `tapping negative index throws`() = testScope.runTest {
            engine.startGame()
            org.junit.jupiter.api.assertThrows<IllegalArgumentException> {
                engine.tapCell(-1)
            }
        }

        @Test
        fun `tapCell returns TapResult Missed for GRAY cells`() = testScope.runTest {
            engine.startGame()
            // All cells start as GRAY
            val result = engine.tapCell(0)
            assertEquals(TapResult.Missed, result)
        }

        @Test
        fun `initial state is MENU`() = testScope.runTest {
            assertEquals(GamePhase.MENU, engine.state.value.phase)
            assertEquals(0, engine.state.value.score)
            assertEquals(30f, engine.state.value.timeRemaining)
        }

        @Test
        fun `StateFlow emits updated state after tap`() = testScope.runTest {
            val engine = GameEngine(greenSpawner)
            engine.startGame()
            engine.setCellColor(0, CellColor.GREEN)
            val collected = mutableListOf<Int>()
            val job = launch {
                engine.state.collect { collected.add(it.score) }
            }
            engine.tapCell(0)
            // Allow collection
            job.cancel()
            assertTrue(collected.contains(1)) {
                "Collected states should include score=1, got: $collected"
            }
        }
    }
}
