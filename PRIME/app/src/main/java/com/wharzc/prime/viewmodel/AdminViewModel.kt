package com.wharzc.prime.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.wharzc.prime.model.FarmerUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AdminViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    private val _farmers = MutableStateFlow<List<FarmerUser>>(emptyList())
    val farmers: StateFlow<List<FarmerUser>> = _farmers.asStateFlow()

    init {
        db.collection("users")
            .whereEqualTo("role", "farmer")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val farmerList = snapshot.documents.map { doc ->
                        FarmerUser(
                            id = doc.id,
                            name = doc.getString("name") ?: "Unknown",
                            email = doc.getString("email") ?: "No Email"
                        )
                    }
                    _farmers.value = farmerList
                }
            }
    }
}