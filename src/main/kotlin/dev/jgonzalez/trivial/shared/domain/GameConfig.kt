package dev.jgonzalez.trivial.shared.domain

import kotlinx.serialization.Serializable

@Serializable
data class GameConfig(
    val mode: GameMode,
    val questions: Int,
    val categories: List<Category>,
    val difficulty: Difficulty?,
    val timeLimitSeconds: Int?,
    val turnMode: TurnMode
)
