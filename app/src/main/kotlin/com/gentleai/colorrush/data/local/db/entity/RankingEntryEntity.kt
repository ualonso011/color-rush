package com.gentleai.colorrush.data.local.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity representing a persisted ranking entry.
 *
 * Maps to the [com.gentleai.colorrush.domain.model.RankingEntry] domain model
 * via [RankingRepositoryImpl].
 *
 * @property id Auto-generated primary key.
 * @property playerName Display name of the player.
 * @property score Final score achieved.
 * @property playedAt Epoch millis timestamp when the game was completed.
 */
@Entity(tableName = "rankings")
data class RankingEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "player_name")
    val playerName: String,

    @ColumnInfo(name = "score")
    val score: Int,

    @ColumnInfo(name = "played_at")
    val playedAt: Long,
)
