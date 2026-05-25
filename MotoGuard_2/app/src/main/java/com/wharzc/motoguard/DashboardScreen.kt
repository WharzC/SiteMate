package com.wharzc.motoguard

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.motoguard_logo),
                            contentDescription = "Logo",
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("MotoGuard", fontWeight = FontWeight.Bold, color = Color(0xFF005691))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { /* Open Drawer */ }) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                },
                        actions = {
                    IconButton(onClick = { navController.navigate("notifications") }) { // Add navigation here
                        Icon(Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = Color.Black
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Status Section
            StatusRow(label = "Status:", value = "ONLINE", valueColor = Color(0xFF2E7D32))
            Divider(modifier = Modifier.padding(vertical = 16.dp))

            // Location Section
            StatusRow(label = "Location:", value = "ROXAS", valueColor = Color.Red)
            Divider(modifier = Modifier.padding(vertical = 16.dp))

            Spacer(modifier = Modifier.height(24.dp))

            // Live Map Button
            Button(
                onClick = { navController.navigate("liveMap") },
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF006699)),
                modifier = Modifier.height(50.dp).fillMaxWidth(0.7f)
            ) {
                Text("VIEW LIVE MAP", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.weight(1f))

            // Bottom Action Grid
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly, // Forces single-line alignment
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Alarm - Premium Internet Icon
                ActionButton(
                    label = "Alarm",
                    iconSource = "https://img.icons8.com/fluency/96/siren.png",
                    bgColor = Color(0xFFFFEBEE)
                ) { /* Alarm logic */ }

                // 2. Trip History
                ActionButton(
                    label = "History",
                    iconSource = R.drawable.ic_trip_path,
                    bgColor = Color(0xFFFFF8E1)
                ) {
                    navController.navigate("tripHistory")
                }

                // 3. Geo-Fence
                ActionButton(
                    label = "Fence",
                    iconSource = R.drawable.ic_geofence,
                    bgColor = Color(0xFFE0F2F1)
                ) { /* Fence logic */ }

                // 4. Settings
                ActionButton(
                    label = "Settings",
                    iconSource = R.drawable.settings,
                    bgColor = Color(0xFFF5F5F5)
                ) {
                    navController.navigate("settings")
                }
            }
            }
        }
    }


@Composable
fun StatusRow(label: String, value: String, valueColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 24.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.weight(1f))
        Text(value, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = valueColor)
    }
}

@Composable
fun ActionButton(
    label: String,
    iconSource: Any,
    bgColor: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(4.dp)
            .width(75.dp) // Fixed width keeps them from jumping to new lines
    ) {
        Surface(
            onClick = onClick,
            shape = CircleShape,
            color = bgColor,
            shadowElevation = 3.dp,
            modifier = Modifier.size(56.dp) // Slightly smaller to fit 4 in a row easily
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.padding(12.dp)
            ) {
                AsyncImage(
                    model = iconSource,
                    contentDescription = label,
                    modifier = Modifier.fillMaxSize(),
                    // This allows internet icons to keep their color,
                    // and local icons to be tinted properly.
                    colorFilter = if (iconSource is Int) null else null
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color.DarkGray,
            maxLines = 1 // Keeps the row tight
        )
    }
}