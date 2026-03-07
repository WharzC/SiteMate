package com.wharzc.sitemate_02.model

enum class UserRole { ENGINEER, FOREMAN, CLIENT }
enum class UserSex { MALE, FEMALE }

data class User(
    val firstName: String,
    val lastName: String,
    val sex: UserSex,
    val email: String,
    val role: UserRole? = null
)