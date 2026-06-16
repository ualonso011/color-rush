package com.gentleai.colorrush.domain.model

/**
 * Represents the current phase of the game state machine.
 *
 * Transitions: [MENU] → [PLAYING] → [GAME_OVER] → [MENU]
 */
enum class GamePhase {
    /** Initial state — showing the main menu with language selector and leaderboard. */
    MENU,

    /** Active gameplay — grid is interactive, timer is counting down. */
    PLAYING,

    /** Game has ended — final score displayed, awaiting name entry or return to menu. */
    GAME_OVER,
}
