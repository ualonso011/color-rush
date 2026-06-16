package com.gentleai.colorrush.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.gentleai.colorrush.data.local.db.entity.RankingEntryEntity
import kotlinx.coroutines.flow.Flow

/**
 * Room DAO for the [RankingEntryEntity] table.
 *
 * Supports:
 * - Inserting new ranking entries (upserts on conflict by primary key).
 * - Observing the top 10 scores ordered by score descending,
 *   with ties broken by playedAt (most recent first).
 */
@Dao
interface RankingDao {

    /**
     * Returns a [Flow] emitting the top 10 rankings whenever the table changes.
     *
     * Ordering: score DESC, played_at DESC — ties are resolved by recency.
     */
    @Query("SELECT * FROM rankings ORDER BY score DESC, played_at DESC LIMIT 10")
    fun getTop10(): Flow<List<RankingEntryEntity>>

    /**
     * Inserts a single [entry] into the rankings table.
     *
     * Uses [OnConflictStrategy.REPLACE] so that if the same id (auto-generated)
     * would conflict, the row is replaced. In practice this only applies when
     * an explicit id is set.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: RankingEntryEntity)
}
