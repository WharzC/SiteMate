package com.wharzc.sitemate.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions


class AuthViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    var currentUser by mutableStateOf<FirebaseUser?>(auth.currentUser)
        private set

    // NEW: Variable to hold the user's role (Engineer, Foreman, Client)
    var userRole by mutableStateOf<String?>(null)
        private set

    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    init {
        // Listen for login/logout events
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            currentUser = user

            if (user != null) {
                // NEW: If logged in, fetch their role from Firestore
                FirebaseFirestore.getInstance().collection("users").document(user.uid).get()
                    .addOnSuccessListener { document ->
                        if (document.exists()) {
                            userRole = document.getString("role")
                        }
                    }
            } else {
                userRole = null
            }
        }
    }



    fun saveUserRole(role: String, onResult: (Boolean, String) -> Unit) {
        val user = FirebaseAuth.getInstance().currentUser

        // 1. Check if the session accidentally dropped
        if (user == null) {
            onResult(false, "Error: User session lost. Please log in again.")
            return
        }

        val db = FirebaseFirestore.getInstance()
        val data = hashMapOf("role" to role)

        // 2. Attempt the save
        db.collection("users").document(user.uid)
            .set(data, com.google.firebase.firestore.SetOptions.merge())
            .addOnSuccessListener {
                userRole = role
                onResult(true, "Success")
            }
            .addOnFailureListener { e ->
                // 3. Send the exact Firebase error back to the UI!
                onResult(false, "Database Error: ${e.message}")
            }
    }


    // --- 1. SIGN UP (Email/Password) ---
    fun signUpWithEmail(email: String, pass: String, onSuccess: () -> Unit) {
        if (email.isBlank() || pass.isBlank()) {
            errorMessage = "Email and password cannot be empty."
            return
        }

        isLoading = true
        errorMessage = null

        auth.createUserWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                isLoading = false
                if (task.isSuccessful) {
                    onSuccess()
                } else {
                    errorMessage = task.exception?.message ?: "Sign up failed."
                }
            }
    }

    // --- 2. LOGIN (Email/Password) ---
    fun loginWithEmail(email: String, pass: String, onSuccess: () -> Unit) {
        if (email.isBlank() || pass.isBlank()) {
            errorMessage = "Email and password cannot be empty."
            return
        }

        isLoading = true
        errorMessage = null

        auth.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                isLoading = false
                if (task.isSuccessful) {
                    onSuccess()
                } else {
                    errorMessage =
                        task.exception?.message ?: "Login failed. Check your credentials."
                }
            }
    }

    fun signUpWithEmail(
        firstName: String,
        lastName: String,
        sex: String,
        email: String,
        pass: String,
        onSuccess: () -> Unit
    ) {
        if (email.isBlank() || pass.isBlank() || firstName.isBlank() || lastName.isBlank()) {
            errorMessage = "Please fill out all fields."
            return
        }

        isLoading = true
        errorMessage = null

        // 1. Create the Auth account
        auth.createUserWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = task.result?.user?.uid
                    if (userId != null) {
                        // 2. Save the extra details to a "users" collection in Firestore
                        val db = FirebaseFirestore.getInstance()
                        val userProfile = hashMapOf(
                            "firstName" to firstName,
                            "lastName" to lastName,
                            "sex" to sex,
                            "email" to email,
                            "role" to "Unassigned", // <-- CHANGE THIS LINE
                            "createdAt" to System.currentTimeMillis()
                        )

                        db.collection("users").document(userId)
                            .set(userProfile)
                            .addOnSuccessListener {
                                isLoading = false
                                onSuccess()
                            }
                            .addOnFailureListener { exception ->
                                isLoading = false
                                errorMessage =
                                    "Account created, but failed to save profile: ${exception.message}"
                            }
                    } else {
                        isLoading = false
                        onSuccess()
                    }
                } else {
                    isLoading = false
                    errorMessage = task.exception?.message ?: "Sign up failed."
                }
            }
    }

    // --- 3. LOGOUT ---
    fun logout() {
        auth.signOut()
        userRole = null // Instantly clear the gatekeeper!
        currentUser = null
    }

    fun updateUserRole(newRole: String, onSuccess: () -> Unit) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            errorMessage = "No user logged in."
            return
        }

        isLoading = true
        errorMessage = null

        val db = FirebaseFirestore.getInstance()

        // Use .set with SetOptions.merge() to prevent crashes on missing documents!
        val data = hashMapOf("role" to newRole)

        db.collection("users").document(uid)
            .set(data, SetOptions.merge())
            .addOnSuccessListener {
                userRole = newRole // Update our local gatekeeper variable
                isLoading = false
                onSuccess()
            }
            .addOnFailureListener { exception ->
                isLoading = false
                errorMessage = "Failed to save role: ${exception.message}"
                // Print the error to Logcat so we can actually see it if it fails again!
                println("FIREBASE ERROR: ${exception.message}")
            }
    }

    // --- ADD OR UPDATE THIS IN AuthViewModel.kt ---
    fun login(email: String, pass: String, onLoginSuccess: (String?) -> Unit) {
        if (email.isBlank() || pass.isBlank()) {
            errorMessage = "Email and password cannot be empty."
            return
        }

        isLoading = true
        errorMessage = null

        auth.signInWithEmailAndPassword(email, pass)
            .addOnSuccessListener {
                val uid = auth.currentUser?.uid
                if (uid != null) {
                    // Fetch the user's role from Firestore
                    FirebaseFirestore.getInstance().collection("users").document(uid)
                        .get()
                        .addOnSuccessListener { document ->
                            // Grab the role, or default to Unassigned
                            val fetchedRole = document.getString("role") ?: "Unassigned"
                            userRole = fetchedRole
                            isLoading = false

                            // This specifically sends the role back to your LoginScreen!
                            onLoginSuccess(fetchedRole)
                        }
                        .addOnFailureListener { e ->
                            isLoading = false
                            errorMessage = "Failed to fetch role: ${e.message}"
                        }
                }
            }
            .addOnFailureListener { e ->
                isLoading = false
                errorMessage = e.message ?: "Login failed"
            }
    }
}