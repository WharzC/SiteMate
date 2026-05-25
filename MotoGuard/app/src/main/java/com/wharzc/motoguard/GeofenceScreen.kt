package com.wharzc.motoguard

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import androidx.compose.ui.text.input.KeyboardType

data class Geofence(
    val id: String = "",          // Firestore document ID
    val name: String,
    val latitude: Double,
    val longitude: Double
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeofenceScreen(navController: NavController) {
    val primaryDarkBlue = Color(0xFF10213E)
    val backgroundColor = Color(0xFFF8F9FB)
    val actionRed = Color(0xFFD32F2F)

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val currentUser = auth.currentUser

    var geofences by remember { mutableStateOf<List<Geofence>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showAddDialog by remember { mutableStateOf(false) }
    var selectionMode by remember { mutableStateOf(false) }
    var selectedIds by remember { mutableStateOf<Set<String>>(emptySet()) }

    // New geofence input fields
    var newName by remember { mutableStateOf("") }
    var newLat by remember { mutableStateOf("") }
    var newLng by remember { mutableStateOf("") }

    // Load geofences from Firestore
    suspend fun loadGeofences() {
        if (currentUser == null) return
        try {
            val snapshot = db.collection("users")
                .document(currentUser.uid)
                .collection("geofences")
                .get()
                .await()
            geofences = snapshot.documents.mapNotNull { doc ->
                val name = doc.getString("name") ?: return@mapNotNull null
                val lat = doc.getDouble("latitude") ?: return@mapNotNull null
                val lng = doc.getDouble("longitude") ?: return@mapNotNull null
                Geofence(doc.id, name, lat, lng)
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to load geofences: ${e.message}", Toast.LENGTH_SHORT)
                .show()
        } finally {
            isLoading = false
        }
    }

    // Add new geofence
    suspend fun addGeofence(name: String, lat: Double, lng: Double) {
        if (currentUser == null) return
        try {
            val data = hashMapOf(
                "name" to name,
                "latitude" to lat,
                "longitude" to lng,
                "createdAt" to System.currentTimeMillis()
            )
            db.collection("users")
                .document(currentUser.uid)
                .collection("geofences")
                .add(data)
                .await()
            Toast.makeText(context, "Geofence added", Toast.LENGTH_SHORT).show()
            loadGeofences()
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to add: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // Delete selected geofences
    suspend fun deleteSelected() {
        if (currentUser == null) return
        try {
            val batch = db.batch()
            selectedIds.forEach { id ->
                val ref = db.collection("users")
                    .document(currentUser.uid)
                    .collection("geofences")
                    .document(id)
                batch.delete(ref)
            }
            batch.commit().await()
            Toast.makeText(context, "Deleted ${selectedIds.size} geofence(s)", Toast.LENGTH_SHORT)
                .show()
            selectionMode = false
            selectedIds = emptySet()
            loadGeofences()
        } catch (e: Exception) {
            Toast.makeText(context, "Delete failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // Initial load
    LaunchedEffect(Unit) {
        loadGeofences()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Geofence Settings",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = primaryDarkBlue)
            )
        },
        containerColor = backgroundColor
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = { showAddDialog = true },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryDarkBlue)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Geofence")
                }
                Button(
                    onClick = {
                        if (selectionMode && selectedIds.isNotEmpty()) {
                            scope.launch { deleteSelected() }
                        } else if (!selectionMode && geofences.isNotEmpty()) {
                            selectionMode = true
                        } else if (geofences.isEmpty()) {
                            Toast.makeText(context, "No geofences to delete", Toast.LENGTH_SHORT)
                                .show()
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = if (selectionMode) actionRed else Color.Gray)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (selectionMode) "Confirm Delete" else "Delete Geofences")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // List of geofences
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = primaryDarkBlue)
                }
            } else if (geofences.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Text(
                        text = "No geofences added. Tap 'Add Geofence' to create one.",
                        modifier = Modifier.padding(24.dp),
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(geofences) { geofence ->
                        val isSelected = selectedIds.contains(geofence.id)
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = selectionMode) {
                                    selectedIds = if (isSelected) {
                                        selectedIds.minus(geofence.id)
                                    } else {
                                        selectedIds.plus(geofence.id)
                                    }
                                },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) primaryDarkBlue.copy(alpha = 0.1f) else Color.White
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (selectionMode) {
                                    Checkbox(
                                        checked = isSelected,
                                        onCheckedChange = null,
                                        colors = CheckboxDefaults.colors(checkedColor = primaryDarkBlue)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = geofence.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = primaryDarkBlue
                                    )
                                    Text(
                                        text = "📍 ${geofence.latitude}, ${geofence.longitude}",
                                        fontSize = 13.sp,
                                        color = Color.DarkGray
                                    )
                                }
                                if (!selectionMode) {
                                    Icon(
                                        Icons.Default.Place,
                                        contentDescription = null,
                                        tint = actionRed
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Geofence Dialog
    if (showAddDialog) {
        Dialog(
            onDismissRequest = { showAddDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        "Add Geofence",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryDarkBlue
                    )

                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("Geofence Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = newLat,
                        onValueChange = {
                            // Allow only digits, dot, comma, and minus sign
                            val filtered = it.filter { char ->
                                char.isDigit() || char == '.' || char == ',' || char == '-'
                            }
                            newLat = filtered
                        },
                        label = { Text("Latitude") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    OutlinedTextField(
                        value = newLng,
                        onValueChange = {
                            val filtered = it.filter { char ->
                                char.isDigit() || char == '.' || char == ',' || char == '-'
                            }
                            newLng = filtered
                        },
                        label = { Text("Longitude") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { showAddDialog = false }) {
                            Text("Cancel", color = Color.Gray)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val name = newName.trim()
                                // Clean and convert
                                val latStr = newLat.trim().replace(',', '.')
                                val lngStr = newLng.trim().replace(',', '.')

                                // Debug log (check Logcat)
                                android.util.Log.d(
                                    "GeofenceDebug",
                                    "Lat raw: '${newLat}' -> cleaned: '$latStr'"
                                )
                                android.util.Log.d(
                                    "GeofenceDebug",
                                    "Lng raw: '${newLng}' -> cleaned: '$lngStr'"
                                )

                                val lat = latStr.toDoubleOrNull()
                                val lng = lngStr.toDoubleOrNull()

                                when {
                                    name.isEmpty() -> Toast.makeText(
                                        context,
                                        "Please enter a geofence name",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    lat == null -> Toast.makeText(
                                        context,
                                        "Invalid latitude: '$latStr'\nUse numbers like 12.592623",
                                        Toast.LENGTH_LONG
                                    ).show()

                                    lng == null -> Toast.makeText(
                                        context,
                                        "Invalid longitude: '$lngStr'\nUse numbers like 121.507488",
                                        Toast.LENGTH_LONG
                                    ).show()

                                    else -> {
                                        scope.launch {
                                            addGeofence(name, lat, lng)
                                        }
                                        showAddDialog = false
                                        newName = ""
                                        newLat = ""
                                        newLng = ""
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = primaryDarkBlue)
                        ) {
                            Text("Save", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}