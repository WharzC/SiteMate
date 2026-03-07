package com.wharzc.sitemate.screens.project

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.wharzc.sitemate.model.ProjectCreationViewModel
import com.wharzc.sitemate.screens.components.ProjectWizardHeader
import com.wharzc.sitemate.ui.theme.SiteMateBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateProjectScreen(
    viewModel: ProjectCreationViewModel, // NEW: Shared Brain
    onProceed: () -> Unit,
    onBack: () -> Unit
) {
    var showConfirmDialog by remember { mutableStateOf(false) }

    // --- Data Definitions ---
    val constructionTypes = listOf("Vertical Construction", "Horizontal Construction")
    val verticalCategories = listOf("Residential", "Commercial", "Industrial")
    val horizontalCategories = listOf("Transportation")

    // Determine current categories based on ViewModel's selected Type
    val currentCategories = if (viewModel.selectedType == "Vertical Construction") verticalCategories else horizontalCategories

    // Determine sub-categories based on ViewModel's selected Category
    val currentSubCategories = when (viewModel.selectedCategory) {
        "Residential" -> listOf("Single-Family", "Multi-Family", "High-Rise")
        "Commercial" -> listOf("Office", "Retail", "Institutional")
        "Industrial" -> listOf("Manufacturing", "Storage")
        "Transportation" -> listOf("Roadways")
        else -> emptyList()
    }

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
            title = "Project Classification",
            onBackClick = onBack
        )

        Spacer(modifier = Modifier.height(32.dp))

        // 2. FORM CONTENT
        // Type Dropdown
        DropdownField(
            label = "Construction Type",
            options = constructionTypes,
            selectedOption = viewModel.selectedType,
            onOptionSelected = { type ->
                viewModel.selectedType = type
                // Reset lower fields when top field changes
                viewModel.selectedCategory = ""
                viewModel.selectedSubCategory = ""
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Category Dropdown
        if (viewModel.selectedType.isNotEmpty()) {
            DropdownField(
                label = "Category",
                options = currentCategories,
                selectedOption = viewModel.selectedCategory,
                onOptionSelected = { category ->
                    viewModel.selectedCategory = category
                    // Reset lower field
                    viewModel.selectedSubCategory = ""
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Sub-Category Dropdown
        if (viewModel.selectedCategory.isNotEmpty()) {
            DropdownField(
                label = "Sub-Category",
                options = currentSubCategories,
                selectedOption = viewModel.selectedSubCategory,
                onOptionSelected = { sub ->
                    viewModel.selectedSubCategory = sub
                }
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Proceed Button
        if (viewModel.selectedSubCategory.isNotEmpty()) {
            Button(
                onClick = { showConfirmDialog = true },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SiteMateBlue)
            ) {
                Text(text = "Proceed")
            }
        }
    }

    // --- DIALOG ---
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Confirm Step") },
            text = { Text("Are you sure you want to proceed with this classification?") },
            confirmButton = {
                TextButton(onClick = {
                    showConfirmDialog = false
                    onProceed()
                }) { Text("Yes, Proceed") }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) { Text("Cancel") }
            }
        )
    }
}

// (Keep your DropdownField helper at the bottom of the file)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownField(label: String, options: List<String>, selectedOption: String, onOptionSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }, modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = selectedOption, onValueChange = {}, readOnly = true, label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            modifier = Modifier.fillMaxWidth().menuAnchor()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { selectionOption ->
                DropdownMenuItem(text = { Text(selectionOption) }, onClick = { onOptionSelected(selectionOption); expanded = false })
            }
        }
    }
}