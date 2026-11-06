package com.wharzc.sentry

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.firebase.FirebaseApp
import com.wharzc.sentry.uinterface.AttendanceScreen
import com.wharzc.sentry.uinterface.DashboardScreen
import com.wharzc.sentry.uinterface.FingerprintScreen
import com.wharzc.sentry.uinterface.RfidScreen
import com.wharzc.sentry.viewmodel.SentryViewModel

import android.app.Application

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)



        // 2. LOAD THE UI AND VIEWMODEL SECOND
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Dashboard : Screen("dashboard", "Dashboard", Icons.Default.Home)
    object Rfid : Screen("rfid", "RFID", Icons.Default.Person)

    // Changed these two to standard core icons to prevent rendering crashes
    object Fingerprint : Screen("fingerprint", "Fingerprint", Icons.Default.Check)
    object Attendance : Screen("attendance", "Attendance", Icons.Default.List)
}

@Composable
fun AppNavigation(viewModel: SentryViewModel = viewModel(factory = SentryViewModel.Factory)) {
    val navController = rememberNavController()
    val navItems = listOf(Screen.Dashboard, Screen.Rfid, Screen.Fingerprint, Screen.Attendance)

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                navItems.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = currentRoute == screen.route,
                        onClick = {
                            if (currentRoute != screen.route) {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    viewModel = viewModel,
                    onNavigateToRfid = { navController.navigate(Screen.Rfid.route) },
                    onNavigateToFingerprint = { navController.navigate(Screen.Fingerprint.route) },
                    onNavigateToAttendance = { navController.navigate(Screen.Attendance.route) }
                )
            }
            composable(Screen.Rfid.route) {
                RfidScreen(viewModel = viewModel)
            }
            composable(Screen.Fingerprint.route) {
                FingerprintScreen(viewModel = viewModel)
            }
            composable(Screen.Attendance.route) {
                AttendanceScreen(viewModel = viewModel)
            }
        }
    }
}