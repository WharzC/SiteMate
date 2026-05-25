package com.wharzc.sitemate.screens.project

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wharzc.sitemate.model.ProjectCreationViewModel
import com.wharzc.sitemate.screens.components.ProjectWizardHeader
import com.wharzc.sitemate.ui.theme.SiteMateBlue
import com.wharzc.sitemate.ui.theme.SiteMateOrange

@Composable
fun ProjectReviewScreen(
    viewModel: ProjectCreationViewModel,
    onConfirmSave: () -> Unit, // This should trigger the navigation back to the list in your NavHost
    onBack: () -> Unit
) {
    // --- State ---
    var isSaving by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // 1. HEADER
        ProjectWizardHeader(
            title = "Review & Confirm",
            onBackClick = onBack
        )

        Text(
            text = "Please review the project details before creating.",
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 24.dp)
        )


        // 2. SUMMARY CARDS
        ReviewSection("Classification") {
            ReviewRow("Type", viewModel.selectedType)
            ReviewRow("Category", viewModel.selectedCategory)
            ReviewRow("Sub-Category", viewModel.selectedSubCategory)
        }

        Spacer(modifier = Modifier.height(16.dp))

        ReviewSection("Project Details") {
            ReviewRow("Name", viewModel.projectName)
            ReviewRow("Manpower", "${viewModel.manpower} Heads")
            ReviewRow("Timeline", "${viewModel.startDate} - ${viewModel.endDate}")
        }

        Spacer(modifier = Modifier.height(16.dp))

        ReviewSection("Location") {
            // Cleanly format the new address variables
            val address = if (viewModel.selectedCity.isNotEmpty()) {
                listOf(viewModel.streetAddress, viewModel.selectedBarangay, viewModel.selectedCity)
                    .filter { it.isNotBlank() }
                    .joinToString(", ")
            } else {
                "Not Set"
            }

            ReviewRow("Address", address)

            // Only show the landmark row if they actually typed one in
            if (viewModel.landmark.isNotBlank()) {
                ReviewRow("Landmark", viewModel.landmark)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

       /* ReviewSection("Assets") {
            val photoCount = viewModel.sitePhotos.size
            val docCount = viewModel.documentPhotos.size
            ReviewRow("Attachments", "$photoCount Site Photos, $docCount Documents")
        } */

        Spacer(modifier = Modifier.height(48.dp))

        // Error message display if Firebase fails
        if (errorMessage != null) {
            Text(
                text = errorMessage!!,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // 3. CONFIRM BUTTON
        Button(
            onClick = {
                isSaving = true // Show loading spinner
                errorMessage = null // Clear old errors

                // Trigger Firebase save
                viewModel.saveProjectToFirestore(
                    onSuccess = {
                        isSaving = false
                        showSuccessDialog = true // Pop the success dialog!
                    },
                    onError = { exception ->
                        isSaving = false
                        errorMessage = exception.message ?: "Failed to connect to server."
                        println("Error saving to Firebase: ${exception.message}")
                    }
                )
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SiteMateBlue),
            enabled = !isSaving // Prevent spam-clicking while saving
        ) {
            if (isSaving) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text("Confirm & Save Project")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    // --- SUCCESS DIALOG ---
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { /* Prevent dismissal by tapping outside */ },
            icon = {
                Icon(Icons.Filled.CheckCircle, null, tint = SiteMateBlue, modifier = Modifier.size(48.dp))
            },
            title = { Text("Success!") },
            text = { Text("Project successfully created and added to your dashboard.") },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        onConfirmSave() // Triggers NavHost to go back to projects screen
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SiteMateBlue)
                ) {
                    Text("Go to Dashboard")
                }
            }
        )
    }
}

// --- HELPER COMPONENTS ---
@Composable
fun ReviewSection(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = SiteMateOrange, modifier = Modifier.padding(bottom = 8.dp))
            content()
        }
    }
}

@Composable
fun ReviewRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = Color.Gray, fontSize = 14.sp)
        Text(text = value, color = SiteMateBlue, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
    }
}