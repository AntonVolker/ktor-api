package com.example

import com.example.models.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.slf4j.LoggerFactory // Import LoggerFactory
import java.time.LocalDateTime
import java.util.*

class LocationService {

    private val logger = LoggerFactory.getLogger(LocationService::class.java) // Logger instance

    suspend fun createLocation(request: LocationRequest): LocationResponse = dbQuery {
        logger.info("Attempting to create generic location: {}", request.name)
        val newLocationId = Locations.insertAndGetId {
            it[name] = request.name
            it[description] = request.description
            it[latitude] = request.latitude
            it[longitude] = request.longitude
            it[address] = request.address
            it[city] = request.city
            it[state] = request.state
            it[zipCode] = request.zipCode
            it[country] = request.country
            it[type] = request.type
            it[createdAt] = LocalDateTime.now()
            it[updatedAt] = LocalDateTime.now()
        }
        val response = Locations.select(Locations.id eq newLocationId).single().toLocationResponse()
        logger.info("Created generic location with ID: {}", response.id)
        response
    }

    suspend fun createParkingSpace(request: ParkingSpaceRequest): ParkingSpaceResponse = dbQuery {
        logger.info("Attempting to create parking space: {}", request.location.name)
        val newLocationId = Locations.insertAndGetId {
            it[name] = request.location.name
            it[description] = request.location.description
            it[latitude] = request.location.latitude
            it[longitude] = request.location.longitude
            it[address] = request.location.address
            it[city] = request.location.city
            it[state] = request.location.state
            it[zipCode] = request.location.zipCode
            it[country] = request.location.country
            it[type] = LocationType.PARKING_SPACE // Ensure correct type
            it[createdAt] = LocalDateTime.now()
            it[updatedAt] = LocalDateTime.now()
        }

        ParkingSpaces.insert {
            it[locationId] = newLocationId
            it[totalSpaces] = request.totalSpaces
            it[availableSpaces] = request.availableSpaces
            it[hourlyRate] = request.hourlyRate
            it[maxDurationHours] = request.maxDurationHours
            it[isHandicapAccessible] = request.isHandicapAccessible
            it[isCovered] = request.isCovered
        }
        val response = (Locations innerJoin ParkingSpaces)
            .select(Locations.id eq newLocationId)
            .single()
            .toParkingSpaceResponse()
        logger.info("Created parking space with ID: {}", response.id)
        response
    }

    suspend fun createEvChargingStation(request: EvChargingStationRequest): EvChargingStationResponse = dbQuery {
        logger.info("Attempting to create EV charging station: {}", request.location.name)
        val newLocationId = Locations.insertAndGetId {
            it[name] = request.location.name
            it[description] = request.location.description
            it[latitude] = request.location.latitude
            it[longitude] = request.location.longitude
            it[address] = request.location.address
            it[city] = request.location.city
            it[state] = request.location.state
            it[zipCode] = request.location.zipCode
            it[country] = request.location.country
            it[type] = LocationType.EV_CHARGING_STATION // Ensure correct type
            it[createdAt] = LocalDateTime.now()
            it[updatedAt] = LocalDateTime.now()
        }

        EvChargingStations.insert {
            it[locationId] = newLocationId
            it[chargerType] = request.chargerType
            it[numChargers] = request.numChargers
            it[chargingSpeedKw] = request.chargingSpeedKw
            it[isFastCharging] = request.isFastCharging
            it[costPerKwh] = request.costPerKwh
            it[isOperational] = request.isOperational
        }
        val response = (Locations innerJoin EvChargingStations)
            .select(Locations.id eq newLocationId)
            .single()
            .toEvChargingStationResponse()
        logger.info("Created EV charging station with ID: {}", response.id)
        response
    }

    suspend fun getLocationById(id: UUID): LocationResponse? = dbQuery {
        logger.info("Attempting to get location by ID: {}", id)
        val location = Locations.select(Locations.id eq id)
            .singleOrNull()
            ?.toLocationResponse()
        if (location != null) {
            logger.info("Found location with ID: {}", id)
        } else {
            logger.warn("Location with ID: {} not found", id)
        }
        location
    }

    suspend fun getParkingSpaceById(id: UUID): ParkingSpaceResponse? = dbQuery {
        logger.info("Attempting to get parking space by ID: {}", id)
        val parkingSpace = (Locations innerJoin ParkingSpaces)
            .select(Locations.id eq id)
            .singleOrNull()
            ?.toParkingSpaceResponse()
        if (parkingSpace != null) {
            logger.info("Found parking space with ID: {}", id)
        } else {
            logger.warn("Parking space with ID: {} not found", id)
        }
        parkingSpace
    }

    suspend fun getEvChargingStationById(id: UUID): EvChargingStationResponse? = dbQuery {
        logger.info("Attempting to get EV charging station by ID: {}", id)
        val evChargingStation = (Locations innerJoin EvChargingStations)
            .select(Locations.id eq id)
            .singleOrNull()
            ?.toEvChargingStationResponse()
        if (evChargingStation != null) {
            logger.info("Found EV charging station with ID: {}", id)
        } else {
            logger.warn("EV charging station with ID: {} not found", id)
        }
        evChargingStation
    }

