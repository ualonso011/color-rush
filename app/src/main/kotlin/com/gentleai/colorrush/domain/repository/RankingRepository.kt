package com.gentleai.colorrush.domain.repository

import com.gentleai.colorrush.domain.model.RankingEntry

/**
 * Repository contract for persisting and retrieving game rankings.
 *
 * Implementations are provided in the data layer (Phase 3).
 */
interface RankingRepository {

    /**
     * Returns the top 10 ranking entries ordered by score descending,
     * with ties broken by playedAt (most recent first).
     */
    suspend fun getTop10(): List<RankingEntry>

    /**
     * Persists a completed game's [entry].
     */
    suspend fun saveScore(entry: RankingEntry)
}
