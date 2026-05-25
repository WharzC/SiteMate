package com.wharzc.motoguard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

data class Trip(
    val date: String,
    val startLocation: String,
    val endLocation: String,
    val distance: String,
    val time: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripHistoryScreen(navController: NavController) {
    // Sample data remains the same
    val trips = listOf(
        Trip("March 6", "Roxas", "Calapan", "15 km", "25 mins"),
        Trip("March 5", "Calapan", "Roxas", "28 km", "40 mins")
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar( // Use CenterAligned for better symmetry
                title = {
                    Text(
                        "Trip History",
                        color = Color(0xFF005691), // Matching your branding
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp), // Standard padding for all screens
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(trips) { trip ->
                TripItem(trip)
                HorizontalDivider(
                    modifier = Modifier.padding(top = 8.dp),
                    thickness = 0.5.dp,
                    color = Color.LightGray
                )
            }
        }
    }
}

@Composable
fun TripItem(trip: Trip) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
            // Date Header
            Text(
                text = trip.date,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Main Route Section - Dynamic Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween // Forces items to edges
            ) {
                Text(
                    text = trip.startLocation,
                    modifier = Modifier.weight(1f), // Takes up available space
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1 // Prevents overlapping on small screens
                )

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.padding(horizontal = 12.dp).size(24.dp),
                    tint = Color.Gray
                )

                Text(
                    text = trip.endLocation,
                    modifier = Modifier.weight(1f), // Takes up equal space as startLocation
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Stats Section - Spaced out to cover horizontal width
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Distance", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                    Text(trip.distance, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Travel Time", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                    Text(trip.time, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}