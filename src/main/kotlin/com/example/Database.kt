package com.example

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.BooleanColumnType
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
import java.util.*

// Enum for location types
enum class LocationType {
    PARKING_SPACE,
    EV_CHARGING_STATION,
    GENERIC_POINT_OF_INTEREST
}

// Enum for charger types (EV_CHARGING_STATION specific)
enum class ChargerType {
    TYPE_2_AC,
    CCS_COMBO_2_DC,
    CHADEMO_DC,
    TESLA_SUPERCHARGER
}

object Locations : UUIDTable("locations") {
    val name = varchar("name", 255)
    val description = text("description").nullable()
    val latitude = double("latitude")
    val longitude = double("longitude")
    val address = varchar("address", 255)
    val city = varchar("city", 255)
    val state = varchar("state", 255).nullable()
    val zipCode = varchar("zip_code", 20).nullable()
    val country = varchar("country", 255)
    val type = enumerationByName("type", 50, LocationType::class)
    val createdAt = datetime("created_at").clientDefault { LocalDateTime.now() }
    val updatedAt = datetime("updated_at").clientDefault { LocalDateTime.now() }
}

object ParkingSpaces : UUIDTable("parking_spaces") {
    val locationId = reference("location_id", Locations, onDelete = org.jetbrains.exposed.sql.ReferenceOption.CASCADE)
    val totalSpaces = integer("total_spaces")
    val availableSpaces = integer("available_spaces")
    val hourlyRate = double("hourly_rate").nullable()
    val maxDurationHours = integer("max_duration_hours").nullable()
    val isHandicapAccessible = registerColumn<Boolean>("is_handicap_accessible", org.jetbrains.exposed.sql.BooleanColumnType())
    val isCovered = registerColumn<Boolean>("is_covered", org.jetbrains.exposed.sql.BooleanColumnType())
}

object EvChargingStations : UUIDTable("ev_charging_stations") {
    val locationId = reference("location_id", Locations, onDelete = org.jetbrains.exposed.sql.ReferenceOption.CASCADE)
    val chargerType = enumerationByName("charger_type", 50, ChargerType::class)
    val numChargers = integer("num_chargers")
    val chargingSpeedKw = double("charging_speed_kw")
    val isFastCharging = registerColumn<Boolean>("is_fast_charging", org.jetbrains.exposed.sql.BooleanColumnType())
    val costPerKwh = double("cost_per_kwh").nullable()
    val isOperational = registerColumn<Boolean>("is_operational", org.jetbrains.exposed.sql.BooleanColumnType())
}

fun initDatabase() {
    val config = HikariConfig().apply {
        jdbcUrl = System.getenv("JDBC_DATABASE_URL") ?: "jdbc:postgresql://localhost:5432/ktor_api_db"
        username = System.getenv("DB_USER") ?: "postgres"
        password = System.getenv("DB_PASSWORD") ?: "postgres"
        driverClassName = "org.postgresql.Driver"
        maximumPoolSize = 3
        isAutoCommit = false
        transactionIsolation = "TRANSACTION_REPEATABLE_READ"
        validate()
    }
    val dataSource = HikariDataSource(config)
    Database.connect(dataSource)

    transaction {
        SchemaUtils.create(Locations, ParkingSpaces, EvChargingStations)
    }
}

suspend fun <T> dbQuery(block: suspend () -> T): T =
    newSuspendedTransaction(Dispatchers.IO) { block() }
