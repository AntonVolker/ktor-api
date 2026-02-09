package com.apoc.services

import com.apoc.models.*
import com.apoc.db.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.util.*

class LocationService {

    private val logger = LoggerFactory.getLogger(LocationService::class.java)

    suspend fun createLocation(request: LocationRequest): LocationResponse = dbQuery {
        logger.info("Attempting to create generic location: {}", request.name)
        val newLocationId = Locations.insertAndGetId {
            it[name] = request.name
            it[description] = request.description
            it[geom] = "POINT(${request.longitude} ${request.latitude})"
            it[address] = request.address
            it[city] = request.city
            it[state] = request.state
            it[zipCode] = request.zipCode
            it[country] = request.country
            it[type] = request.type
            it[createdAt] = LocalDateTime.now()
            it[updatedAt] = LocalDateTime.now()
        }
        val response = Locations.select(Locations.columns)
            .where { Locations.id eq newLocationId }
            .single()
            .toLocationResponse()
        logger.info("Created generic location with ID: {}", response.id)
        response
    }

    suspend fun createParkingSpace(request: ParkingSpaceRequest): ParkingSpaceResponse = dbQuery {
        logger.info("Attempting to create parking space: {}", request.location.name)
        val newLocationId = Locations.insertAndGetId {
            it[name] = request.location.name
            it[description] = request.location.description
            it[geom] = "POINT(${request.location.longitude} ${request.location.latitude})"
            it[address] = request.location.address
            it[city] = request.location.city
            it[state] = request.location.state
            it[zipCode] = request.location.zipCode
            it[country] = request.location.country
            it[type] = LocationType.PARKING_SPACE
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
            .select(Locations.columns + ParkingSpaces.columns)
            .where { Locations.id eq newLocationId }
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
            it[geom] = "POINT(${request.location.longitude} ${request.location.latitude})"
            it[address] = request.location.address
            it[city] = request.location.city
            it[state] = request.location.state
            it[zipCode] = request.location.zipCode
            it[country] = request.location.country
            it[type] = LocationType.EV_CHARGING_STATION
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
            .select(Locations.columns + EvChargingStations.columns)
            .where { Locations.id eq newLocationId }
            .single()
            .toEvChargingStationResponse()
        logger.info("Created EV charging station with ID: {}", response.id)
        response
    }

    suspend fun getLocationById(id: UUID): LocationResponse? = dbQuery {
        logger.info("Attempting to get location by ID: {}", id)
        (Locations.selectAll().where { Locations.id eq id } as Query)
            .singleOrNull()
            ?.toLocationResponse()
    }

    suspend fun getParkingSpaceById(id: UUID): ParkingSpaceResponse? = dbQuery {
        logger.info("Attempting to get parking space by ID: {}", id)
        ((Locations innerJoin ParkingSpaces)
            .selectAll().where { Locations.id eq id } as Query)
            .singleOrNull()
            ?.toParkingSpaceResponse()
    }

    suspend fun getEvChargingStationById(id: UUID): EvChargingStationResponse? = dbQuery {
        logger.info("Attempting to get EV charging station by ID: {}", id)
        ((Locations innerJoin EvChargingStations)
            .selectAll().where { Locations.id eq id } as Query)
            .singleOrNull()
            ?.toEvChargingStationResponse()
    }

    suspend fun getAllLocations(): List<LocationResponse> = dbQuery {
        logger.info("Attempting to get all locations")
        Locations.selectAll().map { it.toLocationResponse() }
    }

    suspend fun findLocationsWithinRadius(latitude: Double, longitude: Double, radiusKm: Double): List<LocationResponse> = dbQuery {
        logger.info("Attempting to find locations within {}km of ({}, {})", radiusKm, latitude, longitude)
        val center = CenterPoint(longitude, latitude)
        Locations.selectAll()
            .where { DWithinOp(Locations.geom, center, radiusKm * 1000.0) }
            .map { it.toLocationResponse() }
    }

    class DWithinOp(val geom: Expression<*>, val center: CenterPoint, val distance: Double) : Op<Boolean>() {
        override fun toQueryBuilder(queryBuilder: QueryBuilder) {
            queryBuilder.append("ST_DWithin(")
            queryBuilder.append(geom)
            queryBuilder.append(", ST_SetSRID(ST_Point(")
            queryBuilder.registerArgument(DoubleColumnType(), center.lon)
            queryBuilder.append(", ")
            queryBuilder.registerArgument(DoubleColumnType(), center.lat)
            queryBuilder.append("), 4326), ")
            queryBuilder.registerArgument(DoubleColumnType(), distance)
            queryBuilder.append(")")
        }
    }
    data class CenterPoint(val lon: Double, val lat: Double)

    suspend fun updateLocation(id: UUID, request: LocationUpdateRequest): LocationResponse? = dbQuery {
        Locations.update({ Locations.id eq id }) {
            request.name?.let { name -> it[Locations.name] = name }
            request.description?.let { description -> it[Locations.description] = description }
            if (request.latitude != null && request.longitude != null) {
                it[Locations.geom] = "POINT(${request.longitude} ${request.latitude})"
            }
            request.address?.let { address -> it[Locations.address] = address }
            request.city?.let { city -> it[Locations.city] = city }
            request.state?.let { state -> it[Locations.state] = state }
            request.zipCode?.let { zipCode -> it[Locations.zipCode] = zipCode }
            request.country?.let { country -> it[Locations.country] = country }
            request.type?.let { type -> it[Locations.type] = type }
            it[Locations.updatedAt] = LocalDateTime.now()
        }
        getLocationById(id)
    }

    suspend fun updateParkingSpace(id: UUID, request: ParkingSpaceUpdateRequest): ParkingSpaceResponse? = dbQuery {
        request.location?.let { updateLocation(id, it) }
        ParkingSpaces.update({ ParkingSpaces.locationId eq id }) {
            request.totalSpaces?.let { totalSpaces -> it[ParkingSpaces.totalSpaces] = totalSpaces }
            request.availableSpaces?.let { availableSpaces -> it[ParkingSpaces.availableSpaces] = availableSpaces }
            request.hourlyRate?.let { hourlyRate -> it[ParkingSpaces.hourlyRate] = hourlyRate }
            request.maxDurationHours?.let { maxDurationHours -> it[ParkingSpaces.maxDurationHours] = maxDurationHours }
            request.isHandicapAccessible?.let { isHandicapAccessible -> it[ParkingSpaces.isHandicapAccessible] = isHandicapAccessible }
            request.isCovered?.let { isCovered -> it[ParkingSpaces.isCovered] = isCovered }
        }
        getParkingSpaceById(id)
    }

    suspend fun updateEvChargingStation(id: UUID, request: EvChargingStationUpdateRequest): EvChargingStationResponse? = dbQuery {
        request.location?.let { updateLocation(id, it) }
        EvChargingStations.update({ EvChargingStations.locationId eq id }) {
            request.chargerType?.let { chargerType -> it[EvChargingStations.chargerType] = chargerType }
            request.numChargers?.let { numChargers -> it[EvChargingStations.numChargers] = numChargers }
            request.chargingSpeedKw?.let { chargingSpeedKw -> it[EvChargingStations.chargingSpeedKw] = chargingSpeedKw }
            request.isFastCharging?.let { isFastCharging -> it[EvChargingStations.isFastCharging] = isFastCharging }
            request.costPerKwh?.let { costPerKwh -> it[EvChargingStations.costPerKwh] = costPerKwh }
            request.isOperational?.let { isOperational -> it[EvChargingStations.isOperational] = isOperational }
        }
        getEvChargingStationById(id)
    }

    suspend fun deleteLocation(id: UUID): Boolean = dbQuery {
        Locations.deleteWhere { Locations.id eq id } > 0
    }

    private fun ResultRow.toLocationResponse(): LocationResponse {
        val geomValue = this[Locations.geom]
        val (lon, lat) = parseGeomValue(geomValue)
        return LocationResponse(
            id = this[Locations.id].value,
            name = this[Locations.name],
            description = this[Locations.description],
            latitude = lat,
            longitude = lon,
            address = this[Locations.address],
            city = this[Locations.city],
            state = this[Locations.state],
            zipCode = this[Locations.zipCode],
            country = this[Locations.country],
            type = this[Locations.type],
            createdAt = this[Locations.createdAt].toString(),
            updatedAt = this[Locations.updatedAt].toString()
        )
    }

    private fun ResultRow.toParkingSpaceResponse(): ParkingSpaceResponse {
        val geomValue = this[Locations.geom]
        val (lon, lat) = parseGeomValue(geomValue)
        return ParkingSpaceResponse(
            id = this[Locations.id].value,
            name = this[Locations.name],
            description = this[Locations.description],
            latitude = lat,
            longitude = lon,
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
    }

    private fun ResultRow.toEvChargingStationResponse(): EvChargingStationResponse {
        val geomValue = this[Locations.geom]
        val (lon, lat) = parseGeomValue(geomValue)
        return EvChargingStationResponse(
            id = this[Locations.id].value,
            name = this[Locations.name],
            description = this[Locations.description],
            latitude = lat,
            longitude = lon,
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

    private fun parseGeomValue(value: String): Pair<Double, Double> {
        // Handle both WKT "POINT(lon lat)" and EWKB Hex strings
        return if (value.startsWith("0101")) {
            parseEwkbHex(value)
        } else {
            val coordinates = value.substringAfter("(").substringBefore(")")
                .trim()
                .split(" ")
            coordinates[0].toDouble() to coordinates[1].toDouble()
        }
    }

    private fun parseEwkbHex(hex: String): Pair<Double, Double> {
        val xHex = hex.substring(18, 34)
        val yHex = hex.substring(34, 50)
        
        fun hexToDouble(h: String): Double {
            val reversed = h.chunked(2).reversed().joinToString("")
            val longBits = java.lang.Long.parseUnsignedLong(reversed, 16)
            return java.lang.Double.longBitsToDouble(longBits)
        }
        
        return hexToDouble(xHex) to hexToDouble(yHex)
    }
}
