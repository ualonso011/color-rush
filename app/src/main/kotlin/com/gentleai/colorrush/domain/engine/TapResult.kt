package com.gentleai.colorrush.domain.engine

/**
 * Result of a cell tap, returned by [GameEngine.tapCell].
 *
 * - [Scored]: a non-gray cell was tapped; [points] and optionally [timeBonus] were awarded.
 * - [Missed]: the tap was ignored (wrong phase or gray cell).
 */
sealed interface TapResult {
    /**
     * A scoring tap on a colored cell.
     *
     * @property points Points added to the score (negative values represent a penalty).
     * @property timeBonus Seconds added to the countdown timer (0f for non-yellow taps).
     */
    data class Scored(val points: Int, val timeBonus: Float = 0f) : TapResult

    /** The tap was ignored — either the game is not in PLAYING phase or the cell is gray. */
    data object Missed : TapResult
}
