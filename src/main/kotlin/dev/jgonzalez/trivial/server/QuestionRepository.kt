package dev.jgonzalez.trivial.server

import dev.jgonzalez.trivial.shared.domain.Category
import dev.jgonzalez.trivial.shared.domain.Difficulty
import dev.jgonzalez.trivial.shared.domain.GameConfig
import dev.jgonzalez.trivial.shared.domain.Question
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.InputStreamReader

@Serializable
private data class QuestionDb(
    val questions: List<Question>
)

class QuestionRepository(
    private val jsonResourceName: String = "questions.json"
) {

    private val allQuestions: List<Question>

    init {
        val resourceStream = javaClass.classLoader.getResourceAsStream(jsonResourceName)
            ?: error("Resource $jsonResourceName not found")
        val text = InputStreamReader(resourceStream, Charsets.UTF_8).readText()
        val db = Json { ignoreUnknownKeys = true }
            .decodeFromString<QuestionDb>(text)
        allQuestions = db.questions
        println("SERVER: cargadas ${allQuestions.size} preguntas desde $jsonResourceName")
    }

    fun getRandomQuestions(config: GameConfig): List<Question> {
        // 1) Filtro por categorías y dificultad
        val filtered = allQuestions
            .asSequence()
            .filter { it.category in config.categories }
            .filter {
                config.difficulty == Difficulty.MIXED ||
                        it.difficulty == config.difficulty
            }
            .toList()

        println("SERVER: preguntas filtradas = ${filtered.size} (categorías=${config.categories}, dificultad=${config.difficulty})")

        val baseList =
            if (filtered.isNotEmpty()) {
                filtered
            } else {
                // Si no hay ninguna que cumpla el filtro, usamos TODAS para no dejar la partida vacía
                println("SERVER: no hay preguntas que cumplan el filtro, usando todas las preguntas")
                allQuestions
            }

        // 2) Tomar N aleatorias, pero sin fallar si hay menos de las pedidas
        val n = config.questions.coerceAtMost(baseList.size)
        val result = baseList.shuffled().take(n)

        println("SERVER: devolviendo ${result.size} preguntas para la partida")
        return result
    }
}
