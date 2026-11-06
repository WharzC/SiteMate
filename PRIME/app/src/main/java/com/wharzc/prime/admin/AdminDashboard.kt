package com.wharzc.prime.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboard(onLogout: () -> Unit) {
    var selectedTab by remember { mutableIntStateOf(0) }

    val bgColor = Color(0xFF195532)
    val cardColor = Color(0xFF2A5339)
    val neonGreen = Color(0xFF00FFCC)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("PRIME Admin Portal", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text("System Control Center", color = neonGreen, fontSize = 14.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = bgColor)
            )
        },
        bottomBar = {
            NavigationBar(containerColor = cardColor) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Filled.Wifi, contentDescription = "Wi-Fi Config") },
                    label = { Text("Wi-Fi") },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = bgColor, selectedTextColor = neonGreen, indicatorColor = neonGreen, unselectedIconColor = Color.LightGray, unselectedTextColor = Color.LightGray)
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.AutoMirrored.Filled.ListAlt, contentDescription = "Audit Logs") },
                    label = { Text("Logs") },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = bgColor, selectedTextColor = neonGreen, indicatorColor = neonGreen, unselectedIconColor = Color.LightGray, unselectedTextColor = Color.LightGray)
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Filled.People, contentDescription = "Users") },
                    label = { Text("Users") },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = bgColor, selectedTextColor = neonGreen, indicatorColor = neonGreen, unselectedIconColor = Color.LightGray, unselectedTextColor = Color.LightGray)
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Filled.Info, contentDescription = "About & Logout") },
                    label = { Text("About") },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = bgColor, selectedTextColor = neonGreen, indicatorColor = neonGreen, unselectedIconColor = Color.LightGray, unselectedTextColor = Color.LightGray)
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (selectedTab) {
                0 -> AdminWiFiScreen()
                1 -> AdminAuditLogsScreen()
                2 -> AdminUsersScreen()
                3 -> AdminAboutScreen(onLogout = onLogout)
            }
        }
    }
}

@Composable
fun AdminAboutScreen(onLogout: () -> Unit) {
    val bgColor = Color(0xFF195532)
    val cardColor = Color(0xFF356948)
    val lightGreenText = Color(0xFF67C27B)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "About PRIME Admin",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "System Information & Controls",
            color = lightGreenText,
            fontSize = 13.sp,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = cardColor),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Palay Resource and Irrigation Monitoring for Fertilizer Efficiency",
                    color = lightGreenText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Developers:", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(text = "• Reshallyn D. Mortel\n• Silwen Dellova Galos\n• Jefferson Naquita\n• Cecille Ariane Dela Peña", color = Color.LightGray, fontSize = 13.sp)

                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Consultant:", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(text = "• AphelioS", color = Color.LightGray, fontSize = 13.sp)
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                FirebaseAuth.getInstance().signOut()
                onLogout()
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Logout,
                contentDescription = "Sign Out",
                modifier = Modifier.size(20.dp),
                tint = Color.White
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Sign Out", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}