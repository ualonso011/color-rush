package com.gentleai.colorrush.di

import com.gentleai.colorrush.domain.engine.ColorSpawner
import com.gentleai.colorrush.domain.engine.GameEngine
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module that provides domain-layer game components.
 *
 * - [ColorSpawner] — singleton shared across the app.
 * - [GameEngine] — new instance per injection point (unscoped)
 *   because each game screen needs its own engine instance.
 */
@Module
@InstallIn(SingletonComponent::class)
object GameModule {

    @Provides
    @Singleton
    fun provideColorSpawner(): ColorSpawner {
        return ColorSpawner()
    }

    @Provides
    fun provideGameEngine(colorSpawner: ColorSpawner): GameEngine {
        return GameEngine(colorSpawner)
    }
}
