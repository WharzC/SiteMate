package com.wharzc.motoguard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.KeyboardDoubleArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material.icons.filled.Add
import kotlinx.coroutines.launch

import androidx.compose.material3.BottomAppBar
import androidx.compose.runtime.rememberCoroutineScope


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripHistoryScreen(
    tripViewModel: TripViewModel,
    onNavigateBack: () -> Unit
) {
    val primaryDarkBlue = Color(0xFF10213E)
    val backgroundColor = Color(0xFFF8F9FB)
    val actionRed = Color(0xFFD32F2F)

    val tripList by tripViewModel.tripList.collectAsState()
    var showDeleteWarningDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Trip History", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    if (tripList.isNotEmpty()) {
                        IconButton(onClick = { showDeleteWarningDialog = true }) {
                            Icon(imageVector = Icons.Filled.DeleteSweep, contentDescription = "Clear All", tint = Color.White)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = primaryDarkBlue)
            )
        },
        bottomBar = {
            BottomAppBar(
                containerColor = Color.White,
                modifier = Modifier.height(72.dp)
            ) {
                Button(
                    onClick = {
                        val success = tripViewModel.manualSaveTrip()
                        scope.launch {
                            if (success) {
                                snackbarHostState.showSnackbar("Current location logged successfully.")
                            } else {
                                snackbarHostState.showSnackbar("You can only log once per minute. Please wait.")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryDarkBlue)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Log your Current Location", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = backgroundColor
    ) { innerPadding ->
        if (tripList.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No recorded trips available.\nLogs populate automatically when movement is detected.\n\nYou can also tap the button below to manually save your current location.",
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(24.dp)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(tripList, key = { it.id }) { trip -> TripItemCard(trip = trip) }
            }
        }

        // Delete confirmation dialog remains unchanged
        if (showDeleteWarningDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteWarningDialog = false },
                title = { Text("Delete Trip History?", fontWeight = FontWeight.Bold, color = primaryDarkBlue) },
                text = { Text("Warning: You are about to clear all recorded tracking logs. This action is irreversible and cannot be undone.", color = Color.DarkGray) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            tripViewModel.clearAllHistory()
                            showDeleteWarningDialog = false
                        }
                    ) {
                        Text("Delete All", color = actionRed, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteWarningDialog = false }) {
                        Text("Cancel", color = primaryDarkBlue)
                    }
                },
                containerColor = Color.White
            )
        }
    }
}

@Composable
fun TripItemCard(trip: MotorcycleTripLog) { // Updated Type Here
    val primaryDarkBlue = Color(0xFF10213E)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(18.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = trip.date, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = primaryDarkBlue)
                Text(text = trip.time, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.Gray)
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "FROM", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Text(text = trip.previousLocation, fontSize = 13.sp, color = Color.Black)
                }
                Icon(imageVector = Icons.Filled.KeyboardDoubleArrowRight, contentDescription = null, tint = primaryDarkBlue, modifier = Modifier.padding(horizontal = 4.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "TO (EXACT)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Text(text = trip.currentExactLocation, fontSize = 13.sp, color = Color.Black)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row {
                    Text(text = "Coordinates: ", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = primaryDarkBlue)
                    Text(text = trip.coordinates, fontSize = 13.sp, color = Color.DarkGray)
                }
                Row {
                    Text(text = "Distance traveled: ", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = primaryDarkBlue)
                    Text(text = trip.distance, fontSize = 13.sp, color = Color.DarkGray)
                }
                Row {
                    Text(text = "Time since last log: ", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = primaryDarkBlue)
                    Text(text = trip.timeInterval, fontSize = 13.sp, color = Color.DarkGray)
                }
            }
        }
    }
}