package dev.jgonzalez.trivial.shared.domain

data class AnswerScoreResult(
    val basePoints: Int,
    val speedBonus: Int,
    val streakMultiplier: Double,
    val totalPoints: Int
)

fun calculateScore(
    isCorrect: Boolean,
    difficulty: Difficulty,
    timeElapsedSeconds: Int?,
    currentStreak: Int
): AnswerScoreResult {

    // Si es incorrecta: 0 puntos, sin bonus ni multiplicador
    if (!isCorrect) {
        return AnswerScoreResult(
            basePoints = 0,
            speedBonus = 0,
            streakMultiplier = 1.0,
            totalPoints = 0
        )
    }

    // Puntuación base
    val base = 10

    // x1.5 si es MEDIUM, x2 si es HARD
    val difficultyMultiplier = when (difficulty) {
        Difficulty.EASY, Difficulty.MIXED -> 1.0
        Difficulty.MEDIUM -> 1.5
        Difficulty.HARD -> 2.0
    }

    // Bonus por velocidad: +5 si responde en menos de 5 segundos
    val speedBonus = if (timeElapsedSeconds != null && timeElapsedSeconds < 5) 5 else 0

    // Racha: a partir de 5 respuestas correctas seguidas, multiplicador x2
    val streakMultiplier = if (currentStreak >= 5) 2.0 else 1.0

    val raw = (base * difficultyMultiplier).toInt() + speedBonus
    val total = (raw * streakMultiplier).toInt()

    return AnswerScoreResult(
        basePoints = base,
        speedBonus = speedBonus,
        streakMultiplier = streakMultiplier,
        totalPoints = total
    )
}
