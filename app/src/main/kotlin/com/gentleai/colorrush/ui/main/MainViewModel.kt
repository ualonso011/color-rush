package com.gentleai.colorrush.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gentleai.colorrush.domain.repository.PreferencesRepository
import com.gentleai.colorrush.domain.repository.RankingRepository
import com.gentleai.colorrush.ui.navigation.LocaleHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the main menu screen.
 *
 * Responsibilities:
 * - Loads and exposes the top 10 ranking entries from [RankingRepository].
 * - Manages language selection via [PreferencesRepository] and [LocaleHelper].
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    private val rankingRepository: RankingRepository,
    private val preferencesRepository: PreferencesRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        observeLanguage()
        loadRankings()
    }

    /**
     * Reloads the top 10 rankings from the database.
     * Called on init and can be called to refresh after a game is saved.
     */
    fun loadRankings() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val rankings = rankingRepository.getTop10()
                _uiState.update { it.copy(rankings = rankings, isLoading = false) }
            } catch (_: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    /**
     * Observes the persisted language preference and updates UI state.
     */
    private fun observeLanguage() {
        viewModelScope.launch {
            preferencesRepository.language.collect { lang ->
                _uiState.update { it.copy(currentLanguage = lang) }
            }
        }
    }

    /**
     * Switches the application language to [lang].
     *
     * Persists the selection via [PreferencesRepository] and applies it
     * immediately via [LocaleHelper.applyLanguage], which triggers an
     * Activity recreation with the new locale.
     */
    fun setLanguage(lang: String) {
        viewModelScope.launch {
            preferencesRepository.setLanguage(lang)
            LocaleHelper.applyLanguage(lang)
        }
    }
}
