package com.wharzc.prime.farmer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wharzc.prime.viewmodel.SensorViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID
import androidx.compose.ui.text.style.TextAlign
import com.wharzc.prime.viewmodel.FertilizerViewModel
import com.wharzc.prime.data.FertilizerNotif



fun isDateExpired(dateStr: String): Boolean {
    val format = SimpleDateFormat("MM/dd/yyyy", Locale.US).apply { isLenient = false }
    return try {
        val parsedDate = format.parse(dateStr) ?: return false
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
        parsedDate.before(today)
    } catch (e: Exception) { false }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FertilizerScreen(viewModel: SensorViewModel, fertViewModel: FertilizerViewModel) {
    val data by viewModel.sensorData.collectAsState()

    // Observe the live Room database list instead of a temporary local state
    val notifications by fertViewModel.notifications.collectAsState()

    val scrollState = rememberScrollState()
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp

    var showAddModal by remember { mutableStateOf(false) }
    var notifToDelete by remember { mutableStateOf<FertilizerNotif?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val bgColor = Color(0xFF195532)
    val cardColor = Color(0xFF2A5339)
    val neonGreen = Color(0xFF00FFCC)

    val insight = when {
        data.npk.nitrogen > 40 || data.npk.phosphorus > 50 || data.npk.potassium > 40 ->
            "High nutrient levels detected. Delay fertilization to prevent fertilizer burn and nutrient runoff."
        data.npk.nitrogen < 15 ->
            "Low Nitrogen detected (${data.npk.nitrogen} mg/kg). Ideal time to apply urea or nitrogen-rich fertilizer to boost leaf growth."
        data.npk.phosphorus < 20 ->
            "Low Phosphorus detected (${data.npk.phosphorus} mg/kg). Consider applying phosphate fertilizers to strengthen root development."
        data.npk.potassium < 15 ->
            "Low Potassium detected (${data.npk.potassium} mg/kg). Apply potassium-based fertilizer to improve crop resistance to disease."
        else ->
            "NPK levels are optimal. No immediate fertilization required. Maintain current schedule."
    }

    if (notifToDelete != null) {
        AlertDialog(
            onDismissRequest = { notifToDelete = null },
            title = { Text("Delete Schedule", color = Color.White) },
            text = { Text("Are you sure you want to delete this schedule?", color = Color.LightGray) },
            containerColor = cardColor,
            confirmButton = {
                TextButton(onClick = {
                    // Call the ViewModel to delete from Room and cancel the OS alarm
                    fertViewModel.deleteNotification(notifToDelete!!)
                    notifToDelete = null
                }) { Text("Delete", color = Color(0xFFEF5350)) }
            },
            dismissButton = {
                TextButton(onClick = { notifToDelete = null }) { Text("Cancel", color = neonGreen) }
            }
        )
    }

    if (showAddModal) {
        AddNotificationModal(
            cardColor = cardColor,
            neonGreen = neonGreen,
            onDismiss = { showAddModal = false },
            onSave = { label, time, amPm, isRecurring, rule ->
                val isDuplicate = notifications.any { it.time == time && it.amPm == amPm && it.dateOrRule == rule }
                if (notifications.size >= 10) {
                    errorMessage = "Maximum 10 notifications allowed."
                } else if (isDuplicate) {
                    errorMessage = "This exact schedule already exists."
                } else {
                    // Call the ViewModel to save to Room and register the OS alarm
                    fertViewModel.addNotification(
                        FertilizerNotif(label = label, time = time, amPm = amPm, isRecurring = isRecurring, dateOrRule = rule)
                    )
                    errorMessage = null
                    showAddModal = false
                }
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize().background(bgColor)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                // Custom Neon Scrollbar modifier
                .drawWithContent {
                    drawContent()
                    val scrollbarHeight = size.height * (size.height / (scrollState.maxValue + size.height))
                    val scrollbarY = (scrollState.value.toFloat() / scrollState.maxValue) * (size.height - scrollbarHeight)
                    if (scrollState.maxValue > 0) {
                        drawRoundRect(
                            color = neonGreen.copy(alpha = 0.6f),
                            topLeft = Offset(size.width - 8.dp.toPx(), scrollbarY),
                            size = Size(4.dp.toPx(), scrollbarHeight),
                            cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                        )
                    }
                }
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))


            Card(
                colors = CardDefaults.cardColors(containerColor = cardColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(screenHeight * 0.25f)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp).fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally // Centers content horizontally
                ) {
                    Text(
                        text = "LIVE FIELD ANALYSIS", // Capitalized
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        textAlign = TextAlign.Center // Ensures the text itself aligns center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = insight,
                        color = neonGreen,
                        fontSize = 18.sp,
                        lineHeight = 26.sp,
                        textAlign = TextAlign.Center // Centers multi-line descriptions
                    )
                }
            }

            Text("Fertilizer Schedules", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)

            if (errorMessage != null) {
                Text(errorMessage!!, color = Color(0xFFEF5350), fontSize = 12.sp)
            }

            if (notifications.isEmpty()) {
                Text("No active schedules. Press + to add a notification.", color = Color.Gray, fontSize = 14.sp)
            } else {
                notifications.forEach { notif ->
                    val expired = !notif.isRecurring && isDateExpired(notif.dateOrRule)
                    Card(colors = CardDefaults.cardColors(containerColor = cardColor), modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                if (notif.label.isNotBlank()) {
                                    Text(notif.label, color = neonGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("${notif.time} ${notif.amPm}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    if (expired) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("EXPIRED", color = Color(0xFFEF5350), fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                    }
                                }
                                Text(
                                    if (notif.isRecurring) "Recurring: ${notif.dateOrRule}" else "Exact: ${notif.dateOrRule}",
                                    color = if (expired) Color(0xFFEF5350) else Color.LightGray,
                                    fontSize = 12.sp
                                )
                            }
                            IconButton(onClick = { notifToDelete = notif }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = Color(0xFFEF5350))
                            }
                        }
                    }
                }
            }
            // Bottom padding to ensure FAB and Navigation don't obscure content
            Spacer(modifier = Modifier.height(80.dp))
        }

        FloatingActionButton(
            onClick = { showAddModal = true },
            containerColor = neonGreen,
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp).padding(bottom = 60.dp)
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Add Schedule", tint = bgColor)
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNotificationModal(
    cardColor: Color,
    neonGreen: Color,
    onDismiss: () -> Unit,
    onSave: (String, String, String, Boolean, String) -> Unit
) {
    var label by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var amPm by remember { mutableStateOf("AM") }
    var isRecurring by remember { mutableStateOf(true) }
    var exactDate by remember { mutableStateOf("") }

    var timeError by remember { mutableStateOf(false) }
    var dateError by remember { mutableStateOf(false) }

    var expandedAmPm by remember { mutableStateOf(false) }
    var expandedRecurring by remember { mutableStateOf(false) }
    var recurringRule by remember { mutableStateOf("Sundays to Mondays") }
    val recurringOptions = listOf("Sundays to Mondays", "Weekdays", "Weekends", "Every nth of the month")
    var nthDay by remember { mutableStateOf("1") }

    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    // Validation Logic
    fun validateTime(): Boolean = "^(0[1-9]|1[0-2]):[0-5][0-9]$".toRegex().matches(time)
    fun validateDate(): Boolean {
        if (!"^(0[1-9]|1[0-2])/(0[1-9]|[12][0-9]|3[01])/\\d{4}$".toRegex().matches(exactDate)) return false
        val format = SimpleDateFormat("MM/dd/yyyy", Locale.US).apply { isLenient = false }
        return try { format.parse(exactDate); true } catch (e: Exception) { false }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val format = SimpleDateFormat("MM/dd/yyyy", Locale.US)
                        exactDate = format.format(java.util.Date(millis))
                        dateError = false
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

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = cardColor,
        title = { Text("Add Schedule", color = Color.White) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Optional Label
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Label (Optional)", color = Color.LightGray) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                    singleLine = true
                )

                // Time and AM/PM
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
                        supportingText = if (timeError) { { Text("Invalid time (01:00 - 12:59)") } } else null,
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                        placeholder = { Text("08:00", color = Color.Gray) }
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

                // Type Selection
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = isRecurring, onClick = { isRecurring = true }, colors = RadioButtonDefaults.colors(selectedColor = neonGreen))
                    Text("Recurring", color = Color.White)
                    Spacer(modifier = Modifier.width(16.dp))
                    RadioButton(selected = !isRecurring, onClick = { isRecurring = false; dateError = false }, colors = RadioButtonDefaults.colors(selectedColor = neonGreen))
                    Text("Exact Date", color = Color.White)
                }

                // Dynamic Date/Recurring Inputs
                if (isRecurring) {
                    ExposedDropdownMenuBox(expanded = expandedRecurring, onExpandedChange = { expandedRecurring = !expandedRecurring }) {
                        OutlinedTextField(
                            value = recurringRule,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Recurrence", color = Color.LightGray) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                        )
                        ExposedDropdownMenu(expanded = expandedRecurring, onDismissRequest = { expandedRecurring = false }) {
                            recurringOptions.forEach { selection ->
                                DropdownMenuItem(
                                    text = { Text(selection) },
                                    onClick = {
                                        recurringRule = selection
                                        expandedRecurring = false
                                    }
                                )
                            }
                        }
                    }

                    if (recurringRule == "Every nth of the month") {
                        OutlinedTextField(
                            value = nthDay,
                            onValueChange = { if (it.length <= 2 && it.all { char -> char.isDigit() }) nthDay = it },
                            label = { Text("Day of month (1-31)", color = Color.LightGray) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                        )
                        Text("Note: Day 31 shifts to the last day of the month for shorter months.", color = neonGreen, fontSize = 10.sp)
                    }
                } else {
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
                        supportingText = if (dateError) { { Text("Invalid date format") } } else null,
                        placeholder = { Text("12/31/2026", color = Color.Gray) },
                        trailingIcon = {
                            IconButton(onClick = { showDatePicker = true }) {
                                Icon(Icons.Filled.CalendarToday, contentDescription = "Pick Date", tint = neonGreen)
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val isTimeValid = validateTime()
                val isDateValid = if (!isRecurring) validateDate() else true

                if (!isTimeValid) timeError = true
                if (!isDateValid) dateError = true

                if (isTimeValid && isDateValid) {
                    val rule = if (!isRecurring) exactDate else if (recurringRule == "Every nth of the month") "Day $nthDay of month" else recurringRule
                    onSave(label, time, amPm, isRecurring, rule)
                }
            }) { Text("Save", color = neonGreen) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = Color.Gray) }
        }
    )
}