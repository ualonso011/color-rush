package com.gentleai.colorrush.domain.model

/**
 * Represents the color of a cell in the game grid.
 *
 * [GREEN], [RED], and [YELLOW] are active colors that affect scoring.
 * [GRAY] represents an inactive/hidden cell (reserved for future auto-disappear mechanic).
 */
enum class CellColor {
    GREEN,
    RED,
    YELLOW,
    GRAY,
}
