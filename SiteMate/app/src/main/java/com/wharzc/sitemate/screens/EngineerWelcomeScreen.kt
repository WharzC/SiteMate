package com.wharzc.sitemate.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import com.wharzc.sitemate.R
import com.wharzc.sitemate.ui.theme.SiteMateBlue

@Composable
fun EngineerWelcomeScreen(
    onWelcomeComplete: () -> Unit // Removed the 'surname' parameter!
) {
    var lastName by remember { mutableStateOf("...") }
    var isDataLoaded by remember { mutableStateOf(false) }

    // 1. Fetch the user's last name from Firestore
    LaunchedEffect(Unit) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            FirebaseFirestore.getInstance().collection("users").document(user.uid).get()
                .addOnSuccessListener { doc ->
                    lastName = doc.getString("lastName") ?: ""
                    isDataLoaded = true
                }
                .addOnFailureListener {
                    lastName = ""
                    isDataLoaded = true
                }
        } else {
            lastName = ""
            isDataLoaded = true
        }
    }

    // 2. Wait 2 seconds AFTER the data is loaded, then trigger navigation
    LaunchedEffect(isDataLoaded) {
        if (isDataLoaded) {
            delay(2000)
            onWelcomeComplete()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.sitemate_logo),
            contentDescription = "Logo",
            modifier = Modifier.size(150.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Welcome,",
            fontSize = 24.sp,
            color = SiteMateBlue
        )
        Text(
            text = if (isDataLoaded) "Engr. $lastName!" else "Loading...",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = SiteMateBlue
        )
    }
}