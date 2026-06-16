package com.gentleai.colorrush.ui.gameover

/**
 * UI state for the [GameOverScreen].
 *
 * @property finalScore The score the player achieved in the completed game.
 * @property playerName Current text in the name input field.
 * @property isSaving True while the score is being persisted to the database.
 * @property isSaved True after the score has been successfully saved.
 * @property error User-facing error message if saving failed, or null.
 */
data class GameOverUiState(
    val finalScore: Int = 0,
    val playerName: String = "",
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null,
)
