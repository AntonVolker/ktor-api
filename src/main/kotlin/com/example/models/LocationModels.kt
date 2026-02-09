package com.example.models

import com.example.ChargerType
import com.example.LocationType
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.*

// --- Requests ---

@Serializable
data class LocationRequest(
    val name: String,
    val description: String? = null,
    val latitude: Double, // Re-added for API consistency
    val longitude: Double, // Re-added for API consistency
    val address: String,
    val city: String,
    val state: String? = null,
    val zipCode: String? = null,
    val country: String,
    val type: LocationType
)

@Serializable
data class ParkingSpaceRequest(
    val location: LocationRequest,
    val totalSpaces: Int,
    val availableSpaces: Int,
    val hourlyRate: Double? = null,
    val maxDurationHours: Int? = null,
    val isHandicapAccessible: Boolean,
    val isCovered: Boolean
)

@Serializable
data class EvChargingStationRequest(
    val location: LocationRequest,
    val chargerType: ChargerType,
    val numChargers: Int,
    val chargingSpeedKw: Double,
    val isFastCharging: Boolean,
    val costPerKwh: Double? = null,
    val isOperational: Boolean
)

// --- Responses ---

@Serializable
data class LocationResponse(
    @Contextual val id: UUID,
    val name: String,
    val description: String? = null,
    val latitude: Double, // Kept for API consistency
    val longitude: Double, // Kept for API consistency
    val address: String,
    val city: String,
    val state: String? = null,
    val zipCode: String? = null,
    val country: String,
    val type: LocationType,
    val createdAt: String, // Represent LocalDateTime as String for simplicity in JSON
    val updatedAt: String
)

@Serializable
data class ParkingSpaceResponse(
    @Contextual val id: UUID,
    val name: String,
    val description: String? = null,
    val latitude: Double, // Kept for API consistency
    val longitude: Double, // Kept for API consistency
    val address: String,
    val city: String,
    val state: String? = null,
    val zipCode: String? = null,
    val country: String,
    val totalSpaces: Int,
    val availableSpaces: Int,
    val hourlyRate: Double? = null,
    val maxDurationHours: Int? = null,
    val isHandicapAccessible: Boolean,
    val isCovered: Boolean,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class EvChargingStationResponse(
    @Contextual val id: UUID,
    val name: String,
    val description: String? = null,
    val latitude: Double, // Kept for API consistency
    val longitude: Double, // Kept for API consistency
    val address: String,
    val city: String,
    val state: String? = null,
    val zipCode: String? = null,
    val country: String,
    val chargerType: ChargerType,
    val numChargers: Int,
    val chargingSpeedKw: Double,
    val isFastCharging: Boolean,
    val costPerKwh: Double? = null,
    val isOperational: Boolean,
    val createdAt: String,
    val updatedAt: String
)

// --- Update Requests ---

@Serializable
data class LocationUpdateRequest(
    val name: String? = null,
    val description: String? = null,
    val latitude: Double? = null, // Re-added for API consistency
    val longitude: Double? = null, // Re-added for API consistency
    val address: String? = null,
    val city: String? = null,
    val state: String? = null,
    val zipCode: String? = null,
    val country: String? = null,
    val type: LocationType? = null
)

@Serializable
data class ParkingSpaceUpdateRequest(
    val location: LocationUpdateRequest? = null, // Nested update for base location properties
    val totalSpaces: Int? = null,
    val availableSpaces: Int? = null,
    val hourlyRate: Double? = null,
    val maxDurationHours: Int? = null,
    val isHandicapAccessible: Boolean? = null,
    val isCovered: Boolean? = null
)

@Serializable
data class EvChargingStationUpdateRequest(
    val location: LocationUpdateRequest? = null, // Nested update for base location properties
    val chargerType: ChargerType? = null,
    val numChargers: Int? = null,
    val chargingSpeedKw: Double? = null,
    val isFastCharging: Boolean? = null,
    val costPerKwh: Double? = null,
    val isOperational: Boolean? = null
)
