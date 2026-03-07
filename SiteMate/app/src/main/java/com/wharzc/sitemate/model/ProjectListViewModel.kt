package com.wharzc.sitemate.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore

class ProjectListViewModel : ViewModel() {
    // This holds your live list of projects
    var projects = mutableStateListOf<Project>()
        private set

    // A loading state so we can show a spinner while data downloads
    var isLoading by mutableStateOf(true)
        private set

    init {
        // Fetch projects as soon as this ViewModel is created
        fetchProjects()
    }

    fun fetchProjects() {
        isLoading = true
        val db = FirebaseFirestore.getInstance()

        // Go to the "projects" collection and get everything
        db.collection("projects")
            .get()
            .addOnSuccessListener { result ->
                projects.clear()
                for (document in result) {
                    val id = document.id
                    val name = document.getString("name") ?: "Unnamed Project"

                    // Construct the location from the pieces we saved earlier
                    val city = document.getString("city") ?: ""
                    val province = document.getString("province") ?: ""
                    val location = if (city.isNotBlank() && province.isNotBlank()) "$city, $province" else "Unknown Location"

                    // Add it to our live list (assuming your Project model has id, name, and location)
                    projects.add(Project(id = id, name = name, location = location))
                }
                isLoading = false
            }
            .addOnFailureListener { exception ->
                println("Error fetching projects: ${exception.message}")
                isLoading = false
            }
    }

    // Optional: A function to delete from Firebase later
    fun deleteProject(projectId: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("projects").document(projectId)
            .delete()
            .addOnSuccessListener {
                // Remove it from the UI list instantly
                projects.removeAll { it.id == projectId }
            }
    }
}