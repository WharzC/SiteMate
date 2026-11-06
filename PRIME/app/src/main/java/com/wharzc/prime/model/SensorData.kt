package com.wharzc.prime.model

// Use Double for decimals to prevent Firebase deserialization crashes
data class SensorData(
    val timestamp: String = "",
    val npk: NpkData = NpkData(),
    val waterQuality: WaterQualityData = WaterQualityData(),
    val waterLevel: WaterLevelData = WaterLevelData(),
    val location: LocationData = LocationData()
)

data class NpkData(
    val nitrogen: Int = 0,
    val phosphorus: Int = 0,
    val potassium: Int = 0
)

data class WaterQualityData(
    val temperature: Double = 0.0,
    val pH: Double = 0.0,
    val turbidity: Double = 0.0
)

data class WaterLevelData(
    val distance: Double = 0.0,
    val status: String = "UNKNOWN"
)

data class LocationData(
    val barangay: String = "Loading...",
    val city: String = "Loading..."
)

// Data class for Firestore users
data class FarmerUser(
    val id: String = "",
    val name: String = "",
    val email: String = ""
)