    suspend fun getAllLocations(): List<LocationResponse> = dbQuery {
        logger.info("Attempting to get all locations")
        val locations = Locations.selectAll().map { it.toLocationResponse() }
        logger.info("Retrieved {} locations", locations.size)
        locations
    }

    suspend fun updateLocation(id: UUID, request: LocationUpdateRequest): LocationResponse? = dbQuery {
        logger.info("Attempting to update generic location with ID: {}", id)
        val updatedRows = Locations.update({ Locations.id eq id }) {
            request.name?.let { name -> it[Locations.name] = name }
            request.description?.let { description -> it[Locations.description] = description }
            request.latitude?.let { latitude -> it[Locations.latitude] = latitude }
            request.longitude?.let { longitude -> it[Locations.longitude] = longitude }
            request.address?.let { address -> it[Locations.address] = address }
            request.city?.let { city -> it[Locations.city] = city }
            request.state?.let { state -> it[Locations.state] = state }
            request.zipCode?.let { zipCode -> it[Locations.zipCode] = zipCode }
            request.country?.let { country -> it[Locations.country] = country }
            request.type?.let { type -> it[Locations.type] = type }
            it[Locations.updatedAt] = LocalDateTime.now()
        }

        if (updatedRows > 0) {
            val updatedLocation = Locations.select(Locations.id eq id).singleOrNull()?.toLocationResponse()
            if (updatedLocation != null) {
                logger.info("Updated generic location with ID: {}", id)
            } else {
                logger.warn("Updated generic location with ID: {} but could not retrieve it.", id)
            }
            updatedLocation
        } else {
            logger.warn("Failed to find or update generic location with ID: {}", id)
            null
        }
    }

    suspend fun updateParkingSpace(id: UUID, request: ParkingSpaceUpdateRequest): ParkingSpaceResponse? = dbQuery {
        logger.info("Attempting to update parking space with ID: {}", id)
        val updatedLocationRows = request.location?.let { locUpdate ->
            Locations.update({ Locations.id eq id }) {
                locUpdate.name?.let { name -> it[Locations.name] = name }
                locUpdate.description?.let { description -> it[Locations.description] = description }
                locUpdate.latitude?.let { latitude -> it[Locations.latitude] = latitude }
                locUpdate.longitude?.let { longitude -> it[Locations.longitude] = longitude }
                locUpdate.address?.let { address -> it[Locations.address] = address }
                locUpdate.city?.let { city -> it[Locations.city] = city }
                locUpdate.state?.let { state -> it[Locations.state] = state }
                locUpdate.zipCode?.let { zipCode -> it[Locations.zipCode] = zipCode }
                locUpdate.country?.let { country -> it[Locations.country] = country }
                locUpdate.type?.let { type -> it[Locations.type] = type }
                it[Locations.updatedAt] = LocalDateTime.now()
            }
        } ?: 0 // If no location updates, consider 0 rows updated

        val updatedParkingSpaceRows = ParkingSpaces.update({ ParkingSpaces.locationId eq id }) {
            request.totalSpaces?.let { totalSpaces -> it[ParkingSpaces.totalSpaces] = totalSpaces }
            request.availableSpaces?.let { availableSpaces -> it[ParkingSpaces.availableSpaces] = availableSpaces }
            request.hourlyRate?.let { hourlyRate -> it[ParkingSpaces.hourlyRate] = hourlyRate }
            request.maxDurationHours?.let { maxDurationHours -> it[ParkingSpaces.maxDurationHours] = maxDurationHours }
            request.isHandicapAccessible?.let { isHandicapAccessible -> it[ParkingSpaces.isHandicapAccessible] = isHandicapAccessible }
            request.isCovered?.let { isCovered -> it[ParkingSpaces.isCovered] = isCovered }
        }

        if (updatedLocationRows > 0 || updatedParkingSpaceRows > 0) {
            val updatedParkingSpace = (Locations innerJoin ParkingSpaces)
                .select(Locations.id eq id)
                .singleOrNull()
                ?.toParkingSpaceResponse()
            if (updatedParkingSpace != null) {
                logger.info("Updated parking space with ID: {}", id)
            } else {
                logger.warn("Updated parking space with ID: {} but could not retrieve it.", id)
            }
            updatedParkingSpace
        } else {
            logger.warn("Failed to find or update parking space with ID: {}", id)
            null
        }
    }

