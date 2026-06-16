package com.gentleai.colorrush.di

import com.gentleai.colorrush.data.local.datastore.PreferencesDataStoreRepository
import com.gentleai.colorrush.data.repository.RankingRepositoryImpl
import com.gentleai.colorrush.domain.repository.PreferencesRepository
import com.gentleai.colorrush.domain.repository.RankingRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module that binds repository interfaces to their implementations.
 *
 * - [RankingRepository] ← [RankingRepositoryImpl]
 * - [PreferencesRepository] ← [PreferencesDataStoreRepository]
 *
 * All bindings are scoped to [SingletonComponent] since repositories
 * hold no mutable per-screen state.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindRankingRepository(impl: RankingRepositoryImpl): RankingRepository

    @Binds
    @Singleton
    abstract fun bindPreferencesRepository(impl: PreferencesDataStoreRepository): PreferencesRepository
}
