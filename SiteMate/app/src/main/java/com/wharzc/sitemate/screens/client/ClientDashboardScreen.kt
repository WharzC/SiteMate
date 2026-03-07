package com.wharzc.sitemate.screens.client

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
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
import com.wharzc.sitemate.model.ClientProjectsViewModel
import com.wharzc.sitemate.screens.components.RoleAssignmentsTab
import com.wharzc.sitemate.screens.project.ProfileScreen
import com.wharzc.sitemate.ui.theme.SiteMateBlue

@Composable
fun ClientDashboardScreen(navController: NavController) {
// 0 = Projects, 1 = Assignments, 2 = Profile
    var selectedItem by remember { mutableIntStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = SiteMateBlue) {
                NavigationBarItem(
                    icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Projects") },
                    label = { Text("Projects") },
                    selected = selectedItem == 0,
                    onClick = { selectedItem = 0 },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = SiteMateBlue, indicatorColor = Color.White, unselectedIconColor = Color.White.copy(alpha = 0.6f), unselectedTextColor = Color.White.copy(alpha = 0.6f))
                )
                NavigationBarItem(
                    // Using Notifications icon for the request inbox
                    icon = { Icon(Icons.Filled.Notifications, contentDescription = "Assignments") },
                    label = { Text("Assignments") },
                    selected = selectedItem == 1,
                    onClick = { selectedItem = 1 },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = SiteMateBlue, indicatorColor = Color.White, unselectedIconColor = Color.White.copy(alpha = 0.6f), unselectedTextColor = Color.White.copy(alpha = 0.6f))
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Person, contentDescription = "Profile") },
                    label = { Text("Profile") },
                    selected = selectedItem == 2,
                    onClick = { selectedItem = 2 },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = SiteMateBlue, indicatorColor = Color.White, unselectedIconColor = Color.White.copy(alpha = 0.6f), unselectedTextColor = Color.White.copy(alpha = 0.6f))
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedItem) {
                0 -> ClientProjectsList(navController)
                1 -> RoleAssignmentsTab(role = "Client") // <--- NEW TAB!
                2 -> ProfileScreen(navController) // Reuses your awesome profile tab!
            }
        }
    }
}

@Composable
fun ClientProjectsList(
    navController: NavController,
    viewModel: ClientProjectsViewModel = viewModel()
) {
    LaunchedEffect(Unit) { viewModel.fetchClientProjects() }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("My Projects", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = SiteMateBlue, modifier = Modifier.padding(vertical = 16.dp))

        if (viewModel.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = SiteMateBlue) }
        } else if (viewModel.assignedProjects.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Your engineer has not assigned you to any projects yet.", color = Color.Gray)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(viewModel.assignedProjects) { project ->
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable {
                            // DIFFERENT ROUTE: We will build a Read-Only dashboard for this!
                            navController.navigate("client_project/${project.id}")
                        },
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Business, null, tint = SiteMateBlue, modifier = Modifier.size(40.dp))
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(project.name, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = SiteMateBlue)
                                Text(project.location, fontSize = 14.sp, color = Color.Gray)
                            }
                        }
                    }
                }
            }
        }
    }
}