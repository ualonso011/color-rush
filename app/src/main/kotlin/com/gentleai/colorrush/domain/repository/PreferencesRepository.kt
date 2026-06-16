package com.gentleai.colorrush.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * Repository contract for persisting user preferences (language, sound).
 *
 * Implementations are provided in the data layer (Phase 3).
 */
interface PreferencesRepository {

    /** Emits the current language code (default: "eu"). */
    val language: Flow<String>

    /** Emits whether sound effects are enabled (default: true). */
    val soundEnabled: Flow<Boolean>

    /** Persists the selected language code. */
    suspend fun setLanguage(lang: String)

    /** Persists the sound enabled preference. */
    suspend fun setSoundEnabled(enabled: Boolean)
}
