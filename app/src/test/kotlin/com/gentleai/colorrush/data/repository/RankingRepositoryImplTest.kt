package com.gentleai.colorrush.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.gentleai.colorrush.data.local.db.AppDatabase
import com.gentleai.colorrush.data.local.db.dao.RankingDao
import com.gentleai.colorrush.data.local.db.entity.RankingEntryEntity
import com.gentleai.colorrush.domain.model.RankingEntry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Integration test for [RankingRepositoryImpl] using an in-memory Room database.
 *
 * Verifies:
 * - Entity ↔ domain model mapping
 * - Insert and retrieval via getTop10
 * - Ordering: score DESC, played_at DESC
 * - Top-10 limit
 */
@RunWith(RobolectricTestRunner::class)
class RankingRepositoryImplTest {

    private lateinit var database: AppDatabase
    private lateinit var dao: RankingDao
    private lateinit var repository: RankingRepositoryImpl

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java,
        ).build()
        dao = database.rankingDao()
        repository = RankingRepositoryImpl(dao)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `saveScore inserts a ranking entry`() = runTest {
        val entry = RankingEntry(
            playerName = "TestPlayer",
            score = 100,
            playedAt = 1_000_000L,
        )

        repository.saveScore(entry)

        val top10 = repository.getTop10()
        assertEquals(1, top10.size)
        assertEquals("TestPlayer", top10[0].playerName)
        assertEquals(100, top10[0].score)
        assertEquals(1_000_000L, top10[0].playedAt)
    }

    @Test
    fun `getTop10 returns entries ordered by score descending`() = runTest {
        repository.saveScore(RankingEntry("Player1", 50, 100L))
        repository.saveScore(RankingEntry("Player2", 100, 200L))
        repository.saveScore(RankingEntry("Player3", 75, 300L))

        val top10 = repository.getTop10()
        assertEquals(3, top10.size)
        assertEquals(100, top10[0].score) // Player2 (highest)
        assertEquals(75, top10[1].score)   // Player3
        assertEquals(50, top10[2].score)   // Player1 (lowest)
    }

    @Test
    fun `getTop10 returns at most 10 entries`() = runTest {
        repeat(15) { i ->
            repository.saveScore(RankingEntry("Player$i", i * 10, i.toLong()))
        }

        val top10 = repository.getTop10()
        assertEquals(10, top10.size)
    }

    @Test
    fun `ties are broken by most recent playedAt first`() = runTest {
        repository.saveScore(RankingEntry("Old", 100, 100L))
        repository.saveScore(RankingEntry("Recent", 100, 999L))

        val top10 = repository.getTop10()
        assertEquals(2, top10.size)
        assertEquals("Recent", top10[0].playerName)
        assertEquals("Old", top10[1].playerName)
    }

    @Test
    fun `getTop10 returns empty list when no entries exist`() = runTest {
        val top10 = repository.getTop10()
        assertTrue(top10.isEmpty())
    }

    @Test
    fun `multiple saves persist all entries`() = runTest {
        repository.saveScore(RankingEntry("A", 10, 1L))
        repository.saveScore(RankingEntry("B", 20, 2L))
        repository.saveScore(RankingEntry("C", 30, 3L))

        val top10 = repository.getTop10()
        assertEquals(3, top10.size)
    }

    @Test
    fun `entity to domain mapping preserves all fields`() = runTest {
        val entry = RankingEntry(playerName = "Maren", score = 42, playedAt = 1_234_567L)
        repository.saveScore(entry)

        val result = repository.getTop10().first()
        assertEquals("Maren", result.playerName)
        assertEquals(42, result.score)
        assertEquals(1_234_567L, result.playedAt)
    }

    @Test
    fun `getTop10 returns only top 10 by score`() = runTest {
        // Insert 12 entries with decreasing scores
        repeat(12) { i ->
            repository.saveScore(RankingEntry("Player$i", i * 10, i.toLong()))
        }

        val top10 = repository.getTop10()
        assertEquals(10, top10.size)
        // Highest score should be 110 (Player11)
        assertEquals(110, top10[0].score)
        // Lowest among top 10 should be 20 (Player2)
        assertEquals(20, top10[9].score)
    }
}
