package com.wharzc.sitemate.screens.client

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
import com.wharzc.sitemate.ui.theme.SiteMateOrange

@Composable
fun ClientWelcomeScreen(
    onWelcomeComplete: () -> Unit
) {
    var lastName by remember { mutableStateOf("...") }
    var titlePrefix by remember { mutableStateOf("") }
    var isDataLoaded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            FirebaseFirestore.getInstance().collection("users").document(user.uid).get()
                .addOnSuccessListener { doc ->
                    lastName = doc.getString("lastName") ?: ""
                    val sex = doc.getString("sex") ?: "Male"
                    titlePrefix = if (sex.equals("Female", ignoreCase = true)) "Mrs." else "Mr."
                    isDataLoaded = true
                }
                .addOnFailureListener {
                    lastName = ""
                    titlePrefix = "Mr/Mrs."
                    isDataLoaded = true
                }
        } else {
            isDataLoaded = true
        }
    }

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
        Text("Welcome,", fontSize = 24.sp, color = SiteMateBlue)
        Text(
            text = if (isDataLoaded) "$titlePrefix $lastName!" else "Loading...",
            fontSize = 32.sp, fontWeight = FontWeight.Bold, color = SiteMateOrange // VIP color for clients
        )
    }
}