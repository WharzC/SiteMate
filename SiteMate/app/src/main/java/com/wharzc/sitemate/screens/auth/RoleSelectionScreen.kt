package com.wharzc.sitemate.screens.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wharzc.sitemate.R
import com.wharzc.sitemate.ui.theme.SiteMateBlue
import com.wharzc.sitemate.ui.theme.SiteMateOrange

@Composable
fun RoleSelectionScreen(
    // We added a callback function so the screen knows when to stop loading!
    onRoleSelected: (String, (Boolean, String) -> Unit) -> Unit
) {
    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") } // NEW: Holds our error

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.sitemate_logo),
            contentDescription = "Robot Constructor",
            modifier = Modifier.size(100.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text("Choose Your Role", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = SiteMateBlue)
        Spacer(modifier = Modifier.height(24.dp))

        // --- ERROR MESSAGE DISPLAY ---
        if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                color = Color.Red,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        if (isSaving) {
            CircularProgressIndicator(color = SiteMateOrange)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Setting up your workspace...", color = Color.Gray)
        } else {
            // Engineer Button
            RoleCard(title = "Engineer", icon = Icons.Filled.Engineering) {
                isSaving = true
                errorMessage = ""
                onRoleSelected("Engineer") { success, errorMsg ->
                    if (!success) { isSaving = false; errorMessage = errorMsg }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))


            // Client Button
            RoleCard(title = "Client", icon = Icons.Filled.Person) {
                isSaving = true
                errorMessage = ""
                onRoleSelected("Client") { success, errorMsg ->
                    if (!success) { isSaving = false; errorMessage = errorMsg }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoleCard(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(80.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = title, tint = SiteMateOrange, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Text(title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = SiteMateBlue)
        }
    }
}