package com.wharzc.sitemate.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableFloatStateOf

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
    // --- 1. NEW DYNAMIC VITAL STATES ---


    // --- 2. ADD THIS NEW FUNCTION ---
    fun fetchProjectVitals(projectId: String) {
        val db = FirebaseFirestore.getInstance()

        // 0. SELF-HEALING OVERALL PROGRESS CALCULATOR
        // Calculates progress directly from the milestones to guarantee 100% accuracy,
        // even if the 'overallProgress' field is completely missing from the database!
        db.collection("projects").document(projectId).collection("milestones").get()
            .addOnSuccessListener { msDocs ->
                var totalProgress = 0f

                // Sum up the progress of all saved milestones
                for (m in msDocs) {
                    totalProgress += (m.getDouble("progress") ?: 0.0).toFloat()
                }

                // Determine total phases (Standard Vertical construction has 9 phases)
                val expectedPhases = if (msDocs.size() > 9) msDocs.size() else 9

                // Calculate the final percentage
                var calculatedProgress = if (expectedPhases > 0) totalProgress / expectedPhases else 0f
                if (calculatedProgress > 1f) calculatedProgress = 1f // Cap at 100% just in case

                // 1. Update the UI card instantly
                projectOverallProgress = calculatedProgress

                // 2. SELF-HEAL: Force the missing field into the Firestore project document permanently!
                db.collection("projects").document(projectId).update("overallProgress", calculatedProgress)
            }

        // Setup exact PH Time string to match your Daily Logs
        val format = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        format.timeZone = java.util.TimeZone.getTimeZone("Asia/Manila")
        val todayStr = format.format(java.util.Date())

        // 1. Fetch "On-Site Today" (Sums up the manpower from today's daily log)
        db.collection("projects").document(projectId).collection("daily_logs").document(todayStr).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val manpower = doc.get("manpower") as? Map<String, Long> ?: emptyMap()
                    totalWorkersToday = manpower.values.sum().toInt()
                } else {
                    totalWorkersToday = 0
                }
            }

        // 2. Fetch "Pending Invites" (Counts team members with "Pending" status)
        db.collection("projects").document(projectId).collection("team")
            .whereEqualTo("status", "Pending")
            .get()
            .addOnSuccessListener { snap ->
                pendingInvites = snap.size()
            }

        // 3. Fetch "Last Report" (Finds the most recent report and calculates how long ago it was)
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
                        // NEW: Calculate the elapsed minutes
                        val minutes = java.util.concurrent.TimeUnit.MILLISECONDS.toMinutes(diff)

                        lastReportText = when {
                            days > 0 -> "${days}d"       // More than 24 hours
                            hours > 0 -> "${hours}h"     // Between 1 and 23 hours
                            minutes > 0 -> "${minutes}m" // Between 1 and 59 minutes
                            else -> "Just now"           // Less than 1 minute
                        }
                    }
                } else {
                    lastReportText = "None"
                }
            }
    }

    // Helper function for the Last Report text
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