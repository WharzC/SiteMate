package com.wharzc.sitemate.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileViewModel : ViewModel() {
    var userName by mutableStateOf("Loading...")
        private set
    var userEmail by mutableStateOf("")
        private set
    var userRole by mutableStateOf("") // Will display Engineer, Foreman, or Client
        private set
    var isLoading by mutableStateOf(true)
        private set

    fun fetchUserProfile() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            userEmail = user.email ?: ""
            val db = FirebaseFirestore.getInstance()

            // Access the 'users' collection using the Auth UID
            db.collection("users").document(user.uid).get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        // 1. Fetch and combine firstName and lastName
                        val firstName = doc.getString("firstName") ?: ""
                        val lastName = doc.getString("lastName") ?: ""
                        val fullName = "$firstName $lastName".trim()

                        userName = if (fullName.isNotEmpty()) fullName else "No Name Found"

                        // 2. Fetch the specific system role
                        // Checking "role" first, with a fallback to "userType" just in case
                        userRole = doc.getString("role") ?: doc.getString("userType") ?: "Unassigned"
                    } else {
                        userName = "User Not Found"
                    }
                    isLoading = false
                }
                .addOnFailureListener {
                    userName = "Error Loading Profile"
                    isLoading = false
                }
        } else {
            isLoading = false
        }
    }

    fun logout(onLogout: () -> Unit) {
        FirebaseAuth.getInstance().signOut()
        onLogout()
    }
}