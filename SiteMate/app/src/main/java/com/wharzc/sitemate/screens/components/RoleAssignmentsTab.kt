package com.wharzc.sitemate.screens.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wharzc.sitemate.model.AssignmentsViewModel
import com.wharzc.sitemate.model.PendingProject
import com.wharzc.sitemate.ui.theme.SiteMateBlue
import com.wharzc.sitemate.ui.theme.SiteMateOrange

@Composable
fun RoleAssignmentsTab(
    role: String, // Pass "Foreman" or "Client"
    viewModel: AssignmentsViewModel = viewModel()
) {
    LaunchedEffect(Unit) {
        viewModel.fetchPendingRequests(role)
    }

    var projectToConfirm by remember { mutableStateOf<PendingProject?>(null) }
    var confirmationType by remember { mutableStateOf("") } // "Accept" or "Delete"

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Pending Invitations",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = SiteMateBlue,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        if (viewModel.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = SiteMateOrange)
            }
        } else if (viewModel.pendingRequests.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("You have no pending project assignments.", color = Color.Gray)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(viewModel.pendingRequests) { project ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Filled.Business, contentDescription = null, tint = SiteMateBlue, modifier = Modifier.size(32.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(project.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = SiteMateBlue)
                                    Text(project.location, fontSize = 12.sp, color = Color.Gray)
                                    Text("Engr. ${project.engineerName}", fontSize = 12.sp, color = SiteMateOrange, fontWeight = FontWeight.Medium)
                                }
                            }

                            // Action Buttons
                            Row {
                                IconButton(onClick = {
                                    projectToConfirm = project
                                    confirmationType = "Delete"
                                }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Reject", tint = Color.Red.copy(alpha = 0.8f))
                                }
                                IconButton(onClick = {
                                    projectToConfirm = project
                                    confirmationType = "Accept"
                                }) {
                                    Icon(Icons.Filled.CheckCircle, contentDescription = "Accept", tint = Color(0xFF4CAF50)) // Green
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // --- WARNING DIALOGS ---
    if (projectToConfirm != null) {
        AlertDialog(
            onDismissRequest = { projectToConfirm = null },
            title = {
                Text(if (confirmationType == "Accept") "Accept Assignment?" else "Reject Assignment?",
                    color = SiteMateBlue, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    if (confirmationType == "Accept")
                        "Once accepted, '${projectToConfirm!!.name}' will appear in your Projects tab."
                    else
                        "Are you sure you want to reject this invitation? This action cannot be undone."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.handleRequest(
                            project = projectToConfirm!!,
                            role = role,
                            isAccepted = confirmationType == "Accept"
                        ) {
                            projectToConfirm = null // Close dialog
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (confirmationType == "Accept") Color(0xFF4CAF50) else Color.Red
                    )
                ) {
                    Text(if (confirmationType == "Accept") "Yes, Accept" else "Yes, Reject", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { projectToConfirm = null }) { Text("Cancel", color = Color.Gray) }
            }
        )
    }
}