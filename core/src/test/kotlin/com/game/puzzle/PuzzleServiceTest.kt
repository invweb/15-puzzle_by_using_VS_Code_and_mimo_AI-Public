package com.game.puzzle

import com.game.puzzle.model.GameState
import com.game.puzzle.service.PuzzleService
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PuzzleServiceTest {

    @Test
    fun initSolvedCreatesCorrectGrid() {
        val state = PuzzleService.initSolved(4)
        assertEquals(4, state.size)
        assertEquals(0, state.movesCount)
        assertTrue(state.isSolved)

        val expected = listOf(
            listOf(1, 2, 3, 4),
            listOf(5, 6, 7, 8),
            listOf(9, 10, 11, 12),
            listOf(13, 14, 15, 0)
        )
        assertEquals(expected, state.grid)
    }

    @Test
    fun validMovesWork() {
        val state = PuzzleService.initSolved(4)
        // Empty cell is at (3,3), can move (3,2) or (2,3)
        val result = PuzzleService.moveTile(state, 3, 2)
        assertNotNull(result)
        assertEquals(1, result.movesCount)
        assertEquals(0, result.grid[3][2])
        assertEquals(15, result.grid[3][3])
    }

    @Test
    fun invalidMovesReturnNull() {
        val state = PuzzleService.initSolved(4)
        // (0,0) is not adjacent to empty cell (3,3)
        assertNull(PuzzleService.moveTile(state, 0, 0))
        // Moving empty cell itself
        assertNull(PuzzleService.moveTile(state, 3, 3))
        // Out of bounds
        assertNull(PuzzleService.moveTile(state, -1, 0))
        assertNull(PuzzleService.moveTile(state, 4, 0))
    }

    @Test
    fun isSolvedDetectsCompletedPuzzle() {
        val solved = PuzzleService.initSolved(4)
        assertTrue(PuzzleService.isSolved(solved))

        val moved = PuzzleService.moveTile(solved, 3, 2)!!
        assertFalse(PuzzleService.isSolved(moved))
    }

    @Test
    fun shuffleProducesSolvableState() {
        val initial = PuzzleService.initSolved(4)
        val shuffled = PuzzleService.shuffle(initial, 50)
        assertFalse(shuffled.isSolved)
        assertEquals(0, shuffled.movesCount)
        assertEquals(4, shuffled.size)

        // Verify all tiles 1-15 exist exactly once, and exactly one 0
        val flat = shuffled.grid.flatten()
        val nonZero = flat.filterIsInstance<Int>().filter { it != 0 }.sorted()
        assertEquals((1..15).toList(), nonZero)
        assertEquals(1, flat.count { it == 0 })
    }

    @Test
    fun gameControllerWorks() {
        val controller = GameController(4, 10)
        assertTrue(controller.isSolved)
        assertEquals(0, controller.movesCount)

        controller.shuffle()
        assertFalse(controller.isSolved)

        controller.reset()
        assertTrue(controller.isSolved)
        assertEquals(0, controller.movesCount)
    }
}
