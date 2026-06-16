package com.gentleai.colorrush.di

import android.content.Context
import androidx.room.Room
import com.gentleai.colorrush.data.local.db.AppDatabase
import com.gentleai.colorrush.data.local.db.dao.RankingDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module that provides the Room database and its DAOs.
 *
 * - [AppDatabase] — singleton scoped; database name "colorrush.db".
 * - [RankingDao] — extracted from [AppDatabase.rankingDao].
 *
 * Both are scoped to [SingletonComponent] so that the database
 * instance is shared across the application lifetime.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context,
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "colorrush.db",
        ).build()
    }

    @Provides
    fun provideRankingDao(database: AppDatabase): RankingDao {
        return database.rankingDao()
    }
}
