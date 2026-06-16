package com.gentleai.colorrush.di

import android.content.Context
import com.gentleai.colorrush.audio.AudioManager
import com.gentleai.colorrush.audio.AudioManagerImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module that provides the [AudioManager] singleton.
 *
 * Audio resources are managed at the application level so that BGM and SFX
 * can be controlled across the game lifecycle without recreating MediaPlayer
 * or SoundPool instances.
 */
@Module
@InstallIn(SingletonComponent::class)
object AudioModule {

    @Provides
    @Singleton
    fun provideAudioManager(
        @ApplicationContext context: Context,
    ): AudioManager {
        return AudioManagerImpl(context)
    }
}
