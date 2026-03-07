package com.wharzc.sitemate_02.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.wharzc.sitemate_02.R
import com.wharzc.sitemate_02.model.UserSex

@OptIn(ExperimentalMaterial3Api::class) // Needed for the Dropdown design
@Composable
fun SignUpScreen(
    onSignUpSuccess: () -> Unit
) {
    var firstName by remember { mutableStateOf("") }
    var middleInitial by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    // Dropdown State
    var selectedSex by remember { mutableStateOf(UserSex.MALE) }
    var isSexDropdownExpanded by remember { mutableStateOf(false) }

    // Password Visibility State
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isConfirmPasswordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.sitemate_logo),
            contentDescription = "SiteMate Logo",
            modifier = Modifier.size(100.dp)
        )

        Text(text = "Create Account", style = MaterialTheme.typography.headlineMedium)

        OutlinedTextField(value = firstName, onValueChange = { firstName = it }, label = { Text("First Name") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = middleInitial, onValueChange = { middleInitial = it }, label = { Text("M.I. (Optional)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = lastName, onValueChange = { lastName = it }, label = { Text("Last Name") }, modifier = Modifier.fillMaxWidth())

        // --- SEX DROPDOWN ---
        ExposedDropdownMenuBox(
            expanded = isSexDropdownExpanded,
            onExpandedChange = { isSexDropdownExpanded = !isSexDropdownExpanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = if (selectedSex == UserSex.MALE) "Male" else "Female",
                onValueChange = {},
                readOnly = true,
                label = { Text("Sex") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isSexDropdownExpanded) },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = isSexDropdownExpanded,
                onDismissRequest = { isSexDropdownExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Male") },
                    onClick = {
                        selectedSex = UserSex.MALE
                        isSexDropdownExpanded = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Female") },
                    onClick = {
                        selectedSex = UserSex.FEMALE
                        isSexDropdownExpanded = false
                    }
                )
            }
        }

        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email Address") }, modifier = Modifier.fillMaxWidth())

        // --- PASSWORD WITH EYE ICON ---
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                    // Uses standard Eye Icon
                    Icon(
                        imageVector = if (isPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = "Toggle Password"
                    )
                }
            }
        )

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirm Password") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = if (isConfirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { isConfirmPasswordVisible = !isConfirmPasswordVisible }) {
                    Icon(
                        imageVector = if (isConfirmPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = "Toggle Password"
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { onSignUpSuccess() },
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text("Confirm") // Changed button text
        }
    }
}