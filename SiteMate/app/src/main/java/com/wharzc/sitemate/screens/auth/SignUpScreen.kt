package com.wharzc.sitemate.screens.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.wharzc.sitemate.R
import com.wharzc.sitemate.model.AuthViewModel
import com.wharzc.sitemate.ui.theme.SiteMateBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    navController: NavController,
    viewModel: AuthViewModel = viewModel()
) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    val sexOptions = listOf("Male", "Female")
    var expanded by remember { mutableStateOf(false) }
    var selectedSex by remember { mutableStateOf(sexOptions[0]) }

    var passwordVisible by remember { mutableStateOf(false) }
    var localError by remember { mutableStateOf<String?>(null) }



    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.sitemate_logo), // Ensure logo is in res/drawable/
            contentDescription = "SiteMate Logo",
            modifier = Modifier.size(60.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("Create Account", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = SiteMateBlue)
        Text("Join SiteMate today.", fontSize = 14.sp, color = Color.Gray)

        Spacer(modifier = Modifier.height(32.dp))

        val displayError = localError ?: viewModel.errorMessage
        if (displayError != null) {
            Text(text = displayError, color = MaterialTheme.colorScheme.error, fontSize = 14.sp, modifier = Modifier.padding(bottom = 16.dp))
        }

        // Name Row
        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(value = firstName, onValueChange = { firstName = it; localError = null }, label = { Text("First Name") }, modifier = Modifier.weight(1f), singleLine = true)
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedTextField(value = lastName, onValueChange = { lastName = it; localError = null }, label = { Text("Last Name") }, modifier = Modifier.weight(1f), singleLine = true)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Sex Dropdown
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }, modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = selectedSex, onValueChange = {}, readOnly = true, label = { Text("Sex") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor()
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                sexOptions.forEach { option -> DropdownMenuItem(text = { Text(option) }, onClick = { selectedSex = option; expanded = false }) }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(value = email, onValueChange = { email = it; localError = null }, label = { Text("Email Address") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), modifier = Modifier.fillMaxWidth(), singleLine = true)

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password, onValueChange = { password = it; localError = null }, label = { Text("Password (Min 6 chars)") },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                IconButton(onClick = { passwordVisible = !passwordVisible }) { Icon(imageVector = image, contentDescription = null) }
            },
            modifier = Modifier.fillMaxWidth(), singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = confirmPassword, onValueChange = { confirmPassword = it; localError = null }, label = { Text("Confirm Password") },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(), singleLine = true
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                if (password != confirmPassword) {
                    localError = "Passwords do not match."
                } else if (password.length < 6) {
                    localError = "Password must be at least 6 characters."
                } else {
                    // Pass the navigation into the onSuccess callback!
                    viewModel.signUpWithEmail(firstName, lastName, selectedSex, email, password) {
                        navController.navigate("role_selection") {
                            popUpTo("signup") { inclusive = true }
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SiteMateBlue),
            enabled = !viewModel.isLoading
        ) {
            if (viewModel.isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
            } else {
                Text("Sign Up", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = { navController.popBackStack() }) {
            Text("Already have an account? Log In", color = Color.Gray)
        }
    }
}