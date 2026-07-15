package com.game.puzzle

import com.game.puzzle.model.GameState
import com.game.puzzle.service.PuzzleService

class GameController(
    private val size: Int = GameState.DEFAULT_SIZE,
    private val shuffleSteps: Int = PuzzleService.SHUFFLE_STEPS
) {
    var state: GameState = PuzzleService.initSolved(size)
        private set

    fun shuffle() {
        state = PuzzleService.shuffle(state, shuffleSteps)
    }

    fun moveTile(row: Int, col: Int): Boolean {
        val newState = PuzzleService.moveTile(state, row, col) ?: return false
        state = newState
        return true
    }

    fun reset() {
        state = PuzzleService.initSolved(size)
    }

    val isSolved: Boolean get() = state.isSolved
    val movesCount: Int get() = state.movesCount
    val gridSize: Int get() = size
}
