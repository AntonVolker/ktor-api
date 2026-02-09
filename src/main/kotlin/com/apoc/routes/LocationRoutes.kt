package com.apoc.routes

import com.apoc.models.*
import com.apoc.services.LocationService
import com.apoc.db.LocationType
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.*

fun Route.locationRoutes(locationService: LocationService) {
    route("/locations") {
        get {
            val locations = locationService.getAllLocations()
            call.respond(locations)
        }

        get("/{id}") {
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

        get("/search") {
            val lat = call.request.queryParameters["lat"]?.toDoubleOrNull()
            val lon = call.request.queryParameters["lon"]?.toDoubleOrNull()
            val radiusKm = call.request.queryParameters["radiusKm"]?.toDoubleOrNull()

            if (lat == null || lon == null || radiusKm == null) {
                call.respond(HttpStatusCode.BadRequest, "Missing or invalid 'lat', 'lon', or 'radiusKm' query parameters.")
                return@get
            }

            val locations = locationService.findLocationsWithinRadius(lat, lon, radiusKm)
            call.respond(locations)
        }

        post("/parking") {
            val request = call.receive<ParkingSpaceRequest>()
            val parkingSpace = locationService.createParkingSpace(request)
            call.respond(HttpStatusCode.Created, parkingSpace)
        }

        post("/ev-charging") {
            val request = call.receive<EvChargingStationRequest>()
            val evChargingStation = locationService.createEvChargingStation(request)
            call.respond(HttpStatusCode.Created, evChargingStation)
        }

        put("/{id}") {
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
