package com.wharzc.motoguard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.maplibre.android.geometry.LatLng
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.*

data class MotorcycleTripLog(
    val id: Long,
    val date: String,
    val time: String,
    val previousLocation: String,
    val currentExactLocation: String,
    val coordinates: String,
    val distance: String,
    val timeInterval: String
)

class TripViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val userId: String? get() = auth.currentUser?.uid

    private val _tripList = MutableStateFlow<List<MotorcycleTripLog>>(emptyList())
    val tripList: StateFlow<List<MotorcycleTripLog>> = _tripList.asStateFlow()

    private var lastSavedLocation: LatLng? = null
    private var lastSavedTime: Long = 0L
    private var lastSavedSpeed: Double = 0.0

    init {
        loadTripsFromFirestore()
    }

    private fun loadTripsFromFirestore() {
        if (userId == null) return
        viewModelScope.launch {
            try {
                val snapshot = firestore.collection("trips")
                    .whereEqualTo("userId", userId)
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .get()
                    .await()
                val trips = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(TripFirestoreData::class.java)?.toMotorcycleTripLog()
                }
                _tripList.value = trips

                // Restore last tracking state from the most recent trip (if any)
                trips.firstOrNull()?.let { latest ->
                    lastSavedLocation = parseLatLngFromString(latest.currentExactLocation)
                    lastSavedTime = latest.id   // id is the timestamp in milliseconds
                    lastSavedSpeed = 0.0        // speed not stored, assume stationary
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Helper: convert "Lat: 12.3456, Lon: 67.8901" -> LatLng
    private fun parseLatLngFromString(locationStr: String): LatLng? {
        val pattern = Regex("Lat: ([0-9.-]+), Lon: ([0-9.-]+)")
        val match = pattern.find(locationStr)
        return if (match != null) {
            val lat = match.groupValues[1].toDoubleOrNull()
            val lon = match.groupValues[2].toDoubleOrNull()
            if (lat != null && lon != null) LatLng(lat, lon) else null
        } else null
    }

    fun evaluateTelemetryRules(currentLocation: LatLng, currentSpeedKmh: Double) {
        val currentTimeMs = System.currentTimeMillis()

        if (lastSavedLocation == null) {
            // No previous trip: create an initial entry
            saveTripLog(
                currentLocation = currentLocation,
                previousLoc = currentLocation,
                distanceKm = 0.0,
                timeDeltaMs = 0L,
                isManualSave = false
            )
            updateTrackingState(currentLocation, currentTimeMs, currentSpeedKmh)
            return
        }

        val distanceDeltaKm = calculateHaversineDistance(lastSavedLocation!!, currentLocation)
        val timeDeltaMs = currentTimeMs - lastSavedTime

        val fiveMinutesMs = 5 * 60 * 1000L
        val oneHourMs = 60 * 60 * 1000L

        val isAcceleratingSignificantly = currentSpeedKmh > (lastSavedSpeed + 15.0)
        val hasMovedInPast5Mins = (distanceDeltaKm > 0.02) && (timeDeltaMs >= fiveMinutesMs)

        if ((isAcceleratingSignificantly || hasMovedInPast5Mins) && timeDeltaMs >= fiveMinutesMs) {
            saveTripLog(currentLocation, lastSavedLocation!!, distanceDeltaKm, timeDeltaMs, false)
            updateTrackingState(currentLocation, currentTimeMs, currentSpeedKmh)
            return
        }

        if (timeDeltaMs >= oneHourMs) {
            saveTripLog(currentLocation, lastSavedLocation!!, distanceDeltaKm, timeDeltaMs, false)
            updateTrackingState(currentLocation, currentTimeMs, currentSpeedKmh)
        }
    }

    private fun updateTrackingState(location: LatLng, timeMs: Long, speedKmh: Double) {
        lastSavedLocation = location
        lastSavedTime = timeMs
        lastSavedSpeed = speedKmh
    }

    private fun saveTripLog(
        currentLocation: LatLng,
        previousLoc: LatLng,
        distanceKm: Double,
        timeDeltaMs: Long,
        isManualSave: Boolean
    ) {
        val currentTimeMs = System.currentTimeMillis()
        val dateFormatter = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
        val timeFormatter = SimpleDateFormat("hh:mm a", Locale.getDefault())

        // Determine time interval string according to requirements
        val timeInterval = when {
            isManualSave -> "Manual save"
            timeDeltaMs == 0L -> "Initial Start"
            else -> formatTimeInterval(timeDeltaMs)
        }

        val newTrip = MotorcycleTripLog(
            id = currentTimeMs,
            date = dateFormatter.format(Date(currentTimeMs)),
            time = timeFormatter.format(Date(currentTimeMs)),
            previousLocation = "Lat: ${String.format("%.4f", previousLoc.latitude)}, Lon: ${String.format("%.4f", previousLoc.longitude)}",
            currentExactLocation = "Lat: ${String.format("%.4f", currentLocation.latitude)}, Lon: ${String.format("%.4f", currentLocation.longitude)}",
            coordinates = "${String.format("%.5f", currentLocation.latitude)}, ${String.format("%.5f", currentLocation.longitude)}",
            distance = "${String.format("%.2f", distanceKm)} km",
            timeInterval = timeInterval
        )

        // Update local list immediately
        _tripList.value = listOf(newTrip) + _tripList.value

        // Save to Firestore if user is logged in
        if (userId != null) {
            val data = TripFirestoreData(
                userId = userId!!,
                timestamp = currentTimeMs,
                id = newTrip.id,
                date = newTrip.date,
                time = newTrip.time,
                previousLocation = newTrip.previousLocation,
                currentExactLocation = newTrip.currentExactLocation,
                coordinates = newTrip.coordinates,
                distance = newTrip.distance,
                timeInterval = newTrip.timeInterval
            )
            viewModelScope.launch {
                try {
                    firestore.collection("trips").document(currentTimeMs.toString()).set(data).await()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun manualSaveTrip(): Boolean {
        val now = System.currentTimeMillis()
        // Cooldown: 1 minute
        if (now - lastManualSaveTime < 60_000L) {
            return false
        }
        val currentLoc = lastSavedLocation
        if (currentLoc == null) {
            return false
        }
        saveTripLog(
            currentLocation = currentLoc,
            previousLoc = currentLoc,
            distanceKm = 0.0,
            timeDeltaMs = 0L,
            isManualSave = true
        )
        lastManualSaveTime = now
        return true
    }

    fun clearAllHistory() {
        if (userId == null) return
        viewModelScope.launch {
            try {
                val snapshot = firestore.collection("trips")
                    .whereEqualTo("userId", userId)
                    .get()
                    .await()
                val batch = firestore.batch()
                snapshot.documents.forEach { doc ->
                    batch.delete(doc.reference)
                }
                batch.commit().await()
                _tripList.value = emptyList()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        lastSavedLocation = null
        lastSavedTime = 0L
        lastSavedSpeed = 0.0
    }

    private var lastManualSaveTime = 0L

    private fun formatTimeInterval(ms: Long): String {
        val totalMinutes = ms / (1000 * 60)
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        return when {
            hours > 0 -> "$hours hr $minutes mins"
            else -> "$minutes mins"
        }
    }

    private fun calculateHaversineDistance(loc1: LatLng, loc2: LatLng): Double {
        val earthRadiusKm = 6371.0
        val dLat = (loc2.latitude - loc1.latitude) * Math.PI / 180.0
        val dLon = (loc2.longitude - loc1.longitude) * Math.PI / 180.0
        val lat1Rad = loc1.latitude * Math.PI / 180.0
        val lat2Rad = loc2.latitude * Math.PI / 180.0
        val sinDLat = sin(dLat / 2.0)
        val sinDLon = sin(dLon / 2.0)
        val a = sinDLat * sinDLat + cos(lat1Rad) * cos(lat2Rad) * sinDLon * sinDLon
        val c = 2.0 * atan2(sqrt(a), sqrt(1.0 - a))
        return earthRadiusKm * c
    }
}

// Internal data class for Firestore serialization
private data class TripFirestoreData(
    val userId: String,
    val timestamp: Long,
    val id: Long,
    val date: String,
    val time: String,
    val previousLocation: String,
    val currentExactLocation: String,
    val coordinates: String,
    val distance: String,
    val timeInterval: String
) {
    fun toMotorcycleTripLog(): MotorcycleTripLog = MotorcycleTripLog(
        id = id,
        date = date,
        time = time,
        previousLocation = previousLocation,
        currentExactLocation = currentExactLocation,
        coordinates = coordinates,
        distance = distance,
        timeInterval = timeInterval
    )
}