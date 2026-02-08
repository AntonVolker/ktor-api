plugins {
    kotlin("jvm") version "2.0.21"
    kotlin("plugin.serialization") version "2.0.21"
    // Back to the original Ktor Gradle plugin version
    id("io.ktor.plugin") version "3.0.2"
}

kotlin {
    jvmToolchain(21)
}

group = "com.example"
version = "0.0.1"

application {
    // This is what the original (working) project used
    mainClass.set("com.example.ApplicationKt")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-server-core-jvm")
    implementation("io.ktor:ktor-server-netty-jvm")
    implementation("io.ktor:ktor-server-content-negotiation-jvm")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm")
    implementation("ch.qos.logback:logback-classic:1.5.16")

    // Exposed ORM
    implementation("org.jetbrains.exposed:exposed-core:0.52.0")
    implementation("org.jetbrains.exposed:exposed-dao:0.52.0")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.52.0")
    implementation("org.jetbrains.exposed:exposed-java-time:0.52.0")

    // PostgreSQL Driver
    implementation("org.postgresql:postgresql:42.7.3")

    // HikariCP for connection pooling
    implementation("com.zaxxer:HikariCP:5.1.0")
}
