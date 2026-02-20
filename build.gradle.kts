plugins {
    kotlin("jvm") version "2.0.0"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.0.0"
}

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    val ktorVersion = "3.0.0"

    // Ktor server
    implementation("io.ktor:ktor-server-core-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-netty-jvm:$ktorVersion")

    // WebSockets
    implementation("io.ktor:ktor-server-websockets-jvm:$ktorVersion")

    // JSON (kotlinx.serialization) para HTTP y WebSockets
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation-jvm:$ktorVersion")

    // Serialización Kotlin
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.0")

    testImplementation(kotlin("test"))
}
