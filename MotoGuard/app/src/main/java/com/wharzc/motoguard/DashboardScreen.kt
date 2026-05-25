package com.wharzc.motoguard

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Notifications
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun DashboardScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToTripHistory: () -> Unit,
    onNavigateToLiveTracking: () -> Unit
) {
    val backgroundColor = Color(0xFFF8F9FB)
    val actionBlue = Color(0xFF2196F3)
    val actionRed = Color(0xFFF44336)
    val primaryDarkBlue = Color(0xFF10213E)
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val currentUser = auth.currentUser

    // Profile data state
    var motorcycleName by remember { mutableStateOf("My Motorcycle") }
    var motorcycleModel by remember { mutableStateOf("Honda Click 125i") }
    var phoneNumber by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var showEditDialog by remember { mutableStateOf(false) }

    // Temporary states for dialog inputs
    var tempMotorcycleName by remember { mutableStateOf(motorcycleName) }
    var tempMotorcycleModel by remember { mutableStateOf(motorcycleModel) }
    var tempPhoneNumber by remember { mutableStateOf(phoneNumber) }

    // Unread notifications count
    var unreadCount by remember { mutableStateOf(0) }

    // Load user profile from Firestore and setup real‑time unread count
    LaunchedEffect(Unit) {
        if (currentUser != null) {
            try {
                val doc = db.collection("users").document(currentUser.uid).get().await()
                if (doc.exists()) {
                    motorcycleName = doc.getString("motorcycleName") ?: "My Motorcycle"
                    motorcycleModel = doc.getString("motorcycleModel") ?: "Honda Click 125i"
                    phoneNumber = doc.getString("phoneNumber") ?: ""
                } else {
                    db.collection("users").document(currentUser.uid).set(
                        mapOf(
                            "motorcycleName" to motorcycleName,
                            "motorcycleModel" to motorcycleModel,
                            "phoneNumber" to phoneNumber
                        )
                    ).await()
                }
                isLoading = false
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Failed to load profile", Toast.LENGTH_SHORT).show()
                isLoading = false
            }

            // Listen for unread notifications
            db.collection("users").document(currentUser.uid)
                .collection("notifications")
                .whereEqualTo("read", false)
                .addSnapshotListener { snapshot, _ ->
                    unreadCount = snapshot?.size() ?: 0
                }
        }
    }

    // Simulate battery check every 60 seconds (in real app, get from hardware)
    LaunchedEffect(Unit) {
        while (true) {
            delay(60000)
            // Mock battery level – replace with actual hardware reading
            val batteryLevel = 15
            if (batteryLevel <= 15) {
                NotificationRepository.addNotification(
                    context,
                    "Battery Low",
                    "Tracker battery is at ${batteryLevel}%. Please charge soon.",
                    "battery"
                )
            }
        }
    }

    // Save profile to Firestore
    fun saveProfile(name: String, model: String, phone: String) {
        if (currentUser == null) return
        scope.launch {
            try {
                db.collection("users").document(currentUser.uid).set(
                    mapOf(
                        "motorcycleName" to name,
                        "motorcycleModel" to model,
                        "phoneNumber" to phone
                    )
                ).await()
                motorcycleName = name
                motorcycleModel = model
                phoneNumber = phone
                Toast.makeText(context, "Profile updated", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to save: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // SOS state (unchanged)
    var sosState by remember { mutableIntStateOf(0) }
    LaunchedEffect(sosState) {
        if (sosState == 1) {
            delay(1500)
            sosState = 2
        } else if (sosState == 2) {
            delay(10000)
            sosState = 3
        } else if (sosState == 3) {
            delay(2000)
            sosState = 0
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // Header with notification badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateToSettings) {
                    Icon(Icons.Outlined.AccountCircle, "Profile", modifier = Modifier.size(28.dp), tint = primaryDarkBlue)
                }
                Text("MotoGuard", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = primaryDarkBlue)
                Box {
                    IconButton(onClick = onNavigateToNotifications) {
                        Icon(Icons.Outlined.Notifications, "Notifications", modifier = Modifier.size(28.dp), tint = primaryDarkBlue)
                    }
                    if (unreadCount > 0) {
                        Badge(
                            modifier = Modifier.offset(x = 12.dp, y = (-4).dp),
                            containerColor = Color.Red,
                            contentColor = Color.White
                        ) {
                            Text(if (unreadCount > 99) "99+" else unreadCount.toString())
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Motorcycle Profile Card
            if (!isLoading) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = motorcycleName,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = primaryDarkBlue
                                )
                                Text(
                                    text = motorcycleModel,
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )
                                if (phoneNumber.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Filled.Phone, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Gray)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(text = phoneNumber, fontSize = 14.sp, color = Color.DarkGray)
                                    }
                                }
                            }
                            IconButton(onClick = {
                                tempMotorcycleName = motorcycleName
                                tempMotorcycleModel = motorcycleModel
                                tempPhoneNumber = phoneNumber
                                showEditDialog = true
                            }) {
                                Icon(Icons.Filled.Edit, contentDescription = "Edit Profile", tint = primaryDarkBlue)
                            }
                        }
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = primaryDarkBlue)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Current Location Card
            Card(
                onClick = onNavigateToLiveTracking,
                modifier = Modifier.fillMaxWidth().height(90.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(modifier = Modifier.fillMaxSize().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(64.dp).background(Color(0xFFE0E0E0), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.LocationOn, contentDescription = null, tint = Color.Gray)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(verticalArrangement = Arrangement.Center) {
                        Text("Current Location: Roxas", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.DarkGray)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Last updated: 2 mins ago", fontSize = 12.sp, color = Color.Gray)
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Action Buttons
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(
                    onClick = onNavigateToTripHistory,
                    modifier = Modifier.weight(1f).height(64.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = actionBlue)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Map, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Trip History", fontSize = 14.sp)
                    }
                }
                Button(
                    onClick = { sosState = 1 },
                    modifier = Modifier.weight(1f).height(64.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = actionRed)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.NotificationsActive, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("SOS/Alarm", fontSize = 14.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // SOS Overlay (unchanged)
        if (sosState != 0) {
            Box(
                modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)).clickable(enabled = false) {},
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (sosState == 1 || sosState == 2) {
                            CircularProgressIndicator(color = actionRed, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(24.dp))
                        } else {
                            Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                        val message = when(sosState) {
                            1 -> "Sending SOS/Alarm..."
                            2 -> "Alarming for 10 seconds"
                            3 -> "Alarm sent."
                            else -> ""
                        }
                        Text(message, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = primaryDarkBlue)
                    }
                }
            }
        }

        // Edit Profile Dialog (unchanged)
        if (showEditDialog) {
            Dialog(
                onDismissRequest = { showEditDialog = false },
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
                        Text("Edit Motorcycle Profile", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = primaryDarkBlue)
                        OutlinedTextField(
                            value = tempMotorcycleName,
                            onValueChange = { tempMotorcycleName = it },
                            label = { Text("Motorcycle Name") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = tempMotorcycleModel,
                            onValueChange = { tempMotorcycleModel = it },
                            label = { Text("Motorcycle Model") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = tempPhoneNumber,
                            onValueChange = { tempPhoneNumber = it },
                            label = { Text("Phone Number (for alerts)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(onClick = { showEditDialog = false }) {
                                Text("Cancel", color = Color.Gray)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    if (tempMotorcycleName.isNotBlank() && tempMotorcycleModel.isNotBlank()) {
                                        saveProfile(tempMotorcycleName, tempMotorcycleModel, tempPhoneNumber)
                                        showEditDialog = false
                                    } else {
                                        Toast.makeText(context, "Motorcycle name and model cannot be empty", Toast.LENGTH_SHORT).show()
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
}