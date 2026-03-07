package com.wharzc.sitemate.screens.project

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.wharzc.sitemate.model.ProjectCreationViewModel
import com.wharzc.sitemate.screens.components.ProjectWizardHeader
import com.wharzc.sitemate.ui.theme.SiteMateBlue

@Composable
fun ProjectNameScreen(
    viewModel: ProjectCreationViewModel, // Receives the Shared Brain
    onProceed: () -> Unit,
    onBack: () -> Unit
) {
    var showConfirmDialog by remember { mutableStateOf(false) }

    // --- MAIN LAYOUT (Single Centered Block) ---
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. HEADER
        ProjectWizardHeader(
            title = "New Project",
            onBackClick = onBack
        )

        Spacer(modifier = Modifier.height(32.dp))

        // 2. INPUT FIELDS
        // Project Name
        OutlinedTextField(
            value = viewModel.projectName, // READ from ViewModel
            onValueChange = { viewModel.projectName = it }, // WRITE to ViewModel
            label = { Text("Project Name") },
            placeholder = { Text("e.g., Grand Residences Tower 1") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Description (Multi-line)
        OutlinedTextField(
            value = viewModel.projectDesc, // READ from ViewModel
            onValueChange = { viewModel.projectDesc = it }, // WRITE to ViewModel
            label = { Text("Description") },
            placeholder = { Text("Brief details about the project...") },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp), // Taller box for description
            maxLines = 5,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences
            )
        )

        Spacer(modifier = Modifier.height(48.dp))

        // 3. PROCEED BUTTON
        // Only enabled if name is not empty
        if (viewModel.projectName.isNotEmpty()) {
            Button(
                onClick = { showConfirmDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SiteMateBlue)
            ) {
                Text("Proceed to Classification")
            }
        }
    }

    // --- CONFIRMATION DIALOG ---
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Confirm Name") },
            text = { Text("Proceed with '${viewModel.projectName}'?") },
            confirmButton = {
                TextButton(onClick = {
                    showConfirmDialog = false
                    onProceed()
                }) { Text("Yes") }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) { Text("Cancel") }
            }
        )
    }
}