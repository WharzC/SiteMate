package com.wharzc.prime.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.wharzc.prime.model.SensorData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SensorViewModel : ViewModel() {
    private val dbRef = FirebaseDatabase.getInstance().getReference("devices/PrimeNode1")

    private val _sensorData = MutableStateFlow(SensorData())
    val sensorData: StateFlow<SensorData> = _sensorData.asStateFlow()

    init {
        // addValueEventListener stays open continuously.
        // It triggers automatically whenever any child data changes.
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val data = snapshot.getValue(SensorData::class.java)
                    if (data != null) {
                        Log.d("PRIME_LIVE", "Data updated successfully: $data")
                        _sensorData.value = data
                    } else {
                        Log.w("PRIME_LIVE", "Snapshot exists but parsed as null.")
                    }
                } catch (e: Exception) {
                    // This catches silent parsing crashes (e.g., mismatching Int vs Double)
                    Log.e("PRIME_LIVE", "Data parsing failed: ${e.message}")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("PRIME_LIVE", "Firebase sync cancelled/failed: ${error.message}")
            }
        })
    }
}