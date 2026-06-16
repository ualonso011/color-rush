package com.gentleai.colorrush.domain.engine

import com.gentleai.colorrush.domain.model.CellColor
import kotlin.random.Random
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Weighted random color spawner for the game grid.
 *
 * Default probabilities: Green 45%, Red 35%, Yellow 20%.
 * All probabilities are injectable for testability.
 *
 * @param greenProbability  Probability of spawning [CellColor.GREEN]   (default 0.45).
 * @param redProbability    Probability of spawning [CellColor.RED]     (default 0.35).
 * @param yellowProbability Probability of spawning [CellColor.YELLOW] (default 0.20).
 */
@Singleton
class ColorSpawner @Inject constructor(
    val greenProbability: Float = DEFAULT_GREEN_PROBABILITY,
    val redProbability: Float = DEFAULT_RED_PROBABILITY,
    val yellowProbability: Float = DEFAULT_YELLOW_PROBABILITY,
) {
    init {
        val sum = greenProbability + redProbability + yellowProbability
        require((sum - 1.0f).let { it in -0.001f..0.001f }) {
            "Probabilities must sum to 1.0, got $sum"
        }
        require(greenProbability >= 0f) { "greenProbability must be non-negative" }
        require(redProbability >= 0f) { "redProbability must be non-negative" }
        require(yellowProbability >= 0f) { "yellowProbability must be non-negative" }
    }

    private val random: Random = Random.Default

    /**
     * Returns a [CellColor] drawn from the configured weighted distribution.
     */
    fun spawn(): CellColor {
        val roll = random.nextFloat()
        return when {
            roll < greenProbability -> CellColor.GREEN
            roll < greenProbability + redProbability -> CellColor.RED
            else -> CellColor.YELLOW
        }
    }

    companion object {
        const val DEFAULT_GREEN_PROBABILITY = 0.45f
        const val DEFAULT_RED_PROBABILITY = 0.35f
        const val DEFAULT_YELLOW_PROBABILITY = 0.20f
    }
}
