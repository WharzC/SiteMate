package com.wharzc.sitemate.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ClientProjectsViewModel : ViewModel() {
    var assignedProjects by mutableStateOf<List<AssignedProject>>(emptyList())
        private set
    var isLoading by mutableStateOf(true)
        private set

    fun fetchClientProjects() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            val userEmail = user.email ?: return
            val db = FirebaseFirestore.getInstance()

            // Look for projects where the Client's email is assigned
            db.collection("projects")
                .whereArrayContains("assignedClients", userEmail)
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
                .addOnFailureListener {
                    isLoading = false
                }
        } else {
            isLoading = false
        }
    }
}