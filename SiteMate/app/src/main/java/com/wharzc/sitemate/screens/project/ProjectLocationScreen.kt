package com.wharzc.sitemate.screens.project

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wharzc.sitemate.model.ProjectCreationViewModel
import com.wharzc.sitemate.ui.theme.SiteMateBlue
import com.wharzc.sitemate.screens.components.ProjectWizardHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectLocationScreen(
    viewModel: ProjectCreationViewModel,
    onProceed: () -> Unit,
    onBack: () -> Unit
) {

    LaunchedEffect(Unit) {
        if (viewModel.cityList.isEmpty()) {
            viewModel.fetchCities()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // --- 1. PINNED HEADER ---
        ProjectWizardHeader(
            title = "Location",
            onBackClick = onBack
        )

        // --- 2. CENTERED CASCADING FORM ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f) // Takes up the remaining vertical space
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center, // Centers everything vertically
            horizontalAlignment = Alignment.CenterHorizontally // Centers text/items horizontally
        ) {
            Text(
                text = "Please select a municipality and barangay within Oriental Mindoro, then enter your specific address.",
                color = Color.Gray,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // --- CITY DROPDOWN (Always Visible) ---
            var cityExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = cityExpanded,
                onExpandedChange = { cityExpanded = !cityExpanded }
            ) {
                OutlinedTextField(
                    value = viewModel.selectedCity,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("City / Municipality") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = cityExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SiteMateBlue, focusedLabelColor = SiteMateBlue)
                )
                ExposedDropdownMenu(
                    expanded = cityExpanded,
                    onDismissRequest = { cityExpanded = false }
                ) {
                    if (viewModel.cityList.isEmpty()) {
                        DropdownMenuItem(text = { Text("Loading cities...") }, onClick = {})
                    } else {
                        viewModel.cityList.forEach { city ->
                            DropdownMenuItem(
                                text = { Text(city) },
                                onClick = {
                                    viewModel.selectedCity = city
                                    viewModel.fetchBarangays(city)
                                    cityExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // --- BARANGAY DROPDOWN (Cascades when City is filled) ---
            AnimatedVisibility(
                visible = viewModel.selectedCity.isNotEmpty(),
                enter = fadeIn() + expandVertically()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(16.dp))
                    var brgyExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = brgyExpanded,
                        onExpandedChange = { brgyExpanded = !brgyExpanded }
                    ) {
                        OutlinedTextField(
                            value = viewModel.selectedBarangay,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Barangay") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = brgyExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SiteMateBlue, focusedLabelColor = SiteMateBlue)
                        )
                        ExposedDropdownMenu(
                            expanded = brgyExpanded,
                            onDismissRequest = { brgyExpanded = false }
                        ) {
                            if (viewModel.barangayList.isEmpty()) {
                                DropdownMenuItem(text = { Text("Loading barangays...") }, onClick = {})
                            } else {
                                viewModel.barangayList.forEach { brgy ->
                                    DropdownMenuItem(
                                        text = { Text(brgy) },
                                        onClick = {
                                            viewModel.selectedBarangay = brgy
                                            brgyExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // --- STREET ADDRESS (Cascades when Barangay is filled) ---
            AnimatedVisibility(
                visible = viewModel.selectedBarangay.isNotEmpty(),
                enter = fadeIn() + expandVertically()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = viewModel.streetAddress,
                        onValueChange = { viewModel.streetAddress = it },
                        label = { Text("Street, Building or House Number") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SiteMateBlue, focusedLabelColor = SiteMateBlue)
                    )
                }
            }

            // --- LANDMARK & BUTTON (Cascades when Street is filled) ---
            AnimatedVisibility(
                visible = viewModel.streetAddress.isNotEmpty(),
                enter = fadeIn() + expandVertically()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = viewModel.landmark,
                        onValueChange = { viewModel.landmark = it },
                        label = { Text("Landmark (Optional)") },
                        placeholder = { Text("e.g. Near the plaza, behind the church") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SiteMateBlue, focusedLabelColor = SiteMateBlue)
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = onProceed,
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SiteMateBlue)
                    ) {
                        Text("Proceed to Upload", color = Color.White)
                    }
                }
            }
        }
    }
}