package com.wharzc.sentry.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.*

data class RfidUser(
    val cardUuid: String = "",
    val uid: String = "",
    val name: String = "",
    val role: String = ""
)

data class FingerprintUser(
    val hardwareId: String = "",
    val name: String = "",
    val fingerName: String = "",
    val uid: String = ""
)

data class AttendanceRecord(
    val date: String = "",
    val studentUid: String = "",
    val name: String = "",
    val status: String = "",
    val timeFormatted: String = ""
)

class SentryViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()

    // System State
    private val _currentMode = MutableStateFlow("ATTENDANCE")
    val currentMode: StateFlow<String> = _currentMode.asStateFlow()

    private val _isBypassing = MutableStateFlow(false)
    val isBypassing: StateFlow<Boolean> = _isBypassing.asStateFlow()

    // Directories
    private val _rfidUsers = MutableStateFlow<List<RfidUser>>(emptyList())
    val rfidUsers: StateFlow<List<RfidUser>> = _rfidUsers.asStateFlow()

    private val _fingerprintUsers = MutableStateFlow<List<FingerprintUser>>(emptyList())
    val fingerprintUsers: StateFlow<List<FingerprintUser>> = _fingerprintUsers.asStateFlow()

    // Attendance Categorized by Date
    private val _attendanceByDate = MutableStateFlow<Map<String, List<AttendanceRecord>>>(emptyMap())
    val attendanceByDate: StateFlow<Map<String, List<AttendanceRecord>>> = _attendanceByDate.asStateFlow()

    init {
        listenToSystemState()
        listenToRfidUsers()
        listenToFingerprints()
        listenToAttendance()
    }

    // --- SYSTEM CONTROLS ---
    private fun listenToSystemState() {
        firestore.collection("SystemState").document("controller")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null && snapshot.exists()) {
                    _currentMode.value = snapshot.getString("current_mode") ?: "ATTENDANCE"
                    _isBypassing.value = snapshot.getBoolean("bypass_trigger") ?: false
                }
            }
    }

    fun toggleSystemMode() {
        val nextMode = if (_currentMode.value == "ATTENDANCE") "ENROLL" else "ATTENDANCE"
        val data = mapOf("current_mode" to nextMode)
        firestore.collection("SystemState").document("controller")
            .set(data, SetOptions.merge())
    }

    fun triggerBypass() {
        val data = mapOf("bypass_trigger" to true)
        firestore.collection("SystemState").document("controller")
            .set(data, SetOptions.merge())
    }

    // --- RFID DIRECTORY ---
    private fun listenToRfidUsers() {
        firestore.collection("users").addSnapshotListener { snapshot, _ ->
            if (snapshot != null) {
                _rfidUsers.value = snapshot.documents.map { doc ->
                    RfidUser(
                        cardUuid = doc.id,
                        uid = doc.getString("uid") ?: "",
                        name = doc.getString("name") ?: "Unnamed",
                        role = doc.getString("role") ?: "Student"
                    )
                }
            }
        }
    }

    fun updateRfidName(cardUuid: String, newName: String) {
        firestore.collection("users").document(cardUuid).update("name", newName)
    }

    fun deleteRfidUser(cardUuid: String) {
        firestore.collection("users").document(cardUuid).delete()
    }

    // --- FINGERPRINT DIRECTORY ---
    private fun listenToFingerprints() {
        firestore.collection("FingerprintRegistry").addSnapshotListener { snapshot, _ ->
            if (snapshot != null) {
                _fingerprintUsers.value = snapshot.documents.map { doc ->
                    FingerprintUser(
                        hardwareId = doc.id,
                        name = doc.getString("name") ?: "Unnamed",
                        fingerName = doc.getString("finger_name") ?: "Finger",
                        uid = doc.getString("uid") ?: ""
                    )
                }
            }
        }
    }

    fun updateFingerprintName(hardwareId: String, newName: String) {
        firestore.collection("FingerprintRegistry").document(hardwareId).update("name", newName)
    }

    fun deleteFingerprintUser(hardwareId: String) {
        firestore.collection("FingerprintRegistry").document(hardwareId).delete()
    }

    // --- ATTENDANCE HISTORY (GROUPED BY DATE) ---
    private fun listenToAttendance() {
        firestore.collectionGroup("students").addSnapshotListener { snapshot, _ ->
            if (snapshot != null) {
                val records = snapshot.documents.mapNotNull { doc ->
                    val pathParts = doc.reference.path.split("/")
                    // Path format: attendance/{date}/teachers/{teacher_uid}/students/{student_uid}
                    val dateKey = if (pathParts.size >= 2) pathParts[1] else "Unknown Date"
                    val timestamp = doc.getLong("timestamp") ?: 0L
                    val timeStr = if (timestamp > 0L) {
                        SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(timestamp * 1000))
                    } else "--:--"

                    AttendanceRecord(
                        date = dateKey,
                        studentUid = doc.id,
                        name = doc.getString("name") ?: "Unknown",
                        status = doc.getString("status") ?: "PRESENT",
                        timeFormatted = timeStr
                    )
                }
                _attendanceByDate.value = records.groupBy { it.date }
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer { SentryViewModel() }
        }
    }
}