package com.wharzc.motoguard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,  // <-- added for navigation
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit
) {
    val primaryDarkBlue = Color(0xFF10213E)
    val backgroundColor = Color(0xFFF8F9FB)
    val actionRed = Color(0xFFD32F2F)

    var showLogoutDialog by remember { mutableStateOf(false) }
    var showSecurityDialog by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }

    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { onNavigateBack() }) {
                        Icon(Icons.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = primaryDarkBlue)
            )
        },
        containerColor = backgroundColor
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Profile Card (unchanged)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(primaryDarkBlue.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Person, "Profile", tint = primaryDarkBlue, modifier = Modifier.size(36.dp))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("John Doe", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = primaryDarkBlue)
                        Text("johndoe@example.com", fontSize = 14.sp, color = Color.Gray)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Options Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    // Geofence Settings navigates to GeofenceScreen
                    SettingsItem(icon = Icons.Outlined.Map, title = "Geofence Settings") {
                        navController.navigate("geofence_screen")
                    }
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp), color = Color.LightGray.copy(alpha = 0.4f))
                    SettingsItem(icon = Icons.Outlined.Notifications, title = "Notification Preferences") {}
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp), color = Color.LightGray.copy(alpha = 0.4f))
                    SettingsItem(icon = Icons.Outlined.Shield, title = "Security & Hardware Options") {
                        showSecurityDialog = true
                    }
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp), color = Color.LightGray.copy(alpha = 0.4f))
                    SettingsItem(icon = Icons.Outlined.HelpOutline, title = "Help & Support Support Desk") {
                        showHelpDialog = true
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Logout footer (unchanged)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showLogoutDialog = true }
                    .background(Color.White)
                    .padding(vertical = 20.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.AutoMirrored.Filled.ExitToApp, "Logout", tint = actionRed)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Log Out Account", color = actionRed, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }



}

        // Logout confirmation dialog
        if (showLogoutDialog) {
            AlertDialog(
                onDismissRequest = { showLogoutDialog = false },
                title = { Text("Log Out Confirmation", fontWeight = FontWeight.Bold, color = primaryDarkBlue) },
                text = { Text(text = "Are you sure you want to log out of your account?", color = Color.DarkGray) },
                confirmButton = {
                    TextButton(onClick = {
                        showLogoutDialog = false
                        onLogout()
                    }) {
                        Text("Yes, Logout", color = actionRed, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showLogoutDialog = false }) {
                        Text("Cancel", color = primaryDarkBlue)
                    }
                },
                containerColor = Color.White
            )
        }

        // Security & Hardware pop-up modal with Change Password option
        if (showSecurityDialog) {
            AlertDialog(
                onDismissRequest = { showSecurityDialog = false },
                title = { Text("Security & Hardware", fontWeight = FontWeight.Bold, color = primaryDarkBlue) },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Change Password", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                        OutlinedTextField(
                            value = oldPassword,
                            onValueChange = { oldPassword = it },
                            label = { Text("Old Password") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = primaryDarkBlue,
                                unfocusedBorderColor = Color.Gray
                            )
                        )
                        OutlinedTextField(
                            value = newPassword,
                            onValueChange = { newPassword = it },
                            label = { Text("New Password") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = primaryDarkBlue,
                                unfocusedBorderColor = Color.Gray
                            )
                        )
                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            label = { Text("Confirm New Password") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = primaryDarkBlue,
                                unfocusedBorderColor = Color.Gray
                            )
                        )
                        Button(
                            onClick = {
                                if (newPassword.isNotEmpty() && newPassword == confirmPassword) {
                                    // In a real app, call API to change password
                                    Toast.makeText(context, "Password changed successfully", Toast.LENGTH_SHORT).show()
                                    showSecurityDialog = false
                                    // Clear fields
                                    oldPassword = ""
                                    newPassword = ""
                                    confirmPassword = ""
                                } else {
                                    Toast.makeText(context, "Passwords do not match or are empty", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = primaryDarkBlue)
                        ) {
                            Text("Update Password", color = Color.White)
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showSecurityDialog = false }) {
                        Text("Close", color = primaryDarkBlue, fontWeight = FontWeight.Bold)
                    }
                },
                containerColor = Color.White,
                properties = DialogProperties(usePlatformDefaultWidth = false)
            )
        }

        // Help & Support large modal showing support email
        if (showHelpDialog) {
            AlertDialog(
                onDismissRequest = { showHelpDialog = false },
                title = { Text("Help & Support", fontWeight = FontWeight.Bold, color = primaryDarkBlue) },
                text = {
                    Text(
                        text = "Email our customer support:\ndev.motorguard@gmail.com",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = primaryDarkBlue,
                        lineHeight = 28.sp
                    )
                },
                confirmButton = {
                    TextButton(onClick = { showHelpDialog = false }) {
                        Text("OK", color = primaryDarkBlue, fontWeight = FontWeight.Bold)
                    }
                },
                containerColor = Color.White,
                properties = DialogProperties(usePlatformDefaultWidth = false)
            )
        }
    }



@Composable
fun SettingsItem(icon: ImageVector, title: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = title, tint = Color(0xFF10213E), modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color(0xFF10213E), modifier = Modifier.weight(1f))
        Icon(imageVector = Icons.Filled.ChevronRight, contentDescription = "Go", tint = Color.LightGray, modifier = Modifier.size(24.dp))
    }
}