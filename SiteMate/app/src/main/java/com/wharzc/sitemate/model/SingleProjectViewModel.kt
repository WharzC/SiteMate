package com.wharzc.sitemate.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

//Weather
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject

class SingleProjectViewModel : ViewModel() {
    // Holds the specific project once downloaded
    var project by mutableStateOf<Project?>(null)
        private set

    // --- LIVE VITALS FOR HOME TAB ---
    var overallProgress by mutableStateOf(0f)
        private set
    var isLoading by mutableStateOf(true)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    // --- NEW VITAL VARIABLES GO HERE ---
    var totalWorkersToday by mutableIntStateOf(0)
        private set
    var pendingInvites by mutableIntStateOf(0)
        private set
    var lastReportText by mutableStateOf("None")
        private set

    var projectOverallProgress by mutableFloatStateOf(0f)
        private set

    // ==========================================
    // --- ASSIGN TAB SEARCH & FILTER LOGIC ---
    // ==========================================
    var teamSearchQuery by mutableStateOf("")

    var teamList = mutableStateListOf<TeamMember>()
        private set

    // This dynamically updates the UI the exact second a user types in the search bar
    val filteredTeam: List<TeamMember>
        get() = if (teamSearchQuery.isEmpty()) {
            teamList
        } else {
            teamList.filter {
                it.name.contains(teamSearchQuery, ignoreCase = true) ||
                        it.email.contains(teamSearchQuery, ignoreCase = true)
            }
        }

    // WEATHER AND TEMPERATURE FORECAST
    // --- WEATHER STATES ---
    var weatherTemp by mutableStateOf("--°C")
        private set
    var weatherCode by mutableIntStateOf(0) // Tracks the weather condition (sun, rain, cloud)
        private set

    // Call this inside your LaunchedEffect in the Assign Screen!
    fun fetchTeamMembers(projectId: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("projects").document(projectId).collection("team")
            .get()
            .addOnSuccessListener { snap ->
                teamList.clear()
                for (doc in snap.documents) {
                    teamList.add(
                        TeamMember(
                            id = doc.id,
                            name = doc.getString("name") ?: "Unknown User",
                            email = doc.getString("email") ?: "No email provided",
                            role = doc.getString("role") ?: "Role unspecified",
                            status = doc.getString("status") ?: "Pending"
                        )
                    )
                }
            }
    }

