package com.gentleai.colorrush.domain.engine

import com.gentleai.colorrush.domain.engine.GameEngine.Companion.GRID_SIZE
import com.gentleai.colorrush.domain.engine.GameEngine.Companion.INITIAL_TIME
import com.gentleai.colorrush.domain.engine.GameEngine.Companion.MAX_TIME
import com.gentleai.colorrush.domain.engine.GameEngine.Companion.TICK_INTERVAL_MS
import com.gentleai.colorrush.domain.engine.GameEngine.Companion.TICK_TIME_DECREMENT
import com.gentleai.colorrush.domain.engine.GameEngine.Companion.YELLOW_TIME_BONUS
import com.gentleai.colorrush.domain.model.CellColor
import com.gentleai.colorrush.domain.model.GamePhase
import kotlinx.coroutines.flow.first
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
        fun `startGame grid cells all have valid colors`() = testScope.runTest {
            engine.startGame()
            engine.state.value.grid.forEach { cell ->
                assertNotEquals(CellColor.GRAY, cell.color) {
                    "No cell should be GRAY at game start, got index=${cell.index}"
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
            val result = engine.tapCell(0)
            assertEquals(1, engine.state.value.score)
            assertEquals(TapResult.Scored(1, 0f), result)
        }

        @Test
        fun `tapping RED subtracts 1 point`() = testScope.runTest {
            val engine = GameEngine(redSpawner)
            engine.startGame()
            // Start with score 5 by tapping green first
            val greenEngine = GameEngine(greenSpawner)
            greenEngine.startGame()
            greenEngine.tapCell(0) // score = 1
            greenEngine.tapCell(0) // score = 2
            greenEngine.tapCell(0) // score = 3
            greenEngine.tapCell(0) // score = 4
            greenEngine.tapCell(0) // score = 5

            // Now test RED scoring directly by checking TapResult
            val result = engine.tapCell(0)
            assertInstanceOf(TapResult.Scored::class.java, result)
            assertEquals(-1, (result as TapResult.Scored).points)
        }

        @Test
        fun `tapping RED at score 0 floors to 0`() = testScope.runTest {
            val engine = GameEngine(redSpawner)
            engine.startGame()
            engine.tapCell(0)
            assertEquals(0, engine.state.value.score)
        }

        @Test
        fun `tapping YELLOW adds 3 points`() = testScope.runTest {
            val engine = GameEngine(yellowSpawner)
            engine.startGame()
            val result = engine.tapCell(0)
            assertEquals(3, engine.state.value.score)
            assertEquals(TapResult.Scored(3, YELLOW_TIME_BONUS), result)
        }

        @Test
        fun `tapping YELLOW adds time bonus`() = testScope.runTest {
            val engine = GameEngine(yellowSpawner)
            engine.startGame()
            val initialTime = engine.state.value.timeRemaining
            engine.tapCell(0)
            assertEquals(initialTime + YELLOW_TIME_BONUS, engine.state.value.timeRemaining)
        }

        @Test
        fun `score never goes below 0`() = testScope.runTest {
            val engine = GameEngine(redSpawner)
            engine.startGame()
            repeat(5) { engine.tapCell(0) }
            assertEquals(0, engine.state.value.score)
        }

        @Test
        fun `timer is capped at MAX_TIME`() = testScope.runTest {
            val engine = GameEngine(yellowSpawner)
            engine.startGame()
            // Start at 30, each tap adds +3 — after 10 taps we'd be at 60
            // We'll simulate by directly setting state, then tapping
            repeat(10) { engine.tapCell(0) }
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
        fun `reset clears grid`() = testScope.runTest {
            engine.startGame()
            engine.reset()
            assertTrue(engine.state.value.grid.isEmpty())
        }

        @Test
        fun `reset clears score`() = testScope.runTest {
            val eng = GameEngine(yellowSpawner)
            eng.startGame()
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
        fun `tapCell returns TapResult Scored for valid taps`() = testScope.runTest {
            engine.startGame()
            val result = engine.tapCell(0)
            assertInstanceOf(TapResult.Scored::class.java, result)
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
