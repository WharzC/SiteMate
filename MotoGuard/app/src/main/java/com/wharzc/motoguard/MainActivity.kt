package com.wharzc.motoguard

import android.app.AlertDialog
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels // ADDED FOR DELEGATE
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import org.maplibre.android.MapLibre

class MainActivity : ComponentActivity() {

    // FIXED: Using the property delegate pattern safely at the class level
    private val tripViewModel: TripViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize MapLibre Engine
        org.maplibre.android.MapLibre.getInstance(this)

        NotificationRepository.createNotificationChannel(this)
        NotificationRepository.onNewNotification = { title, message ->
            runOnUiThread {
                AlertDialog.Builder(this)
                    .setTitle(title)
                    .setMessage(message)
                    .setPositiveButton("OK", null)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show()
            }
        }

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    val currentUser = FirebaseAuth.getInstance().currentUser
                    val startDestination = if (currentUser?.email == "wharzwharz@gmail.com") {
                        "Admin"   // special user jumps straight to dashboard
                    } else {
                        "login"       // everyone else starts at login
                    }

                    NavHost(navController = navController, startDestination = "login") {

                        composable("login") {
                            LoginScreen(
                                onNavigateToDashboard = {
                                    // Check email AFTER successful login
                                    val user = FirebaseAuth.getInstance().currentUser
                                    if (user?.email == "wharzwharz@gmail.com") {
                                        // Special user → go to admin screen
                                        navController.navigate("Admin") {
                                            popUpTo("login") { inclusive = true }
                                        }
                                    } else {
                                        // Regular user → go to normal dashboard
                                        navController.navigate("dashboard") {
                                            popUpTo("login") { inclusive = true }
                                        }
                                    }
                                },
                                onNavigateToSignUp = { navController.navigate("signup") }
                            )
                        }


                        composable("signup") {
                            SignUpScreen(
                                onNavigateToDashboard = {
                                    navController.navigate("dashboard") {
                                        popUpTo("signup") { inclusive = true }
                                    }
                                },
                                onNavigateToLogin = { navController.navigate("login") }
                            )
                        }

                        composable("admin") {
                            val currentUser = FirebaseAuth.getInstance().currentUser
                            if (currentUser?.email == "wharzwharz@gmail.com") {
                                AdminDashboardScreen(
                                    onLogout = {
                                        FirebaseAuth.getInstance().signOut()
                                        navController.navigate("login") {
                                            popUpTo(0) { inclusive = true }
                                        }
                                    }
                                )
                            } else {
                                LaunchedEffect(Unit) {
                                    navController.navigate("login") {
                                        popUpTo(0) { inclusive = true }
                                    }
                                }
                            }
                        }

                        composable("dashboard") {
                            DashboardScreen(
                                onNavigateToSettings = { navController.navigate("settings") },
                                onNavigateToNotifications = { navController.navigate("notifications") },
                                onNavigateToTripHistory = { navController.navigate("trip_history") },
                                onNavigateToLiveTracking = { navController.navigate("live_tracking") }
                            )
                        }

                        composable("live_tracking") {
                            LiveTrackingScreen(
                                tripViewModel = tripViewModel, // Cleanly passed down
                                onNavigateBack = { navController.popBackStack() },
                                onNavigateToTripHistory = { navController.navigate("trip_history") }
                            )
                        }

                        composable("trip_history") {
                            TripHistoryScreen(
                                tripViewModel = tripViewModel, // Cleanly passed down
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        composable("notifications") {
                            NotificationsScreen(onNavigateBack = { navController.popBackStack() })
                        }
                        composable("geofence_list") {
                            GeofenceScreen(navController = navController)
                        }
                        composable(route = "geofence_screen") {
                            GeofenceScreen(navController = navController)
                        }

                        composable("settings") {
                            SettingsScreen(
                                navController = navController,
                                onNavigateBack = { navController.popBackStack() },
                                onLogout = {
                                    FirebaseAuth.getInstance().signOut()
                                    navController.navigate("login") {
                                        popUpTo(0) { inclusive = true }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}