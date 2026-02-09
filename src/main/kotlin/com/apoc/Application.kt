package com.apoc

import com.apoc.db.initDatabase
import com.apoc.routes.locationRoutes
import com.apoc.services.LocationService
import com.apoc.serializers.UUIDSerializer
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import java.util.*

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        initDatabase()
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                serializersModule = SerializersModule {
                    contextual(UUIDSerializer)
                }
            })
        }
        install(CORS) {
            allowHost("antonvolker.github.io", schemes = listOf("https"))
            allowMethod(HttpMethod.Get)
            allowMethod(HttpMethod.Post)
            allowMethod(HttpMethod.Put)
            allowMethod(HttpMethod.Delete)
            allowMethod(HttpMethod.Options)
            allowHeader(HttpHeaders.ContentType)
            allowHeader(HttpHeaders.Accept)
        }
        configureRouting()
    }.start(wait = true)
}

fun Application.configureRouting() {
    val locationService = LocationService()

    routing {
        get("/test") {
            System.out.println("TEST_LOG: Reached /test endpoint!")
            application.log.info("TEST_LOG: Responding with: Hello from Ktor /test!")
            call.respondText("Hello from Ktor /test!")
        }
        
        // Register the modularized location routes
        locationRoutes(locationService)
    }
}