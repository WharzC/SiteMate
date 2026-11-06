package com.wharzc.prime

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.FirebaseApp
import com.wharzc.prime.navigation.AppNavigation
import com.wharzc.prime.theme.PRIMETheme
import com.wharzc.prime.viewmodel.AdminViewModel
import com.wharzc.prime.viewmodel.AuthViewModel
import com.wharzc.prime.viewmodel.SensorViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseApp.initializeApp(this)

        setContent {
            PRIMETheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val authViewModel: AuthViewModel = viewModel()
                    val sensorViewModel: SensorViewModel = viewModel()
                    val adminViewModel: AdminViewModel = viewModel()

                    AppNavigation(
                        authViewModel = authViewModel,
                        sensorViewModel = sensorViewModel,
                        adminViewModel = adminViewModel
                    )
                }
            }
        }
    }
}