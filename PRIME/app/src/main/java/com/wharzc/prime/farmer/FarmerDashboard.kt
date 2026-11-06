package com.wharzc.prime.farmer

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.wharzc.prime.farmer.FertilizerScreen
import com.wharzc.prime.viewmodel.SensorViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wharzc.prime.viewmodel.FertilizerViewModel
import com.wharzc.prime.viewmodel.HistoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FarmerDashboard(viewModel: SensorViewModel, onLogout: () -> Unit) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var farmerName by remember { mutableStateOf("Loading...") }
    val fertViewModel: FertilizerViewModel = viewModel()
    val historyViewModel: HistoryViewModel = viewModel()

    val bgColor = Color(0xFF195532)
    val cardColor = Color(0xFF2A5339)
    val neonGreen = Color(0xFF00FFCC)

    LaunchedEffect(Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            FirebaseFirestore.getInstance().collection("users").document(userId)
                .get().addOnSuccessListener { doc ->
                    farmerName = doc.getString("name") ?: "Farmer"
                }
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Confirm Logout", color = Color.White) },
            text = { Text("Are you sure you want to log out of your farmer account?", color = Color.LightGray) },
            containerColor = cardColor,
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    FirebaseAuth.getInstance().signOut()
                    onLogout()
                }) { Text("Log Out", color = Color(0xFFEF5350), fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) { Text("Cancel", color = neonGreen) }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("PRIME", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text("Welcome, $farmerName!", color = neonGreen, fontSize = 14.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = bgColor),
                actions = {
                    IconButton(onClick = { showLogoutDialog = true }) {
                        Icon(Icons.Filled.Logout, contentDescription = "Logout", tint = Color.White)
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(containerColor = cardColor) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Filled.Sensors, contentDescription = "Monitoring") },
                    label = { Text("Monitoring") },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = bgColor, selectedTextColor = neonGreen, indicatorColor = neonGreen, unselectedIconColor = Color.LightGray, unselectedTextColor = Color.LightGray)
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Filled.Eco, contentDescription = "Fertilizer") },
                    label = { Text("Fertilizer") },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = bgColor, selectedTextColor = neonGreen, indicatorColor = neonGreen, unselectedIconColor = Color.LightGray, unselectedTextColor = Color.LightGray)
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "History") },
                    label = { Text("History") },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = bgColor, selectedTextColor = neonGreen, indicatorColor = neonGreen, unselectedIconColor = Color.LightGray, unselectedTextColor = Color.LightGray)
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Filled.Person, contentDescription = "Profile") },
                    label = { Text("Profile") },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = bgColor, selectedTextColor = neonGreen, indicatorColor = neonGreen, unselectedIconColor = Color.LightGray, unselectedTextColor = Color.LightGray)
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (selectedTab) {
                0 -> MonitoringScreen(viewModel)
                1 -> FertilizerScreen(viewModel, fertViewModel)
                2 -> HistoryScreen(historyViewModel)
                3 -> ProfileScreen(onSignOut = onLogout)
            }
        }
    }
}