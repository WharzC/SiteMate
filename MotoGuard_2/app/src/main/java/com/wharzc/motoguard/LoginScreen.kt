package com.wharzc.motoguard

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen() {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // Futuristic Color Palette
    val darkBackground = Color(0xFF0B0C10)
    val neonBlue = Color(0xFF45A29E)
    val neonCyan = Color(0xFF66FCF1)
    val surfaceDark = Color(0xFF1F2833)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(darkBackground)
    ) {
        // Dynamic Futuristic Background from the internet using Coil
        AsyncImage(
            model = "https://images.unsplash.com/photo-1550751827-4bd374c3f58b?q=80&w=1080&auto=format&fit=crop", // Cyberpunk/Tech background
            contentDescription = "Futuristic Background",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            alpha = 0.3f // Keep it subtle so the UI pops
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Your Logo
            Image(
                painter = painterResource(id = R.drawable.motoguard_logo), // Replace with your actual drawable name
                contentDescription = "MotoGuard Logo",
                modifier = Modifier
                    .size(150.dp)
                    .clip(RoundedCornerShape(16.dp))
            )

            Spacer(modifier = Modifier.height(24.dp))

            // App Title
            Text(
                text = "MotoGuard",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = neonCyan,
                letterSpacing = 2.sp
            )

            Text(
                text = "SYSTEM SECURED",
                fontSize = 12.sp,
                color = Color.Gray,
                letterSpacing = 4.sp
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Email Input
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email Address", color = Color.Gray) },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = neonBlue) },
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = neonCyan,
                    unfocusedBorderColor = neonBlue.copy(alpha = 0.5f),
                    focusedTextColor = Color.White, // Updated parameter
                    unfocusedTextColor = Color.White, // Updated parameter
                    containerColor = surfaceDark.copy(alpha = 0.8f)
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Password Input
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password", color = Color.Gray) },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = neonBlue) },
                visualTransformation = PasswordVisualTransformation(),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = neonCyan,
                    unfocusedBorderColor = neonBlue.copy(alpha = 0.5f),
                    focusedTextColor = Color.White, // Updated parameter
                    unfocusedTextColor = Color.White, // Updated parameter
                    containerColor = surfaceDark.copy(alpha = 0.8f)
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Login Button with Gradient
            Button(
                onClick = { /* TODO: Implement Login Logic */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(listOf(neonBlue, neonCyan)),
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "INITIALIZE SYSTEM",
                        color = darkBackground,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = { /* TODO: Registration / Forgot Password */ }) {
                Text("Forgot Password?", color = neonBlue)
            }
        }
    }
}