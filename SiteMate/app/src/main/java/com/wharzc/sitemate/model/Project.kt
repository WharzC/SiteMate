package com.wharzc.sitemate.model

import androidx.compose.runtime.mutableStateListOf


// Blueprint for what a "Project" is
data class Project(
    val id: String = "",
    val name: String = "",
    val location: String = "",
    val progress: Float = 0f,
    val status: String = "Pending",
    val budget: Double = 0.0, // <-- Change this to Double
    val spent: Double = 0.0,  // <-- Change this to Double
    val criticalDelays: Int = 0,
    val type: String = "",
    val category: String = "",
    val subCategory: String = ""
)

