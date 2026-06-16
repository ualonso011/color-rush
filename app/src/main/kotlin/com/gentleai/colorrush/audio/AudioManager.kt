package com.gentleai.colorrush.audio

/**
 * Audio manager interface for the game.
 *
 * Responsibilities:
 * - Play looping background music (BGM) via [playBgm] / [stopBgm] / [pauseBgm] / [resumeBgm].
 * - Play one-shot sound effects (SFX) via [playSfx].
 * - Respect the sound-enabled preference via [setEnabled].
 * - Release all audio resources via [release].
 */
interface AudioManager {

    /** Starts or resumes looping background music. */
    fun playBgm()

    /** Stops background music entirely. */
    fun stopBgm()

    /** Pauses background music without releasing resources. */
    fun pauseBgm()

    /** Resumes background music from a paused state. */
    fun resumeBgm()

    /** Plays a one-shot sound effect identified by [type]. */
    fun playSfx(type: SfxType)

    /**
     * Enables or disables all audio output.
     * When disabled, [playBgm], [playSfx] etc. are no-ops.
     */
    fun setEnabled(enabled: Boolean)

    /** Releases all audio resources (MediaPlayer, SoundPool). Call when done. */
    fun release()
}
