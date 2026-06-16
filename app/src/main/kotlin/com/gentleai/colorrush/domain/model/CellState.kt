package com.gentleai.colorrush.domain.model

/**
 * Represents the state of a single cell in the 3×3 game grid.
 *
 * @property index Position in the grid (0..8, row-major order).
 * @property color The current display color of this cell.
 */
data class CellState(
    val index: Int,
    val color: CellColor,
)
