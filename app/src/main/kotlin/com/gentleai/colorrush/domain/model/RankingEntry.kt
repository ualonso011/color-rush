package com.gentleai.colorrush.domain.model

/**
 * Domain model for a persisted ranking entry.
 *
 * @property playerName Display name of the player (1-15 characters).
 * @property score Final score achieved.
 * @property playedAt Epoch millis timestamp when the game was completed.
 */
data class RankingEntry(
    val playerName: String,
    val score: Int,
    val playedAt: Long,
)
