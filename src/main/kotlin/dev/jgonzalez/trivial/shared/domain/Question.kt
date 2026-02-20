package dev.jgonzalez.trivial.shared.domain

import kotlinx.serialization.Serializable

@Serializable
data class Question(
    val id: String,
    val category: Category,
    val difficulty: Difficulty,
    val question: String,
    val options: List<String>,
    val correctAnswer: Int,
    val explanation: String
)
