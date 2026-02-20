package dev.jgonzalez.trivial.server

import dev.jgonzalez.trivial.shared.domain.*
import io.ktor.server.websocket.DefaultWebSocketServerSession
import io.ktor.websocket.CloseReason
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.coroutines.channels.consumeEach
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.decodeFromJsonElement

private val json = Json { ignoreUnknownKeys = true }

suspend fun DefaultWebSocketServerSession.handleTriviaSession(
    questionRepository: QuestionRepository
) {
    var questions: List<Question> = emptyList()
    var currentIndex = -1
    var score = 0
    var streak = 0
    var correctAnswers = 0
    var config: GameConfig? = null
    var playerName: String = "Player"

    incoming.consumeEach { frame ->
        val text = (frame as? Frame.Text)?.readText() ?: return@consumeEach
        println("SERVER: recibido -> $text")

        val root = json.parseToJsonElement(text).jsonObject
        val type = root["type"]?.jsonPrimitive?.contentOrNull

        when (type) {
            "dev.jgonzalez.trivial.shared.domain.ClientMessage.CreateTrivia" -> {
                val clientMsg = json.decodeFromJsonElement(
                    ClientMessage.CreateTrivia.serializer(),
                    root
                )

                playerName = clientMsg.playerName ?: "Player"
                val cfg = GameConfig(
                    mode = clientMsg.mode,
                    questions = clientMsg.questions,
                    categories = clientMsg.categories,
                    difficulty = clientMsg.difficulty,
                    timeLimitSeconds = clientMsg.timeLimit,
                    turnMode = clientMsg.turnMode
                )
                config = cfg
                questions = questionRepository.getRandomQuestions(cfg)
                println("SERVER: preguntas seleccionadas = ${questions.size}")
                currentIndex = 0
                score = 0
                streak = 0
                correctAnswers = 0

                if (questions.isEmpty()) {
                    println("SERVER: no hay preguntas, no se puede iniciar partida")
                    return@consumeEach
                }

                val q = questions[currentIndex]
                println("SERVER: enviando pregunta inicial id=${q.id}")
                val questionMsg = ServerMessage.QuestionMsg(
                    id = q.id,
                    category = q.category,
                    difficulty = q.difficulty,
                    question = q.question,
                    options = q.options,
                    timeLimit = cfg.timeLimitSeconds,
                    index = currentIndex + 1,
                    total = questions.size
                )
                sendSerialized(questionMsg)
            }

            "dev.jgonzalez.trivial.shared.domain.ClientMessage.Answer" -> {
                val clientMsg = json.decodeFromJsonElement(
                    ClientMessage.Answer.serializer(),
                    root
                )

                if (currentIndex !in questions.indices) return@consumeEach
                val q = questions[currentIndex]

                val isCorrect = clientMsg.selectedOption == q.correctAnswer
                if (isCorrect) {
                    streak++
                    correctAnswers++
                } else {
                    streak = 0
                }

                val res = calculateScore(
                    isCorrect = isCorrect,
                    difficulty = q.difficulty,
                    timeElapsedSeconds = clientMsg.timeElapsed,
                    currentStreak = streak
                )
                score += res.totalPoints

                val answerResult = ServerMessage.AnswerResultMsg(
                    questionId = q.id,
                    correct = isCorrect,
                    correctAnswer = q.correctAnswer,
                    points = res.totalPoints,
                    explanation = q.explanation
                )
                sendSerialized(answerResult)

                val scoreUpdate = ServerMessage.ScoreUpdateMsg(
                    players = listOf(PlayerScoreDto(playerName, score, streak))
                )
                sendSerialized(scoreUpdate)

                currentIndex++
                val cfg = config
                if (currentIndex in questions.indices) {
                    val next = questions[currentIndex]
                    val nextMsg = ServerMessage.QuestionMsg(
                        id = next.id,
                        category = next.category,
                        difficulty = next.difficulty,
                        question = next.question,
                        options = next.options,
                        timeLimit = cfg?.timeLimitSeconds,
                        index = currentIndex + 1,
                        total = questions.size
                    )
                    sendSerialized(nextMsg)
                } else {
                    val endMsg = ServerMessage.GameEndMsg(
                        winner = playerName,
                        finalScores = listOf(PlayerScoreDto(playerName, score, streak)),
                        correctAnswers = mapOf(playerName to correctAnswers)
                    )
                    sendSerialized(endMsg)
                    close(CloseReason(CloseReason.Codes.NORMAL, "Game finished"))
                }
            }

            else -> {
                println("SERVER: mensaje con type desconocido: $type")
            }
        }

    }
}

private suspend fun DefaultWebSocketServerSession.sendSerialized(msg: ServerMessage) {
    val text = json.encodeToString(ServerMessage.serializer(), msg)
    send(Frame.Text(text))
}
