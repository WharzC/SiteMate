package com.wharzc.sitemate.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

data class Milestone(
    val id: String,
    val title: String,
    val progress: Float = 0f,
    val status: String = "Pending",
    val works: List<String> = emptyList(),               // flat list for all activities
    val completedWorks: List<String> = emptyList(),
    val categorizedWorks: Map<String, List<String>>? = null // only for demolition
)

class ProjectProgressViewModel : ViewModel() {
    var overallProgress by mutableStateOf(0f)
        private set
    var targetDeadline by mutableStateOf("Not Set")
        private set
    var milestones by mutableStateOf<List<Milestone>>(emptyList())
        private set
    var isLoading by mutableStateOf(true)
        private set

    var hasStarted by mutableStateOf(true)
        private set
    var startDateText by mutableStateOf("-")
        private set
    var daysUntilStart by mutableStateOf("-")
        private set
    var daysElapsed by mutableStateOf("-")
        private set
    var daysRemaining by mutableStateOf("-")
        private set

    fun loadProgress(projectId: String, projectType: String) {
        isLoading = true

        // Determine RTDB path based on project type
        val rtdbPath = when {
            projectType.contains("Vertical", ignoreCase = true) -> "1/VERTICAL CONSTRUCTION"
            projectType.contains("Horizontal", ignoreCase = true) -> "2/HORIZONTAL CONSTRUCTION"
            projectType.contains("Demolition", ignoreCase = true) -> "3/DEMOLITION"
            else -> return
        }

        val rtdbRef = FirebaseDatabase.getInstance().getReference(rtdbPath)

        rtdbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var templateMilestones = mutableListOf<Milestone>()

                for (phaseNode in snapshot.children) {
                    val phaseName = phaseNode.key ?: continue

                    if (projectType.contains("Demolition", ignoreCase = true)) {
                        // --- DEMOLITION: nested categories ---
                        val worksObject = phaseNode.child("Works")
                        val categorized = mutableMapOf<String, List<String>>()
                        val flatWorks = mutableListOf<String>()

                        worksObject.children.forEach { categoryNode ->
                            val categoryName = categoryNode.key ?: return@forEach
                            val items = mutableListOf<String>()
                            categoryNode.children.forEach { itemNode ->
                                itemNode.getValue(String::class.java)?.let {
                                    items.add(it)
                                    flatWorks.add(it)
                                }
                            }
                            if (items.isNotEmpty()) {
                                categorized[categoryName] = items
                            }
                        }

                        templateMilestones.add(
                            Milestone(
                                id = phaseName,
                                title = phaseName,
                                works = flatWorks,
                                categorizedWorks = categorized
                            )
                        )
                    } else {
                        // --- VERTICAL / HORIZONTAL: flat works list ---
                        val worksList = mutableListOf<String>()
                        phaseNode.child("Works").children.forEach { workItem ->
                            workItem.getValue(String::class.java)?.let { worksList.add(it) }
                        }
                        templateMilestones.add(
                            Milestone(
                                id = phaseName,
                                title = phaseName,
                                works = worksList
                            )
                        )
                    }
                }

                // Apply chronological sorting for Vertical (if needed)
                if (projectType.contains("Vertical", ignoreCase = true) && templateMilestones.size >= 9) {
                    val order = listOf(7, 1, 8, 0, 3, 6, 5, 4, 2)
                    templateMilestones = try {
                        order.map { templateMilestones[it] }.toMutableList()
                    } catch (e: Exception) { templateMilestones }
                }

                // Firestore: fetch completed works and calculate progress
                val db = FirebaseFirestore.getInstance()
                db.collection("projects").document(projectId).collection("milestones").get()
                    .addOnSuccessListener { firestoreDocs ->
                        val finalMilestones = templateMilestones.map { template ->
                            val savedDoc = firestoreDocs.documents.find { it.id == template.id }
                            if (savedDoc != null) {
                                val completed = savedDoc.get("completedWorks") as? List<String> ?: emptyList()
                                val progress = if (template.works.isNotEmpty()) {
                                    completed.size.toFloat() / template.works.size.toFloat()
                                } else 0f
                                val status = when {
                                    progress == 0f -> "Pending"
                                    progress == 1f -> "Completed"
                                    else -> "In Progress"
                                }
                                template.copy(
                                    progress = progress,
                                    status = status,
                                    completedWorks = completed
                                )
                            } else {
                                template
                            }
                        }

                        milestones = finalMilestones
                        overallProgress = if (finalMilestones.isEmpty()) 0f
                        else finalMilestones.map { it.progress }.average().toFloat()
                        isLoading = false
                    }

                // Load dates (unchanged)
                db.collection("projects").document(projectId).get()
                    .addOnSuccessListener { projDoc ->
                        val startDateStr = projDoc.getString("startDate") ?: ""
                        targetDeadline = projDoc.getString("endDate") ?: "Not Set"
                        calculateDays(startDateStr, targetDeadline)
                    }
            }

            override fun onCancelled(error: DatabaseError) {
                isLoading = false
            }
        })
    }

    // --- UPDATED DATE CALCULATION ---
    private fun calculateDays(startStr: String, endStr: String) {
        try {
            // 1. Strip out all spaces so "03 / 27 / 2027" becomes safely readable "03/27/2027"
            val cleanStart = startStr.replace("\\s".toRegex(), "")
            val cleanEnd = endStr.replace("\\s".toRegex(), "")

            // 2. Failsafe: If no start date was ever set, default to the elapsed view with dashes
            if (cleanStart.isBlank() || cleanStart == "NotSet") {
                hasStarted = true
                daysElapsed = "-"
                daysRemaining = "-"
                return
            }

            val format = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
            val startDate = format.parse(cleanStart)
            // Safely try to parse the end date, ignore if it's missing
            val endDate = try { format.parse(cleanEnd) } catch (e: Exception) { null }

            // 3. Strip hours/minutes from today's date to prevent off-by-one errors!
            val today = format.parse(format.format(Date()))

            startDateText = startStr // Keep original text for the UI display

            if (startDate != null && today != null) {
                // Check if the project is in the future
                if (today.before(startDate)) {
                    hasStarted = false
                    val diffStart = startDate.time - today.time
                    daysUntilStart = TimeUnit.MILLISECONDS.toDays(diffStart).toString()
                } else {
                    hasStarted = true
                    val diffElapsed = today.time - startDate.time
                    daysElapsed = TimeUnit.MILLISECONDS.toDays(diffElapsed).toString()
                }
            }
            if (endDate != null && today != null) {
                val diffRemain = endDate.time - today.time
                daysRemaining = maxOf(0, TimeUnit.MILLISECONDS.toDays(diffRemain)).toString()
            }
        } catch (e: Exception) {
            // Fallback if the dates are completely corrupted
            println("SiteMate Date Error: ${e.message}")
            hasStarted = true
            daysElapsed = "-"
            daysRemaining = "-"
            startDateText = "-"
            daysUntilStart = "-"
        }
    }
}