package com.wharzc.sitemate.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

data class PendingProject(val id: String, val name: String, val location: String, val engineerName: String, val memberId: String)

class AssignmentsViewModel : ViewModel() {
    var pendingRequests by mutableStateOf<List<PendingProject>>(emptyList())
        private set
    var isLoading by mutableStateOf(true)
        private set

    fun fetchPendingRequests(role: String) {
        isLoading = true
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null || user.email == null) {
            isLoading = false
            return
        }

        val db = FirebaseFirestore.getInstance()
        val pendingArray = "pendingClients"

        db.collection("projects").whereArrayContains(pendingArray, user.email!!).get()
            .addOnSuccessListener { documents ->
                val requests = mutableListOf<PendingProject>()

                // We need to fetch the specific project details AND find their specific team document ID
                for (doc in documents) {
                    val projectId = doc.id
                    // Casts a wide net for the title
                    val projectName = doc.getString("projectName") ?: doc.getString("name") ?: doc.getString("title") ?: "Unnamed Project"
                    val location = doc.getString("location") ?: "Unknown Location"
                    // Casts a wide net for the engineer
                    val engineerName = doc.getString("engineerName") ?: doc.getString("createdBy") ?: "SiteMate Engineer"

                    // Find their specific document in the team subcollection to update its status later
                    db.collection("projects").document(projectId).collection("team")
                        .whereEqualTo("email", user.email).get()
                        .addOnSuccessListener { teamDocs ->
                            if (!teamDocs.isEmpty) {
                                val memberId = teamDocs.documents[0].id
                                requests.add(PendingProject(projectId, projectName, location, engineerName, memberId))
                                // Trigger UI update
                                pendingRequests = requests.toList()
                            }
                        }
                }
                isLoading = false
            }
            .addOnFailureListener {
                isLoading = false
            }
    }

    fun handleRequest(project: PendingProject, role: String, isAccepted: Boolean, onComplete: () -> Unit) {
        val userEmail = FirebaseAuth.getInstance().currentUser?.email ?: return
        val db = FirebaseFirestore.getInstance()
        val batch = db.batch()
        val projectRef = db.collection("projects").document(project.id)
        val teamRef = projectRef.collection("team").document(project.memberId)

        val pendingArray = "pendingClients"
        val assignedArray =  "assignedClients"

        // 1. Always remove them from the pending array
        batch.update(projectRef, pendingArray, FieldValue.arrayRemove(userEmail))

        if (isAccepted) {
            // 2. Add them to the assigned array and update status to Active
            batch.update(projectRef, assignedArray, FieldValue.arrayUnion(userEmail))
            batch.update(teamRef, "status", "Active")
        } else {
            // 3. Delete their team document entirely if rejected
            batch.delete(teamRef)
        }

        batch.commit().addOnSuccessListener {
            // Remove from the UI list instantly
            pendingRequests = pendingRequests.filter { it.id != project.id }
            onComplete()
        }
    }
}