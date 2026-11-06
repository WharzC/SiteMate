package com.wharzc.sentry.uinterface

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.wharzc.sentry.viewmodel.FingerprintUser
import com.wharzc.sentry.viewmodel.SentryViewModel

@Composable
fun FingerprintScreen(viewModel: SentryViewModel) {
    val fingerprints by viewModel.fingerprintUsers.collectAsState()
    var editingUser by remember { mutableStateOf<FingerprintUser?>(null) }
    var editNameInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Fingerprint Directory", style = MaterialTheme.typography.headlineMedium)
        Text("Registered prints in sensor memory", style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(12.dp))

        if (fingerprints.isEmpty()) {
            Text("No registered fingerprint entries found.")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(fingerprints) { user ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(user.name, style = MaterialTheme.typography.titleMedium)
                                Text("Sensor ID #${user.hardwareId}", style = MaterialTheme.typography.bodySmall)
                                Text("Finger: ${user.fingerName} | UID: ${user.uid}", style = MaterialTheme.typography.bodySmall)
                            }
                            Row {
                                IconButton(onClick = {
                                    editingUser = user
                                    editNameInput = user.name
                                }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit Name")
                                }
                                IconButton(onClick = { viewModel.deleteFingerprintUser(user.hardwareId) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Edit Name Dialog
    editingUser?.let { user ->
        AlertDialog(
            onDismissRequest = { editingUser = null },
            title = { Text("Edit Student Name") },
            text = {
                OutlinedTextField(
                    value = editNameInput,
                    onValueChange = { editNameInput = it },
                    label = { Text("Name") }
                )
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.updateFingerprintName(user.hardwareId, editNameInput.trim())
                    editingUser = null
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingUser = null }) { Text("Cancel") }
            }
        )
    }
}