    fun fetchLiveWeather(locationString: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // 1. CLEAN THE LOCATION STRING
                val parts = locationString.split(",").map { it.trim() }
                val cleanParts = parts.filter { !it.contains("Region", ignoreCase = true) }

                // THE FIX: Combine City and Province for hyper-accurate targeting
                // e.g., "Roxas, Oriental Mindoro"
                val searchCity = if (cleanParts.size >= 2) {
                    "${cleanParts[cleanParts.size - 2]}, ${cleanParts.last()}"
                } else {
                    cleanParts.joinToString(", ")
                }

                // 2. Fetch Coordinates (Geocoding)
                val query = java.net.URLEncoder.encode(searchCity, "UTF-8")
                val geoUrl = "https://geocoding-api.open-meteo.com/v1/search?name=$query&count=1&format=json"

                val geoConnection = java.net.URL(geoUrl).openConnection() as java.net.HttpURLConnection
                geoConnection.requestMethod = "GET"
                geoConnection.setRequestProperty("User-Agent", "SiteMateApp/1.0")

                val geoJson = geoConnection.inputStream.bufferedReader().readText()
                val geoObj = JSONObject(geoJson)

                if (geoObj.has("results")) {
                    val result = geoObj.getJSONArray("results").getJSONObject(0)
                    val lat = result.getDouble("latitude")
                    val lon = result.getDouble("longitude")

                    // 3. Fetch Live Weather
                    val weatherUrl = "https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lon&current_weather=true"
                    val weatherConnection = java.net.URL(weatherUrl).openConnection() as java.net.HttpURLConnection
                    weatherConnection.requestMethod = "GET"
                    weatherConnection.setRequestProperty("User-Agent", "SiteMateApp/1.0")

                    val weatherJson = weatherConnection.inputStream.bufferedReader().readText()
                    val weatherObj = JSONObject(weatherJson).getJSONObject("current_weather")

                    // THE FIX: Grab the raw double and format it to 1 decimal place!
                    val temp = weatherObj.getDouble("temperature")
                    val code = weatherObj.getInt("weathercode")

                    // Update UI States
                    weatherTemp = String.format(java.util.Locale.US, "%.1f°C", temp)
                    weatherCode = code
                } else {
                    fetchFallbackWeather()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                fetchFallbackWeather()
            }
        }
    }

    // --- FAILSAFE ---
    private fun fetchFallbackWeather() {
        try {
            val weatherUrl = "https://api.open-meteo.com/v1/forecast?latitude=14.5995&longitude=120.9842&current_weather=true"
            val weatherConnection = java.net.URL(weatherUrl).openConnection() as java.net.HttpURLConnection
            weatherConnection.setRequestProperty("User-Agent", "SiteMateApp/1.0")

            val weatherJson = weatherConnection.inputStream.bufferedReader().readText()
            val weatherObj = JSONObject(weatherJson).getJSONObject("current_weather")

            val temp = weatherObj.getDouble("temperature")
            weatherTemp = String.format(java.util.Locale.US, "%.1f°C", temp)
            weatherCode = weatherObj.getInt("weathercode")
        } catch (e: Exception) {
            // THE FIX: Exact format requested for offline state
            weatherTemp = "XX.X°C"
            weatherCode = -1 // Custom code to trigger an offline icon
        }
    }
    // ==========================================


    fun fetchProjectDetails(projectId: String) {
        isLoading = true
        errorMessage = null

        val db = FirebaseFirestore.getInstance()
        db.collection("projects").document(projectId).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val name = doc.getString("name") ?: doc.getString("projectName") ?: doc.getString("title") ?: "Unnamed Project"

                    val location = doc.getString("location")
                        ?: listOfNotNull(
                            doc.getString("street"),
                            doc.getString("barangay"),
                            doc.getString("city"),
                            doc.getString("province")
                        ).joinToString(", ").takeIf { it.isNotBlank() }
                        ?: "Unknown Location"

                    // --- NEW: Grab the classification details ---
                    val type = doc.getString("type") ?: ""
                    val category = doc.getString("category") ?: ""
                    val subCategory = doc.getString("subCategory") ?: ""

                    // (Note: Make sure your Project data class is updated to accept these 3 new strings!)
                    project = Project(id = doc.id, name = name, location = location, type = type, category = category, subCategory = subCategory)

                    // --- CRITICAL FIX: Trigger the vitals fetch so the dashboard numbers load! ---
                    fetchLiveVitals(projectId)

                    // --- NEW: Trigger the weather fetch based on the project location! ---
                    fetchLiveWeather(location)

                } else {
                    errorMessage = "Project no longer exists."
                    isLoading = false
                }
            }
            .addOnFailureListener { e ->
                errorMessage = e.message ?: "Failed to fetch project details."
                isLoading = false
            }
    }

    private fun fetchLiveVitals(projectId: String) {
        val db = FirebaseFirestore.getInstance()

        // A. Get Average Progress from Milestones
        db.collection("projects").document(projectId)
            .collection("milestones")
            .get()
            .addOnSuccessListener { milestoneSnap ->
                if (!milestoneSnap.isEmpty) {
                    val total = milestoneSnap.sumOf { it.getDouble("progress") ?: 0.0 }
                    overallProgress = (total.toFloat() / milestoneSnap.size())
                } else {
                    overallProgress = 0f
                }

                // B. Get Today's Headcount from Daily Logs
                val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                db.collection("projects").document(projectId)
                    .collection("daily_logs").document(today)
                    .get()
                    .addOnSuccessListener { logDoc ->
                        if (logDoc.exists()) {
                            val manpower = logDoc.get("manpower") as? Map<String, Long> ?: emptyMap()
                            totalWorkersToday = manpower.values.sum().toInt()
                        } else {
                            totalWorkersToday = 0
                        }

                        // Everything is finally loaded
                        isLoading = false
                    }
                    .addOnFailureListener {
                        // Even if daily log fetch fails, we stop loading
                        isLoading = false
                    }
            }
            .addOnFailureListener {
                isLoading = false
            }
    }

    fun fetchProjectVitals(projectId: String) {
        val db = FirebaseFirestore.getInstance()

        // 0. SELF-HEALING OVERALL PROGRESS CALCULATOR
        db.collection("projects").document(projectId).collection("milestones").get()
            .addOnSuccessListener { msDocs ->
                var totalProgress = 0f

                for (m in msDocs) {
                    totalProgress += (m.getDouble("progress") ?: 0.0).toFloat()
                }

                val expectedPhases = if (msDocs.size() > 9) msDocs.size() else 9

                var calculatedProgress = if (expectedPhases > 0) totalProgress / expectedPhases else 0f
                if (calculatedProgress > 1f) calculatedProgress = 1f

                projectOverallProgress = calculatedProgress

                db.collection("projects").document(projectId).update("overallProgress", calculatedProgress)
            }

        val format = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        format.timeZone = java.util.TimeZone.getTimeZone("Asia/Manila")
        val todayStr = format.format(java.util.Date())

        db.collection("projects").document(projectId).collection("daily_logs").document(todayStr).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val manpower = doc.get("manpower") as? Map<String, Long> ?: emptyMap()
                    totalWorkersToday = manpower.values.sum().toInt()
                } else {
                    totalWorkersToday = 0
                }
            }

        db.collection("projects").document(projectId).collection("team")
            .whereEqualTo("status", "Pending")
            .get()
            .addOnSuccessListener { snap ->
                pendingInvites = snap.size()
            }

        db.collection("projects").document(projectId).collection("reports")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(1).get()
            .addOnSuccessListener { snap ->
                if (!snap.isEmpty) {
                    val lastTimestamp = snap.documents[0].getLong("timestamp") ?: 0L

                    if (lastTimestamp == 0L) {
                        lastReportText = "None"
                    } else {
                        val diff = System.currentTimeMillis() - lastTimestamp
                        val days = java.util.concurrent.TimeUnit.MILLISECONDS.toDays(diff)
                        val hours = java.util.concurrent.TimeUnit.MILLISECONDS.toHours(diff)
                        val minutes = java.util.concurrent.TimeUnit.MILLISECONDS.toMinutes(diff)

                        lastReportText = when {
                            days > 0 -> "${days}d"
                            hours > 0 -> "${hours}h"
                            minutes > 0 -> "${minutes}m"
                            else -> "Just now"
                        }
                    }
                } else {
                    lastReportText = "None"
                }
            }
    }

    private fun calculateTimeAgo(timestamp: Long): String {
        if (timestamp == 0L) return "None"
        val diff = System.currentTimeMillis() - timestamp
        val days = java.util.concurrent.TimeUnit.MILLISECONDS.toDays(diff)
        val hours = java.util.concurrent.TimeUnit.MILLISECONDS.toHours(diff)

        return when {
            days > 0 -> "${days}d"
            hours > 0 -> "${hours}h"
            else -> "Just now"
        }
    }
}

