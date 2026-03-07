package com.example.sitemate.model

// 1. We define the possible roles restricted to these options
enum class UserRole {
    ENGINEER, FOREMAN, CLIENT
}

// 2. We define the sex options
enum class UserSex {
    MALE, FEMALE
}

// 3. The main User object that holds all the sign-up info
data class User(
    val id: String = "", // Will be used by Firebase later
    val firstName: String,
    val middleInitial: String? = null, // Optional
    val lastName: String,
    val sex: UserSex,
    val email: String,
    val role: UserRole? = null // Null initially, set after Sign-Up
) {
    // 4. This function calculates the exact greeting you asked for
    fun getWelcomeMessage(): String {
        return when (role) {
            UserRole.ENGINEER -> {
                "Welcome to SiteMate,\nEngr. $lastName!"
            }
            UserRole.FOREMAN, UserRole.CLIENT -> {
                val title = if (sex == UserSex.MALE) "Sir" else "Ma'am"
                "Welcome to SiteMate,\n$title $firstName!"
            }
            null -> "Welcome to SiteMate!" // Fallback if role isn't chosen yet
        }
    }
}