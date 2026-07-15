package com.game.puzzle.model

import com.game.puzzle.service.PuzzleService

data class MoveResult(
    val newState: GameState?,
    val tileId: Int?,
    val fromRow: Int,
    val fromCol: Int,
    val toRow: Int,
    val toCol: Int
)
