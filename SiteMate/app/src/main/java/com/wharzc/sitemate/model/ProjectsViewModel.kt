package com.wharzc.sitemate.model

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth // NEW: Required to get the Engineer's ID
import com.google.firebase.firestore.FirebaseFirestore

class ProjectsViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance() // Gets the current logged-in user

    var projects = mutableStateListOf<Project>()
        private set

    init {
        listenToProjects()
    }

    private fun listenToProjects() {
        val currentUser = auth.currentUser

        // Failsafe: Stop if nobody is logged in
        if (currentUser == null) {
            println("User not logged in, cannot listen to projects.")
            return
        }

        // THE FIX: Only listen to projects where engineerId matches this specific user!
        db.collection("projects")
            .whereEqualTo("engineerId", currentUser.uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    println("Listen failed: ${error.message}")
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    projects.clear()
                    for (document in snapshot.documents) {
                        projects.add(
                            Project(
                                id = document.id,
                                // Casting a wide net just in case to catch all naming variations
                                name = document.getString("name") ?: document.getString("projectName") ?: document.getString("title") ?: "Unnamed Project",
                                location = document.getString("location") ?: "Unknown Location"
                            )
                        )
                    }
                }
            }
    }

    fun deleteProject(projectId: String) {
        db.collection("projects").document(projectId)
            .delete()
            .addOnSuccessListener {
                println("Project successfully deleted!")
            }
            .addOnFailureListener { e ->
                println("Error deleting project: ${e.message}")
            }
    }

    // --- UPDATED: Bulletproofed to include engineer data and arrays ---
    fun addProject(name: String, location: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            onFailure("User not logged in.")
            return
        }

        if (name.isBlank() || location.isBlank()) {
            onFailure("Project name and location cannot be empty.")
            return
        }

        // Fetch user profile to get the engineerName
        db.collection("users").document(currentUser.uid).get()
            .addOnSuccessListener { userDoc ->
                val firstName = userDoc.getString("firstName") ?: ""
                val lastName = userDoc.getString("lastName") ?: ""
                val fullName = "$firstName $lastName".trim()

                // Package the data exactly how Firebase and our Request tabs expect it
                val projectData = hashMapOf(
                    "name" to name,
                    "location" to location,
                    "engineerId" to currentUser.uid,
                    "engineerName" to fullName.ifEmpty { "SiteMate Engineer" },
                    "assignedForemen" to emptyList<String>(),
                    "pendingForemen" to emptyList<String>(),
                    "assignedClients" to emptyList<String>(),
                    "pendingClients" to emptyList<String>(),
                    "createdAt" to System.currentTimeMillis()
                )

                db.collection("projects")
                    .add(projectData)
                    .addOnSuccessListener { documentReference ->
                        println("Project written with ID: ${documentReference.id}")
                        onSuccess()
                    }
                    .addOnFailureListener { e ->
                        println("Error adding project: ${e.message}")
                        onFailure(e.message ?: "An unknown error occurred.")
                    }
            }
            .addOnFailureListener { e ->
                onFailure(e.message ?: "Failed to fetch user profile.")
            }
    }
}