package com.wharzc.sitemate.model

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth // NEW: Required to identify the Engineer
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ProjectCreationViewModel : ViewModel() {
    // --- 1. NAME SCREEN ---
    var projectName by mutableStateOf("")
    var projectDesc by mutableStateOf("")

    // --- 2. CLASSIFICATION SCREEN ---
    var selectedType by mutableStateOf("")
    var selectedCategory by mutableStateOf("")
    var selectedSubCategory by mutableStateOf("")

    // --- 3. DETAILS SCREEN ---
    var manpower by mutableStateOf("")
    var startDate by mutableStateOf("")
    var endDate by mutableStateOf("")

    // --- 4. LOCATION SCREEN ---
    var selectedCity by mutableStateOf("")
    var selectedBarangay by mutableStateOf("")
    var streetAddress by mutableStateOf("")
    var landmark by mutableStateOf("")

    // --- 5. UPLOAD SCREEN ---
    var sitePhotos by mutableStateOf<List<Uri>>(emptyList())
    var documentPhotos by mutableStateOf<List<Uri>>(emptyList())

    var cityList by mutableStateOf<List<String>>(emptyList())
        private set
    var barangayList by mutableStateOf<List<String>>(emptyList())
        private set

    fun fetchCities() {
        // Pointing exactly to the JSON path you specified
        val ref = FirebaseDatabase.getInstance().getReference("0/ORIENTAL MINDORO/municipality_list")

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val cities = mutableListOf<String>()
                for (child in snapshot.children) {
                    child.key?.let { cities.add(it) } // Grabs "BACO", "BANSUD", etc.
                }
                cityList = cities.sorted()
            }
            override fun onCancelled(error: DatabaseError) {
                println("Firebase RTDB Error: ${error.message}")
            }
        })
    }

    fun fetchBarangays(city: String) {
        val ref = FirebaseDatabase.getInstance().getReference("0/ORIENTAL MINDORO/municipality_list/$city/barangay_list")

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val barangays = mutableListOf<String>()
                for (child in snapshot.children) {
                    val barangay = child.getValue(String::class.java)
                    if (barangay != null) barangays.add(barangay)
                }
                barangayList = barangays.sorted()

                // Reset the selected barangay since the city changed!
                selectedBarangay = ""
            }
            override fun onCancelled(error: DatabaseError) {
                println("Firebase RTDB Error: ${error.message}")
            }
        })
    }

    // --- FIREBASE SAVE FUNCTION ---
    fun saveProjectToFirestore(
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val db = FirebaseFirestore.getInstance()
        val user = FirebaseAuth.getInstance().currentUser

        val formattedLocation = if (landmark.isNotBlank()) {
            "$streetAddress, ($landmark), $selectedBarangay, $selectedCity, Oriental Mindoro, Region 4-B "
        } else {
            "$streetAddress, $selectedBarangay, $selectedCity, Oriental Mindoro, Region 4-B"
        }

        // Failsafe if the session dropped
        if (user == null) {
            onError(Exception("User is not logged in."))
            return
        }

        // 1. Fetch the Engineer's profile to get their real name
        db.collection("users").document(user.uid).get()
            .addOnSuccessListener { userDoc ->
                val firstName = userDoc.getString("firstName") ?: ""
                val lastName = userDoc.getString("lastName") ?: ""

                // Combine them
                val fullName = "$firstName $lastName".trim()

                // 2. Package all the new data into the map
                val projectMap = hashMapOf(
                    "name" to projectName,
                    "description" to projectDesc,
                    "type" to selectedType,
                    "category" to selectedCategory,
                    "subCategory" to selectedSubCategory,
                    "manpower" to manpower,
                    "startDate" to startDate,
                    "endDate" to endDate,
                    "city" to selectedCity,
                    "barangay" to selectedBarangay,
                    "street" to streetAddress,
                    "landmark" to landmark,
                    "location" to formattedLocation,
                    "createdAt" to System.currentTimeMillis(),

                    // --- THE NEW REQUIRED FIELDS ---
                    "engineerId" to user.uid,
                    "engineerName" to fullName.ifEmpty { "SiteMate Engineer" },
                    "assignedClients" to emptyList<String>(),
                    "pendingClients" to emptyList<String>()
                )

                // 3. Save the project
                db.collection("projects")
                    .add(projectMap)
                    .addOnSuccessListener {
                        reset()
                        onSuccess()
                    }
                    .addOnFailureListener { exception ->
                        onError(exception)
                    }
            }
            .addOnFailureListener { exception ->
                // Catches errors if fetching the user profile fails
                onError(exception)
            }
    }

    fun reset() {
        projectName = ""
        projectDesc = ""
        selectedType = ""
        selectedCategory = ""
        selectedSubCategory = ""
        manpower = ""
        startDate = ""
        endDate = ""
        selectedCity = ""
        selectedBarangay = ""
        streetAddress = ""
        landmark = ""
        cityList = emptyList()
        barangayList = emptyList()
        sitePhotos = emptyList()
        documentPhotos = emptyList()
    }
}