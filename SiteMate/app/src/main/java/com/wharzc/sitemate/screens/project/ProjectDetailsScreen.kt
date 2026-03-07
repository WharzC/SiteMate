package com.wharzc.sitemate.screens.project

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.wharzc.sitemate.model.ProjectCreationViewModel
import com.wharzc.sitemate.screens.components.ProjectWizardHeader
import com.wharzc.sitemate.ui.theme.SiteMateBlue
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ProjectDetailsScreen(
    viewModel: ProjectCreationViewModel, // NEW PARAMETER
    onProceed: () -> Unit,
    onBack: () -> Unit
) {
    var showConfirmDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ProjectWizardHeader(title = "Project Details", onBackClick = onBack)

        Spacer(modifier = Modifier.height(32.dp))

        // Manpower (Using ViewModel)
        OutlinedTextField(
            value = viewModel.manpower,
            onValueChange = { viewModel.manpower = it },
            label = { Text("Manpower (Headcount)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Start Date (Using ViewModel)
        DateInputField(
            label = "Start Date",
            value = viewModel.startDate,
            onValueChange = { viewModel.startDate = it }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // End Date (Using ViewModel)
        DateInputField(
            label = "Expected Completion",
            value = viewModel.endDate,
            onValueChange = { viewModel.endDate = it }
        )

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = { showConfirmDialog = true },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SiteMateBlue)
        ) {
            Text(text = "Proceed to Location")
        }
    }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Confirm Details") },
            text = { Text("Are these dates and manpower details correct?") },
            confirmButton = { TextButton(onClick = { showConfirmDialog = false; onProceed() }) { Text("Yes") } },
            dismissButton = { TextButton(onClick = { showConfirmDialog = false }) { Text("Cancel") } }
        )
    }
}

// (Keep DateInputField helper here)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateInputField(label: String, value: String, onValueChange: (String) -> Unit) {
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = { TextButton(onClick = { showDatePicker = false; datePickerState.selectedDateMillis?.let { millis -> val formatter = SimpleDateFormat("MM / dd / yyyy", Locale.US); onValueChange(formatter.format(Date(millis))) } }) { Text("OK") } },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } }
        ) { DatePicker(state = datePickerState) }
    }
    OutlinedTextField(
        value = value, onValueChange = onValueChange, label = { Text(label) }, placeholder = { Text("MM / DD / YYYY") },
        modifier = Modifier.fillMaxWidth(), singleLine = true,
        trailingIcon = { IconButton(onClick = { showDatePicker = true }) { Icon(Icons.Filled.CalendarToday, contentDescription = "Select Date") } }
    )
}