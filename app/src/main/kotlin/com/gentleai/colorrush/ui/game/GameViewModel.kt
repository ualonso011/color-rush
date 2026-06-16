package com.gentleai.colorrush.ui.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gentleai.colorrush.audio.AudioManager
import com.gentleai.colorrush.audio.SfxType
import com.gentleai.colorrush.domain.engine.GameEngine
import com.gentleai.colorrush.domain.model.GamePhase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the game screen.
 *
 * Connects the pure-Kotlin [GameEngine] to the Compose UI layer and manages
 * audio lifecycle. Responsibilities:
 *
 * - Starts a new game on initialisation via [GameEngine.startGame].
 * - Launches the engine's coroutine-based game loop in [viewModelScope].
 * - Exposes [GameEngine.state] as a [StateFlow] for Compose to collect.
 * - Handles cell taps by delegating to [GameEngine.tapCell] and playing
 *   the corresponding SFX via [AudioManager].
 * - Emits one-shot [ScoreEffect] events for the score popup animation.
 * - Stops BGM and releases audio resources when the ViewModel is cleared.
 *
 * @param engine A fresh [GameEngine] instance (unscoped — new instance per VM).
 * @param audioManager Shared [AudioManager] singleton for BGM and SFX.
 */
@HiltViewModel
class GameViewModel @Inject constructor(
    private val engine: GameEngine,
    private val audioManager: AudioManager,
) : ViewModel() {

    /** Reactive game state from the engine. */
    val state: StateFlow<com.gentleai.colorrush.domain.model.GameState> = engine.state

    /** One-shot effects for score popup animations. */
    private val _scoreEffect = MutableSharedFlow<ScoreEffect>(extraBufferCapacity = 4)
    val scoreEffect: SharedFlow<ScoreEffect> = _scoreEffect.asSharedFlow()

    init {
        startGame()
    }

    // ── Game lifecycle ──────────────────────────────────────────────────────

    private fun startGame() {
        engine.startGame()
        viewModelScope.launch {
            audioManager.playBgm()
            engine.runGameLoop()
            // Timer reached zero — transition to GAME_OVER
            audioManager.playSfx(SfxType.GAME_OVER)
            audioManager.stopBgm()
        }
    }

    // ── User interaction ────────────────────────────────────────────────────

    /**
     * Processes a cell tap at the given [index].
     *
     * Delegates to [GameEngine.tapCell], plays the appropriate SFX,
     * and emits a [ScoreEffect] for the popup animation.
     */
    fun onCellTap(index: Int) {
        val result = engine.tapCell(index)
        if (result is com.gentleai.colorrush.domain.engine.TapResult.Scored) {
            val sfx = when {
                result.points >= 3 -> SfxType.TAP_YELLOW
                result.points > 0 -> SfxType.TAP_GREEN
                else -> SfxType.TAP_RED
            }
            audioManager.playSfx(sfx)
            _scoreEffect.tryEmit(ScoreEffect(points = result.points, cellIndex = index))
        }
    }

    // ── Audio lifecycle ─────────────────────────────────────────────────────

    /** Pauses background music (call from onPause). */
    fun pauseAudio() {
        audioManager.pauseBgm()
    }

    /** Resumes background music (call from onResume). */
    fun resumeAudio() {
        audioManager.resumeBgm()
    }

    /** Stops the game loop and navigates back (call from back-press handler). */
    fun stopGame() {
        engine.reset()
        audioManager.stopBgm()
    }

    override fun onCleared() {
        super.onCleared()
        audioManager.release()
    }
}
