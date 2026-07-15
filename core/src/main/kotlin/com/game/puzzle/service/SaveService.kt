package com.game.puzzle.service

import com.game.puzzle.model.GameState
import kotlinx.serialization.json.Json
import java.io.File

object SaveService {
    private const val SAVE_FILE = "puzzle_save.json"
    private val json = Json { prettyPrint = false; ignoreUnknownKeys = true }

    fun save(state: GameState) {
        val file = File(SAVE_FILE)
        file.writeText(json.encodeToString(GameState.serializer(), state))
    }

    fun load(): GameState? {
        val file = File(SAVE_FILE)
        if (!file.exists()) return null
        return try {
            json.decodeFromString(GameState.serializer(), file.readText())
        } catch (e: Exception) {
            null
        }
    }
}
