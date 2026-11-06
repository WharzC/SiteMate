package com.wharzc.prime.farmer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wharzc.prime.viewmodel.HistoryViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(viewModel: HistoryViewModel) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Monitoring, 1 = Fertilizer
    var showLogActionModal by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    val monitoringLogs by viewModel.monitoringLogs.collectAsState()
    val fertilizerLogs by viewModel.fertilizerLogs.collectAsState()

    val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.US)
    var selectedDate by remember { mutableStateOf(dateFormat.format(Calendar.getInstance().time)) }
    val datePickerState = rememberDatePickerState()

    val bgColor = Color(0xFF195532)
    val cardColor = Color(0xFF2A5339)
    val neonGreen = Color(0xFF00FFCC)

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val newDate = dateFormat.format(java.util.Date(millis))
                        selectedDate = newDate
                        viewModel.loadDataForDate(newDate) // Trigger DB refresh for new date
                    }
                    showDatePicker = false
                }) { Text("OK", color = neonGreen) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel", color = Color.Gray) }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showLogActionModal) {
        FertilizerLogModal(
            cardColor = cardColor,
            neonGreen = neonGreen,
            onDismiss = { showLogActionModal = false },
            onSave = { time, amPm, date, qty, reason ->
                viewModel.logFertilizerAction(time, amPm, date, qty, reason)
                showLogActionModal = false
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize().background(bgColor)) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            // Tab Selection
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { selectedTab = 0 },
                    colors = ButtonDefaults.buttonColors(containerColor = if (selectedTab == 0) neonGreen else cardColor),
                    modifier = Modifier.weight(1f)
                ) { Text("Monitoring", color = if (selectedTab == 0) bgColor else Color.White, fontWeight = FontWeight.Bold) }
                Button(
                    onClick = { selectedTab = 1 },
                    colors = ButtonDefaults.buttonColors(containerColor = if (selectedTab == 1) neonGreen else cardColor),
                    modifier = Modifier.weight(1f)
                ) { Text("Fertilizer", color = if (selectedTab == 1) bgColor else Color.White, fontWeight = FontWeight.Bold) }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Date Picker Trigger
            Card(
                onClick = { showDatePicker = true },
                colors = CardDefaults.cardColors(containerColor = cardColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Date: $selectedDate", color = Color.White, fontWeight = FontWeight.Bold)
                    Icon(Icons.Filled.CalendarToday, contentDescription = "Pick Date", tint = neonGreen)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (selectedTab == 0) {
                if (monitoringLogs.isEmpty()) {
                    Text("No monitoring data recorded for this date yet.", color = Color.Gray)
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(bottom = 80.dp)) {
                        items(monitoringLogs) { log ->
                            Card(colors = CardDefaults.cardColors(containerColor = cardColor), modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(log.time, color = neonGreen, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("NPK: ${log.avgN.toInt()}/${log.avgP.toInt()}/${log.avgK.toInt()} mg/kg", color = Color.White)
                                        Text("Temp: ${String.format("%.1f", log.avgTemp)}°C", color = Color.White)
                                    }
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("pH: ${String.format("%.1f", log.avgPh)}", color = Color.White)
                                        Text("Turb: ${String.format("%.1f", log.avgTurbidity)} NTU", color = Color.White)
                                    }
                                    Text("Water Dist: ${String.format("%.1f", log.avgWaterLvl)} cm", color = Color.LightGray, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            } else {
                if (fertilizerLogs.isEmpty()) {
                    Text("No fertilizer actions logged for this date.", color = Color.Gray)
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(bottom = 80.dp)) {
                        items(fertilizerLogs) { log ->
                            Card(colors = CardDefaults.cardColors(containerColor = cardColor), modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("${log.time} ${log.amPm}", color = neonGreen, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    if (log.quantity.isNotBlank()) Text("Qty: ${log.quantity}", color = Color.White)
                                    if (log.reason.isNotBlank()) Text("Reason: ${log.reason}", color = Color.LightGray, fontSize = 14.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        if (selectedTab == 1) {
            FloatingActionButton(
                onClick = { showLogActionModal = true },
                containerColor = neonGreen,
                modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp).padding(bottom = 60.dp)
            ) { Icon(Icons.Filled.Add, contentDescription = "Log Action", tint = bgColor) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FertilizerLogModal(
    cardColor: Color,
    neonGreen: Color,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, String) -> Unit
) {
    var time by remember { mutableStateOf("") }
    var amPm by remember { mutableStateOf("AM") }
    var exactDate by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("") }

    var timeError by remember { mutableStateOf(false) }
    var dateError by remember { mutableStateOf(false) }
    var expandedAmPm by remember { mutableStateOf(false) }

    fun validateTime(): Boolean = "^(0[1-9]|1[0-2]):[0-5][0-9]$".toRegex().matches(time)
    fun validateDate(): Boolean = "^(0[1-9]|1[0-2])/(0[1-9]|[12][0-9]|3[01])/\\d{4}$".toRegex().matches(exactDate)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = cardColor,
        title = { Text("FERTILIZER LOG", color = Color.White, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Time & AM/PM
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = time,
                        onValueChange = { newValue ->
                            val digits = newValue.filter { it.isDigit() }.take(4)
                            var formatted = ""
                            for (i in digits.indices) {
                                formatted += digits[i]
                                if (i == 1 && digits.length > 2) formatted += ":"
                            }
                            time = formatted
                            timeError = false
                        },
                        label = { Text("Time (HH:MM)", color = Color.LightGray) },
                        modifier = Modifier.weight(1f),
                        isError = timeError,
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )

                    ExposedDropdownMenuBox(expanded = expandedAmPm, onExpandedChange = { expandedAmPm = !expandedAmPm }, modifier = Modifier.weight(0.7f)) {
                        OutlinedTextField(
                            value = amPm,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("AM/PM", color = Color.LightGray) },
                            modifier = Modifier.menuAnchor(),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                        )
                        ExposedDropdownMenu(expanded = expandedAmPm, onDismissRequest = { expandedAmPm = false }) {
                            DropdownMenuItem(text = { Text("AM") }, onClick = { amPm = "AM"; expandedAmPm = false })
                            DropdownMenuItem(text = { Text("PM") }, onClick = { amPm = "PM"; expandedAmPm = false })
                        }
                    }
                }

                // Date
                OutlinedTextField(
                    value = exactDate,
                    onValueChange = { newValue ->
                        val digits = newValue.filter { it.isDigit() }.take(8)
                        var formatted = ""
                        for (i in digits.indices) {
                            formatted += digits[i]
                            if (i == 1 && digits.length > 2) formatted += "/"
                            if (i == 3 && digits.length > 4) formatted += "/"
                        }
                        exactDate = formatted
                        dateError = false
                    },
                    label = { Text("Date (MM/DD/YYYY)", color = Color.LightGray) },
                    modifier = Modifier.fillMaxWidth(),
                    isError = dateError,
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )

                // Optional Fields
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Quantity (Optional)", color = Color.LightGray) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Reason (Optional)", color = Color.LightGray) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val isTimeValid = validateTime()
                val isDateValid = validateDate()
                if (!isTimeValid) timeError = true
                if (!isDateValid) dateError = true
                if (isTimeValid && isDateValid) {
                    onSave(time, amPm, exactDate, quantity, reason)
                }
            }) { Text("Save", color = neonGreen) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = Color.Gray) }
        }
    )
}