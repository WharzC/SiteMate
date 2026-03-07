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

    // --- FIXED: Scaffold completely removed! Just a clean, simple Column. ---
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .padding(top = 24.dp) // Only one single, fixed gap at the top!
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

        // Project List
        if (viewModel.projects.isEmpty()) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text("No projects found. Tap '+' to create one.", color = Color.Gray)
            }
        } else {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(viewModel.projects) { item ->
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

        // --- NEW ULTRA-SLIM FOOTER INSTALLED HERE ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp, top = 2.dp),
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
                        isDeleteMode = false // Auto exit delete mode
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