package com.example

import com.example.models.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import java.util.*

object UUIDSerializer : KSerializer<UUID> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("UUID", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: UUID) {
        encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): UUID {
        return UUID.fromString(decoder.decodeString())
    }
}

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        initDatabase() // Initialize the database
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                serializersModule = SerializersModule {
                    contextual(UUIDSerializer)
                }
            })
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
        get("/locations") {
            val locations = locationService.getAllLocations()
            call.respond(locations)
        }

        get("/locations/{id}") {
            val id = call.parameters["id"]
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "ID parameter is missing")
                return@get
            }

            try {
                val location = locationService.getLocationById(UUID.fromString(id))
                if (location != null) {
                    call.respond(location)
                } else {
                    call.respond(HttpStatusCode.NotFound)
                }
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, "Invalid UUID format")
            }
        }

        post("/locations/parking") {
            val request = call.receive<ParkingSpaceRequest>()
            val parkingSpace = locationService.createParkingSpace(request)
            call.respond(HttpStatusCode.Created, parkingSpace)
        }

        post("/locations/ev-charging") {
            val request = call.receive<EvChargingStationRequest>()
            val evChargingStation = locationService.createEvChargingStation(request)
            call.respond(HttpStatusCode.Created, evChargingStation)
        }

        put("/locations/{id}") {
            val id = call.parameters["id"]
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "ID parameter is missing")
                return@put
            }
            val uuid = try {
                UUID.fromString(id)
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, "Invalid UUID format")
                return@put
            }

            val existingLocation = locationService.getLocationById(uuid)
            if (existingLocation == null) {
                call.respond(HttpStatusCode.NotFound)
                return@put
            }

            when (existingLocation.type) {
                LocationType.PARKING_SPACE -> {
                    val request = call.receive<ParkingSpaceUpdateRequest>()
                    val updated = locationService.updateParkingSpace(uuid, request)
                    if (updated != null) {
                        call.respond(HttpStatusCode.OK, updated)
                    } else {
                        call.respond(HttpStatusCode.NotFound) // Should not happen if existingLocation != null
                    }
                }
                LocationType.EV_CHARGING_STATION -> {
                    val request = call.receive<EvChargingStationUpdateRequest>()
                    val updated = locationService.updateEvChargingStation(uuid, request)
                    if (updated != null) {
                        call.respond(HttpStatusCode.OK, updated)
                    } else {
                        call.respond(HttpStatusCode.NotFound) // Should not happen if existingLocation != null
                    }
                }
                LocationType.GENERIC_POINT_OF_INTEREST -> {
                    val request = call.receive<LocationUpdateRequest>()
                    val updated = locationService.updateLocation(uuid, request)
                    if (updated != null) {
                        call.respond(HttpStatusCode.OK, updated)
                    } else {
                        call.respond(HttpStatusCode.NotFound) // Should not happen if existingLocation != null
                    }
                }
            }
        }

        delete("/locations/{id}") {
            val id = call.parameters["id"]
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "ID parameter is missing")
                return@delete
            }
            val uuid = try {
                UUID.fromString(id)
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, "Invalid UUID format")
                return@delete
            }

            if (locationService.deleteLocation(uuid)) {
                call.respond(HttpStatusCode.NoContent)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }
    }
}
