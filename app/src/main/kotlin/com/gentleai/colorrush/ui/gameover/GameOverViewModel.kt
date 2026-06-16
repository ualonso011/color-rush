package com.gentleai.colorrush.ui.gameover

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gentleai.colorrush.domain.model.RankingEntry
import com.gentleai.colorrush.domain.repository.RankingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the game-over screen.
 *
 * Reads the [finalScore] from the navigation arguments via [SavedStateHandle]
 * and manages the name-input state and score-persistence flow.
 *
 * Responsibilities:
 * - Expose the final score from navigation args.
 * - Validate and cap the player name at 10 characters.
 * - Persist a [RankingEntry] via [RankingRepository] on save.
 */
@HiltViewModel
class GameOverViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val rankingRepository: RankingRepository,
) : ViewModel() {

    /** The score received from the game screen via navigation arguments. */
    val finalScore: Int = savedStateHandle.get<Int>("score") ?: 0

    private val _uiState = MutableStateFlow(GameOverUiState(finalScore = finalScore))
    val uiState: StateFlow<GameOverUiState> = _uiState.asStateFlow()

    /**
     * Updates the player name in the UI state.
     *
     * Caps input at 10 characters (arcade-style name limit).
     */
    fun onNameChanged(name: String) {
        if (name.length <= 10) {
            _uiState.update { it.copy(playerName = name) }
        }
    }

    /**
     * Persists the current score and player name to the ranking database.
     *
     * - Validates that the name is not blank before saving.
     * - Sets [GameOverUiState.isSaved] on success.
     * - Sets [GameOverUiState.error] on failure.
     */
    fun saveScore() {
        val name = _uiState.value.playerName.trim()
        if (name.isEmpty() || _uiState.value.isSaving) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            try {
                rankingRepository.saveScore(
                    RankingEntry(
                        playerName = name,
                        score = finalScore,
                        playedAt = System.currentTimeMillis(),
                    ),
                )
                _uiState.update { it.copy(isSaving = false, isSaved = true) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        error = e.message ?: "Failed to save score",
                    )
                }
            }
        }
    }
}
