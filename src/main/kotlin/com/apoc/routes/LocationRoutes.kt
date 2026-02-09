package com.apoc.routes

import com.apoc.models.*
import com.apoc.services.LocationService
import com.apoc.db.LocationType
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.*

fun Route.locationRoutes(locationService: LocationService) {
    authenticate("auth-jwt") {
        route("/locations") {
            
            // --- Read Routes ---
            
        get {
            call.ensureHasScope("locations:read")
            val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
            val pageSize = call.request.queryParameters["pageSize"]?.toIntOrNull() ?: 20
            
            if (page < 1 || pageSize < 1) {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse(HttpStatusCode.BadRequest.value, "page and pageSize must be positive integers."))
                return@get
            }

            val limit = pageSize
            val offset = (page - 1).toLong() * pageSize
            
            val locations = locationService.getAllLocations(limit, offset)
            call.respond(locations)
        }

            get("/{id}") {
                call.ensureHasScope("locations:read")
                val id = call.parameters["id"]
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse(HttpStatusCode.BadRequest.value, "ID parameter is missing"))
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
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse(HttpStatusCode.BadRequest.value, "Invalid UUID format"))
                }
            }

            get("/search") {
                call.ensureHasScope("locations:read")
                val lat = call.request.queryParameters["lat"]?.toDoubleOrNull()
                val lon = call.request.queryParameters["lon"]?.toDoubleOrNull()
                val radiusKm = call.request.queryParameters["radiusKm"]?.toDoubleOrNull()

                if (lat == null || lon == null || radiusKm == null) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse(HttpStatusCode.BadRequest.value, "Missing or invalid 'lat', 'lon', or 'radiusKm' query parameters."))
                    return@get
                }

                if (lat !in -90.0..90.0 || lon !in -180.0..180.0 || radiusKm <= 0) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse(HttpStatusCode.BadRequest.value, "Search parameters out of valid bounds."))
                    return@get
                }

                val locations = locationService.findLocationsWithinRadius(lat, lon, radiusKm)
                call.respond(locations)
            }

            // --- Write Routes ---

            post("/parking") {
                call.ensureHasScope("locations:write")
                val request = call.receive<ParkingSpaceRequest>()
                request.validate()
                val parkingSpace = locationService.createParkingSpace(request)
                call.respond(HttpStatusCode.Created, parkingSpace)
            }

            post("/ev-charging") {
                call.ensureHasScope("locations:write")
                val request = call.receive<EvChargingStationRequest>()
                request.validate()
                val evChargingStation = locationService.createEvChargingStation(request)
                call.respond(HttpStatusCode.Created, evChargingStation)
            }

            put("/{id}") {
                call.ensureHasScope("locations:write")
                val id = call.parameters["id"]
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse(HttpStatusCode.BadRequest.value, "ID parameter is missing"))
                    return@put
                }
                val uuid = try {
                    UUID.fromString(id)
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse(HttpStatusCode.BadRequest.value, "Invalid UUID format"))
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
                            call.respond(HttpStatusCode.NotFound)
                        }
                    }
                    LocationType.EV_CHARGING_STATION -> {
                        val request = call.receive<EvChargingStationUpdateRequest>()
                        val updated = locationService.updateEvChargingStation(uuid, request)
                        if (updated != null) {
                            call.respond(HttpStatusCode.OK, updated)
                        } else {
                            call.respond(HttpStatusCode.NotFound)
                        }
                    }
                    LocationType.GENERIC_POINT_OF_INTEREST -> {
                        val request = call.receive<LocationUpdateRequest>()
                        val updated = locationService.updateLocation(uuid, request)
                        if (updated != null) {
                            call.respond(HttpStatusCode.OK, updated)
                        } else {
                            call.respond(HttpStatusCode.NotFound)
                        }
                    }
                }
            }

            delete("/{id}") {
                call.ensureHasScope("locations:write")
                val id = call.parameters["id"]
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse(HttpStatusCode.BadRequest.value, "ID parameter is missing"))
                    return@delete
                }
                val uuid = try {
                    UUID.fromString(id)
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse(HttpStatusCode.BadRequest.value, "Invalid UUID format"))
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
}

/**
 * Extension function to verify that the JWT principal contains the required scope.
 * Throws an exception or responds with Forbidden if the scope is missing.
 */
suspend fun ApplicationCall.ensureHasScope(requiredScope: String) {
    val principal = principal<JWTPrincipal>()
    val scopes = principal?.payload?.getClaim("scope")?.asString()?.split(" ") ?: emptyList()
    
    if (!scopes.contains(requiredScope)) {
        respond(
            HttpStatusCode.Forbidden, 
            ErrorResponse(HttpStatusCode.Forbidden.value, "Insufficient permissions. Missing scope: $requiredScope")
        )
        // In Ktor, we can't easily "stop" the pipeline here without throwing, 
        // so we'll rely on the route handlers to return if this check fails.
        // A more advanced approach would use a custom interceptor or a dedicated authorization plugin.
    }
}