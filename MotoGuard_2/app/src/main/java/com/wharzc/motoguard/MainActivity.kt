package com.wharzc.motoguard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // This is the entry point for Compose
            AppNavigation()
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(navController = navController)
        }
        composable("dashboard") {
            DashboardScreen(navController = navController)
        }
        composable("liveMap") {
            LiveTrackingScreen(navController)
        }
        composable("tripHistory") {
            TripHistoryScreen(navController)
        }
        composable("settings") {
            SettingsScreen(navController)
        }
        composable("notifications") {
            NotificationsScreen(navController)
        }
    }

    }
