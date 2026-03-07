package com.wharzc.sitemate.screens.project

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.wharzc.sitemate.model.ProfileViewModel
import com.wharzc.sitemate.ui.theme.SiteMateBlue
import com.wharzc.sitemate.ui.theme.SiteMateOrange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    profileViewModel: ProfileViewModel = viewModel()
) {
    var showLogoutDialog by remember { mutableStateOf(false) }

    // Fetch the real user data from Firestore when the screen opens
    LaunchedEffect(Unit) {
        profileViewModel.fetchUserProfile()
    }

    Scaffold(
        containerColor = Color(0xFFF8F9FA)
        // The duplicate bottomBar block has been completely removed from here!
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                // Adjusting top padding to account for the removed nav bar
                .padding(top = 48.dp, start = 24.dp, end = 24.dp, bottom = paddingValues.calculateBottomPadding()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // 1. DYNAMIC AVATAR
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(SiteMateBlue.copy(alpha = 0.1f), shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (profileViewModel.isLoading) {
                    CircularProgressIndicator(color = SiteMateBlue, modifier = Modifier.size(30.dp))
                } else {
                    Text(
                        // Uses a fallback to prevent crash if userName is empty
                        text = profileViewModel.userName.takeIf { it.isNotBlank() }?.take(1)?.uppercase() ?: "?",
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold,
                        color = SiteMateBlue
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Account Details", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = SiteMateBlue)
            Spacer(modifier = Modifier.height(32.dp))

            // 2. LIVE INFO CARD
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    InfoRow(label = "Full Name", value = profileViewModel.userName)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.LightGray.copy(alpha = 0.4f))
                    InfoRow(label = "Email", value = profileViewModel.userEmail)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.LightGray.copy(alpha = 0.4f))
                    InfoRow(label = "System Role", value = profileViewModel.userRole, isHighlight = true)
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // 3. LOGOUT BUTTON
            Button(
                onClick = { showLogoutDialog = true },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Log Out Session", color = Color.White, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }

    // --- LOGOUT CONFIRMATION ---
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Confirm Logout") },
            text = { Text("Are you sure you want to end your current session?") },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    profileViewModel.logout {
                        navController.navigate("login") { popUpTo(0) { inclusive = true } }
                    }
                }) { Text("Log Out", color = Color.Red, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun InfoRow(label: String, value: String, isHighlight: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = Color.Gray, fontSize = 14.sp)
        Text(
            text = value,
            color = if (isHighlight) SiteMateOrange else SiteMateBlue,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )
    }
}