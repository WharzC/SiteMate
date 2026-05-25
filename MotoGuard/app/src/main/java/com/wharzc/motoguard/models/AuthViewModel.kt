package com.wharzc.motoguard.models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.google.firebase.auth.UserProfileChangeRequest

// Represents the different states of the login process
sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    // Check if user is already logged in when ViewModel is created
    init {
        if (auth.currentUser != null) {
            _authState.value = AuthState.Success
        }
    }

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("Email and Password cannot be empty.")
            return
        }

        _authState.value = AuthState.Loading

        viewModelScope.launch {
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        _authState.value = AuthState.Success
                    } else {
                        _authState.value =
                            AuthState.Error(task.exception?.localizedMessage ?: "Login failed.")
                    }
                }
        }
    }

    fun signUp(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("Email and Password cannot be empty.")
            return
        }

        _authState.value = AuthState.Loading

        viewModelScope.launch {
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        _authState.value = AuthState.Success
                    } else {
                        _authState.value =
                            AuthState.Error(task.exception?.localizedMessage ?: "Sign up failed.")
                    }
                }
        }
    }

    // Reset state after showing error
    fun resetState() {
        _authState.value = AuthState.Idle
    }

    fun signUp(name: String, email: String, password: String) {
        if (name.isBlank() || email.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("All fields are required.")
            return
        }

        _authState.value = AuthState.Loading

        viewModelScope.launch {
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Automatically add the user's Full Name to their Firebase Profile
                        val user = auth.currentUser
                        val profileUpdates = UserProfileChangeRequest.Builder()
                            .setDisplayName(name)
                            .build()

                        user?.updateProfile(profileUpdates)?.addOnCompleteListener {
                            _authState.value = AuthState.Success
                        }
                    } else {
                        _authState.value =
                            AuthState.Error(task.exception?.localizedMessage ?: "Sign up failed.")
                    }
                }
        }
    }
}