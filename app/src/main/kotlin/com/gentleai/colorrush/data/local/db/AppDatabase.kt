package com.gentleai.colorrush.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.gentleai.colorrush.data.local.db.dao.RankingDao
import com.gentleai.colorrush.data.local.db.entity.RankingEntryEntity

/**
 * Room database for ColorRush.
 *
 * Schema version 1 — single entity: [RankingEntryEntity].
 * Schema export is enabled for future migration tracking
 * (exported to `app/schemas/`).
 */
@Database(
    entities = [RankingEntryEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {

    /**
     * Provides the [RankingDao] for ranking CRUD operations.
     */
    abstract fun rankingDao(): RankingDao
}
