package com.wharzc.sitemate_02

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.delay

// IMPORT YOUR ROLE SELECTION SCREEN
// Ensure this path matches your folder structure exactly
import com.wharzc.sitemate_02.ui.auth.RoleSelectionScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            // Fix: Using standard MaterialTheme to avoid "Unresolved reference" errors
            MaterialTheme {
                val navController = rememberNavController()

                // START: Navigation Graph
                NavHost(navController = navController, startDestination = "splash") {

                    // 1. SPLASH SCREEN
                    composable("splash") {
                        SplashScreen(onTimeout = {
                            navController.navigate("login") {
                                popUpTo("splash") { inclusive = true }
                            }
                        })
                    }

                    // 2. LOGIN SCREEN
                    composable("login") {
                        LoginScreen(
                            onLoginSuccess = {
                                // Check if role exists in storage
                                val sharedPref = getSharedPreferences("SiteMatePrefs", Context.MODE_PRIVATE)
                                val savedRole = sharedPref.getString("USER_ROLE", null)

                                if (savedRole != null) {
                                    // User has a role -> Go Home
                                    navController.navigate("home") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                } else {
                                    // New User -> Go to Role Selection
                                    navController.navigate("role_selection")
                                }
                            }
                        )
                    }

                    // 3. ROLE SELECTION SCREEN
                    composable("role_selection") {
                        RoleSelectionScreen(
                            onRoleSelected = { role ->
                                // Save the selected role
                                getSharedPreferences("SiteMatePrefs", Context.MODE_PRIVATE)
                                    .edit()
                                    .putString("USER_ROLE", role.name)
                                    .apply()

                                // Go to Home
                                navController.navigate("home") {
                                    popUpTo("role_selection") { inclusive = true }
                                    popUpTo("login") { inclusive = true }
                                }
                            }
                        )
                    }

                    // 4. HOME SCREEN (Placeholder)
                    composable("home") {
                        DummyHomeScreen()
                    }
                }
            }
        }
    }
}

// --- COMPOSABLE SCREENS ---

@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(2000) // 2 Seconds delay
        onTimeout()
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(id = R.drawable.sitemate_logo),
                contentDescription = "Logo",
                modifier = Modifier.size(150.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            CircularProgressIndicator(color = Color(0xFF1F3B58))
        }
    }
}

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    // State variables for inputs
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Logo
        Image(
            painter = painterResource(id = R.drawable.sitemate_logo),
            contentDescription = "SiteMate Logo",
            modifier = Modifier.size(100.dp)
        )
        Spacer(modifier = Modifier.height(32.dp))

        // Text
        Text(
            text = "Welcome Back!",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1F3B58)
        )
        Text(text = "Please log in to continue", color = Color.Gray)

        Spacer(modifier = Modifier.height(32.dp))

        // Inputs
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email Address") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Button
        Button(
            onClick = { onLoginSuccess() },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1F3B58)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Log In", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun DummyHomeScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Welcome to SiteMate Dashboard!")
    }
}