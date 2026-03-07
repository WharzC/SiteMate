package com.wharzc.sitemate.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

// Your existing TeamMember data class, slightly updated to include the user's email
data class TeamMember(
    val id: String = "",
    val name: String = "",
    val email: String = "", // Added to link the accounts!
    val role: String = "",
    val status: String = "Pending",
    val canViewBudget: Boolean = false,
    val type: String = "Foreman"
)

// Data class for the pop-up modal's list
data class SelectableUser(val uid: String, val name: String, val email: String)

class ProjectAssignViewModel : ViewModel() {
    var teamMembers = mutableStateListOf<TeamMember>()
        private set
    var isLoading by mutableStateOf(true)
        private set

    // --- NEW: STATE FOR THE SELECTION MODALS ---
    var availableClients by mutableStateOf<List<SelectableUser>>(emptyList())
        private set

    // 1. FETCH THE TEAM (Your existing code)
    fun loadTeam(projectId: String) {
        isLoading = true
        val db = FirebaseFirestore.getInstance()

        db.collection("projects").document(projectId).collection("team")
            .get()
            .addOnSuccessListener { snap ->
                teamMembers.clear()
                for (doc in snap) {
                    teamMembers.add(
                        TeamMember(
                            id = doc.id,
                            name = doc.getString("name") ?: "",
                            email = doc.getString("email") ?: "",
                            role = doc.getString("role") ?: "",
                            status = doc.getString("status") ?: "Pending",
                            canViewBudget = doc.getBoolean("canViewBudget") ?: false,
                            type = doc.getString("type") ?: "Foreman"
                        )
                    )
                }
                isLoading = false
            }
            .addOnFailureListener {
                isLoading = false
            }
    }

    // 2. NEW: FETCH AVAILABLE USERS FOR THE MODAL
    fun fetchAvailableUsers() {
        val db = FirebaseFirestore.getInstance()
        availableClients = emptyList()


        // Fetch Clients
        db.collection("users").whereEqualTo("role", "Client").get()
            .addOnSuccessListener { docs ->
                availableClients = docs.mapNotNull { doc ->
                    val email = doc.getString("email") ?: return@mapNotNull null
                    val firstName = doc.getString("firstName") ?: ""
                    val lastName = doc.getString("lastName") ?: ""
                    SelectableUser(doc.id, "$firstName $lastName".trim(), email)
                }
            }
            .addOnFailureListener { e ->
                println("FIREBASE ERROR (Client): ${e.message}") // THIS TELLS US WHAT WENT WRONG!
            }
    }

    // 3. BATCH ASSIGN MULTIPLE USERS FROM THE MODAL
    fun assignSelectedUsers(projectId: String, type: String, selectedUsers: List<SelectableUser>, onComplete: () -> Unit) {
        if (selectedUsers.isEmpty()) {
            onComplete()
            return
        }

        val db = FirebaseFirestore.getInstance()
        val batch = db.batch()
        val projectRef = db.collection("projects").document(projectId)

        // FIX 1: Push emails to the "pending" array instead of "assigned"
        val arrayField = "pendingClients"
        val emailsToAdd = mutableListOf<String>()

        for (user in selectedUsers) {
            val newMemberRef = projectRef.collection("team").document()
            val newMember = hashMapOf(
                "name" to user.name,
                "email" to user.email,
                "role" to type,
                "status" to "Pending", // FIX 2: Ensures the badge says Pending!
                "type" to type,
                "canViewBudget" to false
            )
            batch.set(newMemberRef, newMember)
            emailsToAdd.add(user.email)
        }

        batch.update(projectRef, arrayField, FieldValue.arrayUnion(*emailsToAdd.toTypedArray()))

        batch.commit()
            .addOnSuccessListener {
                loadTeam(projectId)
                onComplete()
            }
            .addOnFailureListener { e ->
                println("FIREBASE ERROR: Batch assign failed: ${e.message}")
                onComplete()
            }
    }

    // 4. REMOVE A MEMBER (Bulletproof Version)
    fun removeMember(projectId: String, memberId: String, memberEmail: String, type: String) {
        val db = FirebaseFirestore.getInstance()
        val projectRef = db.collection("projects").document(projectId)

        // We target both arrays to guarantee they lose access regardless of their status
        val assignedArray = "assignedClients"
        val pendingArray = "pendingClients"

        // 1. Remove from the Engineer's UI team sub-collection
        projectRef.collection("team").document(memberId).delete().addOnSuccessListener {
            teamMembers.removeAll { it.id == memberId }
        }

        // 2. Rip their email out of BOTH top-level arrays in Firebase
        if (memberEmail.isNotEmpty()) {
            projectRef.update(assignedArray, FieldValue.arrayRemove(memberEmail))
            projectRef.update(pendingArray, FieldValue.arrayRemove(memberEmail))
        }
    }

    // 5. TOGGLE BUDGET PERMISSION (Your existing code)
    fun toggleBudgetPermission(projectId: String, memberId: String, canView: Boolean) {
        val db = FirebaseFirestore.getInstance()

        db.collection("projects").document(projectId).collection("team").document(memberId)
            .update("canViewBudget", canView)
            .addOnSuccessListener {
                val index = teamMembers.indexOfFirst { it.id == memberId }
                if (index != -1) {
                    teamMembers[index] = teamMembers[index].copy(canViewBudget = canView)
                }
            }
    }
}