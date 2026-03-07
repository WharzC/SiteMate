package com.wharzc.sitemate.screens.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
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
import com.wharzc.sitemate.ui.theme.SiteMateOrange

@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: AuthViewModel = viewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // Auto-login logic: Checks for role and redirects
    LaunchedEffect(viewModel.userRole) {
        if (viewModel.userRole != null) {
            when (viewModel.userRole) {
                "Unassigned" -> navController.navigate("role_selection") { popUpTo("login") { inclusive = true } }
                "Engineer" -> navController.navigate("engineer_welcome") { popUpTo("login") { inclusive = true } }
                "Client" -> navController.navigate("client_welcome") { popUpTo("login") { inclusive = true } }
                else -> navController.navigate("role_selection") { popUpTo("login") { inclusive = true } }
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize().padding(24.dp)
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.sitemate_logo),
                contentDescription = "Logo",
                modifier = Modifier.size(80.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("SiteMate", fontSize = 40.sp, fontWeight = FontWeight.ExtraBold, color = SiteMateBlue)
            Text("Construction Management", fontSize = 14.sp, color = Color.Gray)

            Spacer(modifier = Modifier.height(48.dp))

            if (viewModel.errorMessage != null) {
                Text(
                    text = viewModel.errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            OutlinedTextField(
                value = email, onValueChange = { email = it },
                label = { Text("Email Address") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(), singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password, onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, contentDescription = null)
                    }
                },
                modifier = Modifier.fillMaxWidth(), singleLine = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    viewModel.login(email, password) { role: String? ->
                        // Traffic cop routing for manual login!
                        when (role) {
                            "Engineer" -> navController.navigate("engineer_welcome") { popUpTo("login") { inclusive = true } }
                            "Client" -> navController.navigate("client_welcome") { popUpTo("login") { inclusive = true } }
                            "Unassigned" -> navController.navigate("role_selection") { popUpTo("login") { inclusive = true } }
                            else -> navController.navigate("role_selection") { popUpTo("login") { inclusive = true } }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SiteMateBlue)
            ) {
                if (viewModel.isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Login", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedButton(
                onClick = { /* Google Sign In */ },
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("Sign in with Google", color = Color.DarkGray, fontWeight = FontWeight.SemiBold)
            }
        }

        Row(
            modifier = Modifier.align(Alignment.BottomCenter),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Don't have an account?", color = Color.Gray)
            TextButton(onClick = { navController.navigate("signup") }) {
                Text("Sign Up", color = SiteMateOrange, fontWeight = FontWeight.Bold)
            }
        }
    }
}