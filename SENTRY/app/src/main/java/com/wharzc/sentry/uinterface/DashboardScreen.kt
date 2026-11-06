package com.wharzc.sentry.uinterface

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.wharzc.sentry.viewmodel.SentryViewModel

@Composable
fun DashboardScreen(
    viewModel: SentryViewModel,
    onNavigateToRfid: () -> Unit,
    onNavigateToFingerprint: () -> Unit,
    onNavigateToAttendance: () -> Unit
) {
    val currentMode by viewModel.currentMode.collectAsState()
    val isBypassing by viewModel.isBypassing.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("System Controls", style = MaterialTheme.typography.headlineMedium)

        // Status Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (currentMode == "ATTENDANCE") Color(0xFFE8F5E9) else Color(0xFFFFF3E0)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Current Active Mode", style = MaterialTheme.typography.labelLarge)
                Text(
                    text = currentMode,
                    style = MaterialTheme.typography.headlineSmall,
                    color = if (currentMode == "ATTENDANCE") Color(0xFF2E7D32) else Color(0xFFE65100)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { viewModel.toggleSystemMode() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("SWITCH MODE (${if (currentMode == "ATTENDANCE") "Go to ENROLL" else "Go to ATTENDANCE"})")
                }
            }
        }

        // Gate Override Card
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Manual Override", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { viewModel.triggerBypass() },
                    enabled = !isBypassing,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (isBypassing) "Opening Gate..." else "BYPASS GATE / DOOR")
                }
            }
        }

        Text("Directories & Logs", style = MaterialTheme.typography.titleLarge)

        OutlinedButton(
            onClick = onNavigateToRfid,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("RFID Directory (Cards)")
        }

        OutlinedButton(
            onClick = onNavigateToFingerprint,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Fingerprint Directory (Biometrics)")
        }

        OutlinedButton(
            onClick = onNavigateToAttendance,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Attendance History (By Date)")
        }
    }
}