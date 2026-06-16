package com.gentleai.colorrush.ui.main

import com.gentleai.colorrush.domain.model.RankingEntry

/**
 * UI state for the [MainScreen].
 *
 * @property rankings Top 10 ranking entries ordered by score descending.
 * @property currentLanguage Currently selected language code ("eu", "es", "en").
 * @property isLoading True while the initial ranking data is being loaded.
 */
data class MainUiState(
    val rankings: List<RankingEntry> = emptyList(),
    val currentLanguage: String = "eu",
    val isLoading: Boolean = false,
)
