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

import io.ktor.server.plugins.statuspages.*
import com.apoc.models.ErrorResponse

import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm

fun main() {
    val jwtIssuer = System.getenv("JWT_ISSUER") ?: "http://0.0.0.0:8080/"
    val jwtAudience = System.getenv("JWT_AUDIENCE") ?: "apoc-api"
    val jwtRealm = "apoc-api-realm"
    val jwtSecret = System.getenv("JWT_SECRET") ?: "secret"

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
        install(Authentication) {
            jwt("auth-jwt") {
                realm = jwtRealm
                verifier(
                    JWT
                        .require(Algorithm.HMAC256(jwtSecret))
                        .withIssuer(jwtIssuer)
                        .withAudience(jwtAudience)
                        .build()
                )
                validate { credential ->
                    if (credential.payload.audience.contains(jwtAudience)) {
                        JWTPrincipal(credential.payload)
                    } else {
                        null
                    }
                }
                challenge { _, _ ->
                    call.respond(HttpStatusCode.Unauthorized, ErrorResponse(HttpStatusCode.Unauthorized.value, "Token is not valid or has expired"))
                }
            }
        }
        install(StatusPages) {
            exception<Throwable> { call, cause ->
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse(
                        status = HttpStatusCode.InternalServerError.value,
                        message = "An unexpected error occurred",
                        details = cause.localizedMessage
                    )
                )
            }
            status(HttpStatusCode.NotFound) { call, status ->
                call.respond(status, ErrorResponse(status.value, "Resource not found"))
            }
            status(HttpStatusCode.BadRequest) { call, status ->
                call.respond(status, ErrorResponse(status.value, "Bad request"))
            }
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
        
        locationRoutes(locationService)
    }
}
