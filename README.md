Enlace Youtube: https://youtu.be/5r7VUHX47-U

# Trivial Multijugador – Servidor

Servidor Ktor que expone un trivial por WebSocket para un cliente de escritorio en Kotlin/Compose. Gestiona la lógica de partida en modo PVE (un jugador) con filtrado de preguntas, cálculo de puntuación y envío de resultados al cliente.

## Características

- Servidor HTTP + WebSocket usando Ktor.
- Punto WebSocket en `/trivia`.
- Carga de preguntas desde `resources/questions.json`.
- Filtro de preguntas por:
  - Categoría.
  - Dificultad.
  - Número de preguntas solicitadas.
- Lógica de partida:
  - Selección aleatoria de preguntas que cumplan el filtro.
  - Límite de tiempo por pregunta (enviado al cliente).
  - Cálculo de puntos por respuesta (base, dificultad, rapidez, racha).
  - Multiplicador x2 a partir de racha 5.
- Comunicación en JSON con Kotlinx Serialization:
  - Mensajes del cliente: creación de partida, envío de respuesta.
  - Mensajes del servidor: pregunta, resultado de respuesta, actualización de puntuación y fin de partida.

## Requisitos

- JDK 17
- Gradle (se usa el *wrapper* incluido en el proyecto)

## Puesta en marcha

```bash
# Clonar el repositorio del servidor
git clone https://github.com/tu-usuario/trivial-server.git
cd trivial-server

# Ejecutar el servidor
./gradlew :dev.jgonzalez.trivial.server:run
