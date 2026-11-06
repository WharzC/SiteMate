package com.wharzc.prime.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.security.MessageDigest

class AuthViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    // Admin configuration
    private val ADMIN_USERNAME = "prime_admin"

    // PASTE YOUR COPIED HASH STRING HERE:
    private val ADMIN_PASS_HASH = "04712f79a0258803aa9e91b6032f94be179fbbc8370f518cb35856de0404ed6e"

    fun loginUser(
        emailOrUsername: String,
        pass: String,
        onNavigateAdmin: () -> Unit,
        onNavigateFarmer: () -> Unit,
        onError: (String) -> Unit
    ) {
        // 1. Admin Intercept Check
        if (emailOrUsername == ADMIN_USERNAME) {

            // UPDATE THIS LINE TO USE THE ENCRYPTED CHECK:
            if (hashString(pass) == ADMIN_PASS_HASH) {
                onNavigateAdmin()
            } else {
                onError("Invalid admin credentials.")
            }
            return // Prevents Firebase from trying to authenticate "prime_admin"
        }

        // 2. Farmer Firebase Auth Check
        auth.signInWithEmailAndPassword(emailOrUsername, pass)
            .addOnSuccessListener {
                onNavigateFarmer()
            }
            .addOnFailureListener { exception ->
                onError(exception.localizedMessage ?: "Authentication failed")
            }
    }

    fun signUpFarmer(
        fullName: String,
        email: String,
        pass: String,
        onNavigateFarmer: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (fullName.isBlank() || email.isBlank() || pass.isBlank()) {
            onError("Please fill out all fields.")
            return
        }

        auth.createUserWithEmailAndPassword(email, pass)
            .addOnSuccessListener { result ->
                val userId = result.user?.uid ?: return@addOnSuccessListener

                val userData = hashMapOf(
                    "name" to fullName,
                    "email" to email,
                    "role" to "farmer"
                )

                db.collection("users").document(userId)
                    .set(userData)
                    .addOnSuccessListener {
                        onNavigateFarmer()
                    }
                    .addOnFailureListener { e ->
                        onError("Failed to setup profile: ${e.localizedMessage}")
                    }
            }
            .addOnFailureListener { exception ->
                onError(exception.localizedMessage ?: "Registration failed")
            }
    }

    fun hashString(input: String): String {
        return MessageDigest.getInstance("SHA-256")
            .digest(input.toByteArray())
            .joinToString("") { "%02x".format(it) }
    }
}