package com.wharzc.sitemate.screens.project

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background // Added for footer
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.wharzc.sitemate.model.ProjectsViewModel
import com.wharzc.sitemate.ui.theme.SiteMateBlue
import com.wharzc.sitemate.ui.theme.SiteMateOrange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectsScreen(
    navController: NavController,
    viewModel: ProjectsViewModel = viewModel()
) {
    var isDeleteMode by remember { mutableStateOf(false) }
    var projectToDeleteId by remember { mutableStateOf<String?>(null) }
    var projectToDeleteTitle by remember { mutableStateOf("") }

    // --- SORT STATE ---
    var sortOption by remember { mutableStateOf("Name") }
    var showSortMenu by remember { mutableStateOf(false) }

    // Sort the viewModel's filtered list dynamically
    val finalSortedProjects = when (sortOption) {
        "Date" -> viewModel.filteredProjects.sortedBy { it.id } // Replace it.id with it.timestamp if your Project data class has it
        else -> viewModel.filteredProjects.sortedBy { it.name?.lowercase() }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .padding(top = 24.dp)
    ) {

        // Create Project Button
        Button(
            onClick = { navController.navigate("project_name") },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SiteMateBlue),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Create New Project", color = Color.White, fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Delete Toggle Button
        OutlinedButton(
            onClick = { isDeleteMode = !isDeleteMode },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, if (isDeleteMode) Color.Red else SiteMateBlue),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = if (isDeleteMode) Color.Red else SiteMateBlue
            )
        ) {
            Icon(if (isDeleteMode) Icons.Default.Clear else Icons.Default.Delete, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                if (isDeleteMode) "Cancel Deletion" else "Delete Existing Project",
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Existing Projects",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = SiteMateBlue
        )

        Spacer(modifier = Modifier.height(16.dp))

        // --- SEARCH BAR & SORT DROPDOWN ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = viewModel.searchQuery,
                onValueChange = { viewModel.searchQuery = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Search by project name...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = SiteMateOrange) },
                trailingIcon = {
                    if (viewModel.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SiteMateOrange,
                    unfocusedBorderColor = Color.LightGray
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Sort Dropdown Button
            Box {
                IconButton(
                    onClick = { showSortMenu = true },
                    modifier = Modifier
                        .size(56.dp)
                        .background(SiteMateBlue.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                ) {
                    Icon(Icons.Default.FilterList, contentDescription = "Sort", tint = SiteMateBlue)
                }

                DropdownMenu(
                    expanded = showSortMenu,
                    onDismissRequest = { showSortMenu = false },
                    modifier = Modifier.background(Color.White)
                ) {
                    DropdownMenuItem(
                        text = { Text("Sort by Name", fontWeight = if (sortOption == "Name") FontWeight.Bold else FontWeight.Normal) },
                        onClick = { sortOption = "Name"; showSortMenu = false }
                    )
                    DropdownMenuItem(
                        text = { Text("Sort by Date Created", fontWeight = if (sortOption == "Date") FontWeight.Bold else FontWeight.Normal) },
                        onClick = { sortOption = "Date"; showSortMenu = false }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Project List (Now using the sorted and filtered list!)
        if (viewModel.projects.isEmpty()) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text("No projects found. Tap '+' to create one.", color = Color.Gray)
            }
        } else if (finalSortedProjects.isEmpty()) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text("No matching projects found.", color = Color.Gray)
            }
        } else {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(finalSortedProjects) { item ->
                    ProjectCardItem(
                        title = item.name ?: "Untitled",
                        subtitle = item.location ?: "Unknown",
                        isDeleteMode = isDeleteMode,
                        onClick = {
                            if (!isDeleteMode) {
                                navController.navigate("project_dashboard/${item.id}")
                            }
                        },
                        onDeleteClick = {
                            projectToDeleteId = item.id
                            projectToDeleteTitle = item.name ?: "Project"
                        }
                    )
                }
            }
        }

// --- ULTRA-SLIM FOOTER ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Transparent)
                // Removed navigationBarsPadding() and locked the bottom padding to exactly 12.dp!
                .padding(bottom = 12.dp, top = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "SITEMATE @ 2026",
                fontSize = 8.sp,
                color = Color.LightGray,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }
    }

    // Delete Confirmation Dialog
    if (projectToDeleteId != null) {
        AlertDialog(
            onDismissRequest = { projectToDeleteId = null },
            title = { Text("Delete Project", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete '$projectToDeleteTitle'?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteProject(projectToDeleteId!!)
                        projectToDeleteId = null
                        isDeleteMode = false
                    }
                ) {
                    Text("Delete", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { projectToDeleteId = null }) {
                    Text("Cancel", color = Color.Gray)
                }
            },
            containerColor = Color.White
        )
    }
}

@Composable
fun ProjectCardItem(
    title: String,
    subtitle: String,
    isDeleteMode: Boolean,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Business,
                contentDescription = null,
                tint = SiteMateOrange,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = SiteMateBlue
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
            if (isDeleteMode) {
                IconButton(onClick = onDeleteClick) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                }
            }
        }
    }
}