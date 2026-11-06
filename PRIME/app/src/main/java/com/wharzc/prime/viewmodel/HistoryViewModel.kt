package com.wharzc.prime.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.wharzc.prime.data.FertilizerActionLog
import com.wharzc.prime.data.PrimeDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

// Data class mapping exactly to the Firebase JSON pushed by the ESP32
data class HourlyAverage(
    val time: String = "",
    val date: String = "",
    val avgN: Float = 0f,
    val avgP: Float = 0f,
    val avgK: Float = 0f,
    val avgTemp: Float = 0f,
    val avgPh: Float = 0f,
    val avgTurbidity: Float = 0f,
    val avgWaterLvl: Float = 0f
)

class HistoryViewModel(application: Application) : AndroidViewModel(application) {
    private val historyDao = PrimeDatabase.getDatabase(application).historyDao()
    private val dbRef = FirebaseDatabase.getInstance().getReference("devices/PrimeNode1/history")

    private val _monitoringLogs = MutableStateFlow<List<HourlyAverage>>(emptyList())
    val monitoringLogs: StateFlow<List<HourlyAverage>> = _monitoringLogs.asStateFlow()

    private val _fertilizerLogs = MutableStateFlow<List<FertilizerActionLog>>(emptyList())
    val fertilizerLogs: StateFlow<List<FertilizerActionLog>> = _fertilizerLogs.asStateFlow()

    private var firebaseListener: ValueEventListener? = null

    init {
        // Initialize with today's date
        val today = SimpleDateFormat("MM/dd/yyyy", Locale.US).format(Calendar.getInstance().time)
        loadDataForDate(today)
    }

    fun loadDataForDate(targetDate: String) {
        // 1. Load Manual Fertilizer Logs from Room
        viewModelScope.launch {
            historyDao.getFertilizerLogsByDate(targetDate).collect { logs ->
                _fertilizerLogs.value = logs
            }
        }

        // 2. Load Automatic Hourly Averages from Firebase
        firebaseListener?.let { dbRef.removeEventListener(it) } // Remove old listener

        firebaseListener = dbRef.orderByChild("date").equalTo(targetDate)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val logs = mutableListOf<HourlyAverage>()
                    for (child in snapshot.children) {
                        val log = child.getValue(HourlyAverage::class.java)
                        if (log != null) logs.add(log)
                    }
                    // Sort descending so newest hours appear at the top
                    _monitoringLogs.value = logs.sortedByDescending { log: HourlyAverage -> log.time }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("HistoryVM", "Firebase query failed: ${error.message}")
                }
            })
    }

    fun logFertilizerAction(time: String, amPm: String, date: String, quantity: String, reason: String) {
        viewModelScope.launch {
            val log = FertilizerActionLog(
                time = time,
                amPm = amPm,
                date = date,
                quantity = quantity,
                reason = reason,
                fieldAnalysis = "Logged manually by farmer"
            )
            historyDao.insertFertilizerLog(log)
        }
    }
}