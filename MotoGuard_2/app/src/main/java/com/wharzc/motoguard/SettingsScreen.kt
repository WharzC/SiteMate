package com.wharzc.motoguard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage

data class SettingItem(val title: String, val iconUrl: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    val settingsList = listOf(
        SettingItem("Profile", "https://img.icons8.com/fluency/96/user-male-circle.png"),
        SettingItem("My Motorcycle", "https://img.icons8.com/fluency/96/motorcycle.png"),
        SettingItem("Geo-Fence Settings", "https://img.icons8.com/fluency/96/map-marker.png"),
        SettingItem("Notifications", "https://img.icons8.com/fluency/96/filled-message.png"),
        SettingItem("Security", "https://img.icons8.com/fluency/96/shield.png"),
        SettingItem("Logout", "https://img.icons8.com/fluency/96/exit.png")
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Settings", color = Color(0xFF005691), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            items(settingsList) { item ->
                SettingsRow(item) {
                    if (item.title == "Logout") {
                        navController.navigate("login") { popUpTo("dashboard") { inclusive = true } }
                    }
                }
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp)
            }
        }
    }
}

@Composable
fun SettingsRow(item: SettingItem, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = item.iconUrl,
            contentDescription = null,
            modifier = Modifier
                .size(32.dp)
                .padding(end = 4.dp),
            // This ensures the icon scales properly inside the 32dp box
            contentScale = ContentScale.Fit
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = item.title,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = Color.DarkGray
        )
    }
}