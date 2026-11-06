package com.wharzc.sentry.uinterface

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.wharzc.sentry.viewmodel.SentryViewModel

@Composable
fun AttendanceScreen(viewModel: SentryViewModel) {
    val attendanceByDate by viewModel.attendanceByDate.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Attendance History", style = MaterialTheme.typography.headlineMedium)
        Text("Categorized by date", style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(12.dp))

        if (attendanceByDate.isEmpty()) {
            Text("No attendance logs found.")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                attendanceByDate.forEach { (date, records) ->
                    item {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(
                                text = "Date: $date (${records.size} scans)",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                    items(records) { record ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(record.name, style = MaterialTheme.typography.bodyLarge)
                                    Text("UID: ${record.studentUid}", style = MaterialTheme.typography.bodySmall)
                                    Text("Time: ${record.timeFormatted}", style = MaterialTheme.typography.bodySmall)
                                }
                                Text(
                                    text = record.status,
                                    color = if (record.status.equals("PRESENT", true)) Color(0xFF2E7D32) else Color(0xFFC62828),
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}