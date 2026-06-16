package com.gentleai.colorrush.data.repository

import com.gentleai.colorrush.data.local.db.dao.RankingDao
import com.gentleai.colorrush.data.local.db.entity.RankingEntryEntity
import com.gentleai.colorrush.domain.model.RankingEntry
import com.gentleai.colorrush.domain.repository.RankingRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of [RankingRepository] backed by Room via [RankingDao].
 *
 * Handles the mapping between the domain [RankingEntry] model and
 * the data-layer [RankingEntryEntity].
 */
@Singleton
class RankingRepositoryImpl @Inject constructor(
    private val rankingDao: RankingDao,
) : RankingRepository {

    /**
     * Retrieves the top 10 rankings by collecting the first emission
     * from the reactive DAO flow and mapping each entity to its domain model.
     */
    override suspend fun getTop10(): List<RankingEntry> {
        return rankingDao.getTop10().first().map { it.toDomain() }
    }

    /**
     * Persists a completed game score by mapping the domain [entry]
     * to an entity and delegating to the DAO.
     */
    override suspend fun saveScore(entry: RankingEntry) {
        rankingDao.insert(entry.toEntity())
    }

    // ── Mapping helpers ─────────────────────────────────────────────────

    private fun RankingEntryEntity.toDomain() = RankingEntry(
        playerName = playerName,
        score = score,
        playedAt = playedAt,
    )

    private fun RankingEntry.toEntity() = RankingEntryEntity(
        playerName = playerName,
        score = score,
        playedAt = playedAt,
    )
}
