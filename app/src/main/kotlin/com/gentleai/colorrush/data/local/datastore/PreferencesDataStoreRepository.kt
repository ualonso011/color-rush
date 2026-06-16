package com.gentleai.colorrush.data.local.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.gentleai.colorrush.domain.repository.PreferencesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * DataStore-backed implementation of [PreferencesRepository].
 *
 * Uses Jetpack DataStore Preferences to persist:
 * - **language**: stored as a String (ISO 639-1 code, default "eu").
 * - **soundEnabled**: stored as a Boolean (default `true`).
 *
 * The underlying DataStore file is named `color_rush_preferences`.
 */
@Singleton
class PreferencesDataStoreRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) : PreferencesRepository {

    companion object {
        private val LANGUAGE_KEY = stringPreferencesKey("language")
        private val SOUND_ENABLED_KEY = booleanPreferencesKey("sound_enabled")
    }

    /** Extension property to lazily initialise the preferences DataStore. */
    private val Context.dataStore by preferencesDataStore(name = "color_rush_preferences")

    override val language: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[LANGUAGE_KEY] ?: "eu"
    }

    override val soundEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[SOUND_ENABLED_KEY] ?: true
    }

    override suspend fun setLanguage(lang: String) {
        context.dataStore.edit { prefs ->
            prefs[LANGUAGE_KEY] = lang
        }
    }

    override suspend fun setSoundEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[SOUND_ENABLED_KEY] = enabled
        }
    }
}
