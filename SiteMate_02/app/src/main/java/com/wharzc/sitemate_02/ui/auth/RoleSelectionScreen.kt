package com.wharzc.sitemate_02.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wharzc.sitemate_02.R
import com.wharzc.sitemate_02.model.UserRole

@Composable
fun RoleSelectionScreen(
    // Removed onNewUserClick because this screen is post-signup only
    onRoleSelected: (UserRole) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // --- 1. The Logo ---
        Image(
            painter = painterResource(id = R.drawable.sitemate_logo),
            contentDescription = "SiteMate Logo",
            modifier = Modifier.size(100.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // --- 2. The Header ---
        Text(
            text = "Choose Your Role",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(32.dp))

        // --- 3. The Cards ---
        // Engineer Card
        RoleCard(
            title = "Engineer",
            icon = Icons.Filled.Engineering,
            iconColor = Color(0xFF2196F3),
            onClick = { onRoleSelected(UserRole.ENGINEER) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Client Card
        RoleCard(
            title = "Client",
            icon = Icons.Filled.Groups,
            iconColor = Color(0xFF009688),
            onClick = { onRoleSelected(UserRole.CLIENT) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Foreman Card
        RoleCard(
            title = "Foreman",
            icon = Icons.Filled.Construction,
            iconColor = Color(0xFFFF9800),
            onClick = { onRoleSelected(UserRole.FOREMAN) }
        )

        // Removed the "Sign up" text and spacer from here
    }
}

@Composable
fun RoleCard(
    title: String,
    icon: ImageVector,
    iconColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = iconColor
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }
    }
}