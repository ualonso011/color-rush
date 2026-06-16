package com.gentleai.colorrush.domain.engine

import com.gentleai.colorrush.domain.model.CellColor
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

/**
 * Unit tests for [ColorSpawner] — weighted random color generation.
 *
 * Verifies probability constraints, default values, and statistical
 * distribution over a large sample.
 */
class ColorSpawnerTest {

    /** Default spawner with the production probabilities. */
    private lateinit var defaultSpawner: ColorSpawner

    @BeforeEach
    fun setUp() {
        defaultSpawner = ColorSpawner()
    }

    // ── Probability Sum Validation ──────────────────────────────────────────

    @Nested
    inner class Construction {

        @Test
        fun `default probabilities sum to 1`() {
            val sum = ColorSpawner.DEFAULT_GREEN_PROBABILITY +
                ColorSpawner.DEFAULT_RED_PROBABILITY +
                ColorSpawner.DEFAULT_YELLOW_PROBABILITY
            assertEquals(1.0f, sum, 0.001f)
        }

        @Test
        fun `custom probabilities that sum to 1 are accepted`() {
            val spawner = ColorSpawner(0.5f, 0.3f, 0.2f)
            assertEquals(0.5f, spawner.greenProbability)
            assertEquals(0.3f, spawner.redProbability)
            assertEquals(0.2f, spawner.yellowProbability)
        }

        @Test
        fun `probabilities that do not sum to 1 throw`() {
            assertThrows<IllegalArgumentException> {
                ColorSpawner(0.5f, 0.5f, 0.5f)
            }
        }

        @Test
        fun `negative probability throws`() {
            assertThrows<IllegalArgumentException> {
                ColorSpawner(-0.1f, 0.6f, 0.5f)
            }
        }

        @Test
        fun `all probabilities zero throws`() {
            assertThrows<IllegalArgumentException> {
                ColorSpawner(0.0f, 0.0f, 0.0f)
            }
        }

        @Test
        fun `probability slightly off due to float rounding is accepted`() {
            // 0.333f + 0.333f + 0.334f = 1.0f within tolerance
            val spawner = ColorSpawner(0.333f, 0.333f, 0.334f)
            assertEquals(0.333f, spawner.greenProbability, 0.001f)
        }
    }

    // ── spawn() Output Range ─────────────────────────────────────────────────

    @Nested
    inner class SpawnOutput {

        @Test
        fun `spawn never returns GRAY`() {
            repeat(1000) {
                val color = defaultSpawner.spawn()
                assertTrue(
                    color == CellColor.GREEN ||
                        color == CellColor.RED ||
                        color == CellColor.YELLOW,
                    "Expected GREEN, RED, or YELLOW, got $color",
                )
            }
        }

        @Test
        fun `deterministic spawner always returns GREEN`() {
            val spawner = ColorSpawner(1.0f, 0.0f, 0.0f)
            repeat(100) {
                assertEquals(CellColor.GREEN, spawner.spawn())
            }
        }

        @Test
        fun `deterministic spawner always returns RED`() {
            val spawner = ColorSpawner(0.0f, 1.0f, 0.0f)
            repeat(100) {
                assertEquals(CellColor.RED, spawner.spawn())
            }
        }

        @Test
        fun `deterministic spawner always returns YELLOW`() {
            val spawner = ColorSpawner(0.0f, 0.0f, 1.0f)
            repeat(100) {
                assertEquals(CellColor.YELLOW, spawner.spawn())
            }
        }
    }

    // ── Statistical Distribution ────────────────────────────────────────────

    @Nested
    inner class Distribution {

        @Test
        fun `default distribution approximates 45-35-20 over 10000 spawns`() {
            val sampleSize = 10_000
            val counts = mutableMapOf<CellColor, Int>()
            repeat(sampleSize) {
                val color = defaultSpawner.spawn()
                counts[color] = (counts[color] ?: 0) + 1
            }

            val greenPct = (counts[CellColor.GREEN] ?: 0).toFloat() / sampleSize
            val redPct = (counts[CellColor.RED] ?: 0).toFloat() / sampleSize
            val yellowPct = (counts[CellColor.YELLOW] ?: 0).toFloat() / sampleSize

            // Allow 5% tolerance for statistical variance
            assertEquals(0.45f, greenPct, 0.05f, "Green probability should be ~45%")
            assertEquals(0.35f, redPct, 0.05f, "Red probability should be ~35%")
            assertEquals(0.20f, yellowPct, 0.05f, "Yellow probability should be ~20%")
        }

        @Test
        fun `custom 60-30-10 distribution over 10000 spawns`() {
            val spawner = ColorSpawner(0.6f, 0.3f, 0.1f)
            val sampleSize = 10_000
            val counts = mutableMapOf<CellColor, Int>()
            repeat(sampleSize) {
                val color = spawner.spawn()
                counts[color] = (counts[color] ?: 0) + 1
            }

            val greenPct = (counts[CellColor.GREEN] ?: 0).toFloat() / sampleSize
            val redPct = (counts[CellColor.RED] ?: 0).toFloat() / sampleSize
            val yellowPct = (counts[CellColor.YELLOW] ?: 0).toFloat() / sampleSize

            assertEquals(0.60f, greenPct, 0.05f)
            assertEquals(0.30f, redPct, 0.05f)
            assertEquals(0.10f, yellowPct, 0.05f)
        }

        @Test
        fun `all three colors appear in a 1000-sample run`() {
            val colors = mutableSetOf<CellColor>()
            repeat(1000) {
                colors.add(defaultSpawner.spawn())
            }
            assertTrue(colors.contains(CellColor.GREEN))
            assertTrue(colors.contains(CellColor.RED))
            assertTrue(colors.contains(CellColor.YELLOW))
        }
    }

    // ── Boundary Tests ──────────────────────────────────────────────────────

    @Nested
    inner class Boundaries {

        @Test
        fun `very small green probability still spawns green`() {
            // 0.001 probability = expected ~1 in 1000
            val spawner = ColorSpawner(0.001f, 0.5f, 0.499f)
            val hasGreen = (1..10_000).any { spawner.spawn() == CellColor.GREEN }
            assertTrue(hasGreen, "Should eventually spawn GREEN even at 0.1% probability")
        }

        @Test
        fun `pure probability 1-0-0 is deterministic`() {
            val spawner = ColorSpawner(1.0f, 0.0f, 0.0f)
            repeat(100) {
                assertEquals(CellColor.GREEN, spawner.spawn())
            }
        }

        @Test
        fun `probability boundary edge 0-0-1`() {
            val spawner = ColorSpawner(0.0f, 0.0f, 1.0f)
            repeat(100) {
                assertEquals(CellColor.YELLOW, spawner.spawn())
            }
        }
    }
}
