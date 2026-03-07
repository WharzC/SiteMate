package com.wharzc.sitemate.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

// Data structure for the Foreman's view
data class AssignedProject(val id: String, val name: String, val location: String)

class ForemanProjectsViewModel : ViewModel() {
    var assignedProjects by mutableStateOf<List<AssignedProject>>(emptyList())
        private set
    var isLoading by mutableStateOf(true)
        private set

    fun fetchAssignedProjects() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            val userEmail = user.email ?: return
            val db = FirebaseFirestore.getInstance()

            // Look through all projects where this Foreman's email is in the assigned list
            db.collection("projects")
                .whereArrayContains("assignedForemen", userEmail)
                .get()
                .addOnSuccessListener { documents ->
                    val projectList = mutableListOf<AssignedProject>()
                    for (doc in documents) {
                        val name = doc.getString("name") ?: "Unnamed Project"
                        val location = doc.getString("location") ?: "Unknown Location"
                        projectList.add(AssignedProject(doc.id, name, location))
                    }
                    assignedProjects = projectList
                    isLoading = false
                }
                .addOnFailureListener { e ->
                    println("Error fetching assigned projects: ${e.message}")
                    isLoading = false
                }
        } else {
            isLoading = false
        }
    }
}