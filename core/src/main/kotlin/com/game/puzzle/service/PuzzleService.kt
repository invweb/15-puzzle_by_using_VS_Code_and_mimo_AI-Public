package com.game.puzzle.service

import com.game.puzzle.model.GameState

object PuzzleService {
    const val SHUFFLE_STEPS = 50

    fun initSolved(size: Int = GameState.DEFAULT_SIZE): GameState {
        val grid = mutableListOf<List<Int?>>()
        var counter = 1
        for (r in 0 until size) {
            val row = mutableListOf<Int?>()
            for (c in 0 until size) {
                if (r == size - 1 && c == size - 1) {
                    row.add(0)
                } else {
                    row.add(counter++)
                }
            }
            grid.add(row)
        }
        return GameState(
            grid = grid,
            movesCount = 0,
            isSolved = true,
            size = size
        )
    }

    fun shuffle(state: GameState, steps: Int = SHUFFLE_STEPS): GameState {
        var current = state
        var lastDir = -1
        val random = kotlin.random.Random

        repeat(steps) {
            val (er, ec) = current.emptyPosition()
            val directions = mutableListOf<Pair<Int, Int>>()
            if (er > 0 && lastDir != 2) directions.add(-1 to 0)
            if (er < current.size - 1 && lastDir != 0) directions.add(1 to 0)
            if (ec > 0 && lastDir != 3) directions.add(0 to -1)
            if (ec < current.size - 1 && lastDir != 1) directions.add(0 to 1)

            val (dr, dc) = directions[random.nextInt(directions.size)]
            val newEmptyRow = er + dr
            val newEmptyCol = ec + dc

            val grid = current.grid.map { it.toMutableList() }.toMutableList()
            grid[er][ec] = grid[newEmptyRow][newEmptyCol]
            grid[newEmptyRow][newEmptyCol] = 0

            // Update lastDir (opposite direction to avoid undoing last move)
            lastDir = when (dr) {
                -1 -> 0
                1 -> 2
                else -> if (dc == -1) 3 else 1
            }

            current = current.copy(
                grid = grid,
                movesCount = 0,
                isSolved = false
            )
        }
        return current
    }

    fun moveTile(state: GameState, row: Int, col: Int): GameState? {
        if (row !in 0 until state.size || col !in 0 until state.size) return null
        if (state.grid[row][col] == 0) return null

        val (er, ec) = state.emptyPosition()
        val isAdjacent = (kotlin.math.abs(row - er) + kotlin.math.abs(col - ec)) == 1
        if (!isAdjacent) return null

        val grid = state.grid.map { it.toMutableList() }.toMutableList()
        grid[er][ec] = grid[row][col]
        grid[row][col] = 0

        val newState = state.copy(
            grid = grid,
            movesCount = state.movesCount + 1
        )
        return newState.copy(isSolved = isSolved(newState))
    }

    fun isSolved(state: GameState): Boolean {
        val size = state.size
        var expected = 1
        for (r in 0 until size) {
            for (c in 0 until size) {
                if (r == size - 1 && c == size - 1) {
                    if (state.grid[r][c] != 0) return false
                } else {
                    if (state.grid[r][c] != expected) return false
                    expected++
                }
            }
        }
        return true
    }
}
