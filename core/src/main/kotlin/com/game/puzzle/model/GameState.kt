package com.game.puzzle.model

import kotlinx.serialization.Serializable

@Serializable
data class GameState(
    val grid: List<List<Int?>>,
    val movesCount: Int,
    val isSolved: Boolean,
    val size: Int
) {
    companion object {
        const val DEFAULT_SIZE = 4
    }

    fun tileAt(row: Int, col: Int): Int? = grid[row][col]

    fun emptyPosition(): Pair<Int, Int> {
        for (r in 0 until size) {
            for (c in 0 until size) {
                if (grid[r][c] == 0) return r to c
            }
        }
        throw IllegalStateException("No empty cell found")
    }
}
