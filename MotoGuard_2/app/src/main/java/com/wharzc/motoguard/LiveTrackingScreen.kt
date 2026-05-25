package com.wharzc.motoguard // Ensure this is your correct package

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveTrackingScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Live Tracking",
                        fontWeight = FontWeight.Bold,
                        color = Color.Red,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) { // Standard back navigation
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.Black)
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
        ) {
            // 1. Static Map Image Placeholder
            // (Ensure you have res/drawable/static_map_placeholder.png)
            Image(
                painter = painterResource(id = R.drawable.static_map_placeholder),
                contentDescription = "Static Map View",
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.6f), // Takes up top 60%
                contentScale = ContentScale.FillBounds // Stretch to fill area like reference
            )

            // 2. Data Section (Speed/Battery)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.25f) // Middle 25%
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center
            ) {
                DataRow(label = "Speed:", value = "42 km/h")
                Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color.Gray, thickness = 1.dp)
                DataRow(label = "Battery:", value = "89%")
            }

            // 3. Action Buttons Section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.15f) // Bottom 15%
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                MapActionButton("LOCK ENGINE", onClick = { /* Logic later */ })
                MapActionButton("ALARM", onClick = { /* Logic later */ })
            }
        }
    }
}

@Composable
fun DataRow(label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = label,
            fontSize = 24.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = value,
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.Black
        )
    }
}

@Composable
fun MapActionButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .width(160.dp)
            .height(50.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF006699)), // Blue color from reference
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = text,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
    }
}