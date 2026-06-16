package com.gentleai.colorrush.domain.model

/**
 * Represents the state of a single cell in the 3×3 game grid.
 *
 * @property index Position in the grid (0..8, row-major order).
 * @property color The current display color of this cell.
 * @property lifetimeMs How long this cell has been active (0 if GRAY).
 * @property maxLifetimeMs Maximum time before this cell auto-disappears (0 if GRAY).
 */
data class CellState(
    val index: Int,
    val color: CellColor,
    val lifetimeMs: Long = 0L,
    val maxLifetimeMs: Long = 0L,
)
