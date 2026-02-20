package dev.jgonzalez.trivial.server

import dev.jgonzalez.trivial.shared.domain.TurnMode
import dev.jgonzalez.trivial.shared.domain.Category
import dev.jgonzalez.trivial.shared.domain.Difficulty
import dev.jgonzalez.trivial.shared.domain.GameConfig
import dev.jgonzalez.trivial.shared.domain.GameMode
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import kotlinx.serialization.json.Json
import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import kotlin.time.Duration.Companion.seconds
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import dev.jgonzalez.trivial.server.handleTriviaSession
import dev.jgonzalez.trivial.server.QuestionRepository


fun main() {
    embeddedServer(
        Netty,
        port = 8080,
        module = Application::module
    ).start(wait = true)
}

fun Application.module() {
    val questionRepository = QuestionRepository()

    install(WebSockets)
    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true })
    }

    routing {
        get("/") { call.respondText("Trivia server running") }

        webSocket("/trivia") {
            println("WS Nueva conexión")
            handleTriviaSession(questionRepository)
        }
    }
}