    suspend fun updateEvChargingStation(id: UUID, request: EvChargingStationUpdateRequest): EvChargingStationResponse? = dbQuery {
        logger.info("Attempting to update EV charging station with ID: {}", id)
        val updatedLocationRows = request.location?.let { locUpdate ->
            Locations.update({ Locations.id eq id }) {
                locUpdate.name?.let { name -> it[Locations.name] = name }
                locUpdate.description?.let { description -> it[Locations.description] = description }
                locUpdate.latitude?.let { latitude -> it[Locations.latitude] = latitude }
                locUpdate.longitude?.let { longitude -> it[Locations.longitude] = longitude }
                locUpdate.address?.let { address -> it[Locations.address] = address }
                locUpdate.city?.let { city -> it[Locations.city] = city }
                locUpdate.state?.let { state -> it[Locations.state] = state }
                locUpdate.zipCode?.let { zipCode -> it[Locations.zipCode] = zipCode }
                locUpdate.country?.let { country -> it[Locations.country] = country }
                locUpdate.type?.let { type -> it[Locations.type] = type }
                it[Locations.updatedAt] = LocalDateTime.now()
            }
        } ?: 0

        val updatedEvChargingStationRows = EvChargingStations.update({ EvChargingStations.locationId eq id }) {
            request.chargerType?.let { chargerType -> it[EvChargingStations.chargerType] = chargerType }
            request.numChargers?.let { numChargers -> it[EvChargingStations.numChargers] = numChargers }
            request.chargingSpeedKw?.let { chargingSpeedKw -> it[EvChargingStations.chargingSpeedKw] = chargingSpeedKw }
            request.isFastCharging?.let { isFastCharging -> it[EvChargingStations.isFastCharging] = isFastCharging }
            request.costPerKwh?.let { costPerKwh -> it[EvChargingStations.costPerKwh] = costPerKwh }
            request.isOperational?.let { isOperational -> it[EvChargingStations.isOperational] = isOperational }
        }

        if (updatedLocationRows > 0 || updatedEvChargingStationRows > 0) {
            val updatedEvChargingStation = (Locations innerJoin EvChargingStations)
                .select(Locations.id eq id)
                .singleOrNull()
                ?.toEvChargingStationResponse()
            if (updatedEvChargingStation != null) {
                logger.info("Updated EV charging station with ID: {}", id)
            } else {
                logger.warn("Updated EV charging station with ID: {} but could not retrieve it.", id)
            }
            updatedEvChargingStation
        } else {
            logger.warn("Failed to find or update EV charging station with ID: {}", id)
            null
        }
    }

    suspend fun deleteLocation(id: UUID): Boolean = dbQuery {
        logger.info("Attempting to delete location with ID: {}", id)
        val deletedRows = Locations.deleteWhere { Locations.id eq id }
        if (deletedRows > 0) {
            logger.info("Deleted location with ID: {}", id)
        } else {
            logger.warn("Failed to find or delete location with ID: {}", id)
        }
        deletedRows > 0
    }

    private fun ResultRow.toLocationResponse() = LocationResponse(
        id = this[Locations.id].value,
        name = this[Locations.name],
        description = this[Locations.description],
        latitude = this[Locations.latitude],
        longitude = this[Locations.longitude],
        address = this[Locations.address],
        city = this[Locations.city],
        state = this[Locations.state],
        zipCode = this[Locations.zipCode],
        country = this[Locations.country],
        type = this[Locations.type],
        createdAt = this[Locations.createdAt].toString(),
        updatedAt = this[Locations.updatedAt].toString()
    )

    private fun ResultRow.toParkingSpaceResponse() = ParkingSpaceResponse(
        id = this[Locations.id].value,
        name = this[Locations.name],
        description = this[Locations.description],
        latitude = this[Locations.latitude],
        longitude = this[Locations.longitude],
        address = this[Locations.address],
        city = this[Locations.city],
        state = this[Locations.state],
        zipCode = this[Locations.zipCode],
        country = this[Locations.country],
        totalSpaces = this[ParkingSpaces.totalSpaces],
        availableSpaces = this[ParkingSpaces.availableSpaces],
        hourlyRate = this[ParkingSpaces.hourlyRate],
        maxDurationHours = this[ParkingSpaces.maxDurationHours],
        isHandicapAccessible = this[ParkingSpaces.isHandicapAccessible],
        isCovered = this[ParkingSpaces.isCovered],
        createdAt = this[Locations.createdAt].toString(),
        updatedAt = this[Locations.updatedAt].toString()
    )

    private fun ResultRow.toEvChargingStationResponse() = EvChargingStationResponse(
        id = this[Locations.id].value,
        name = this[Locations.name],
        description = this[Locations.description],
        latitude = this[Locations.latitude],
        longitude = this[Locations.longitude],
        address = this[Locations.address],
        city = this[Locations.city],
        state = this[Locations.state],
        zipCode = this[Locations.zipCode],
        country = this[Locations.country],
        chargerType = this[EvChargingStations.chargerType],
        numChargers = this[EvChargingStations.numChargers],
        chargingSpeedKw = this[EvChargingStations.chargingSpeedKw],
        isFastCharging = this[EvChargingStations.isFastCharging],
        costPerKwh = this[EvChargingStations.costPerKwh],
        isOperational = this[EvChargingStations.isOperational],
        createdAt = this[Locations.createdAt].toString(),
        updatedAt = this[Locations.updatedAt].toString()
    )
}