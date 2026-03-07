package com.wharzc.sitemate.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Domain
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.wharzc.sitemate.ui.theme.SiteMateBlue
import com.wharzc.sitemate.screens.project.ProjectsScreen
import com.wharzc.sitemate.screens.project.ProfileScreen
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.wharzc.sitemate.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EngineerDashboardScreen(navController: NavController) {
    // Controls which "tab" is currently visible. Defaults to "projects".
    var currentScreen by remember { mutableStateOf("projects") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // NOTE: I am using a placeholder building icon here.
                        // To use your actual logo, swap this Icon for an Image composable using painterResource.
                        Image(
                            painter = painterResource(id = R.drawable.sitemate_logo),
                            contentDescription = "SiteMate Logo",
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "SITEMATE",
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 2.sp,
                            fontSize = 20.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SiteMateBlue),
                navigationIcon = {
                    // Show a back button ONLY when we are looking at the Profile screen
                    if (currentScreen == "profile") {
                        IconButton(onClick = { currentScreen = "projects" }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back to Projects", tint = Color.White)
                        }
                    }
                },
                actions = {
                    // Show the Profile icon ONLY when we are looking at the Projects screen
                    if (currentScreen == "projects") {
                        IconButton(onClick = { currentScreen = "profile" }) {
                            Icon(Icons.Filled.Person, contentDescription = "Profile", tint = Color.White)
                        }
                    }
                }
            )
        }
        // Notice: The bottomBar parameter is completely removed!
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            when (currentScreen) {
                "projects" -> ProjectsScreen(navController)
                "profile" -> ProfileScreen(navController = navController)
            }
        }
    }
}