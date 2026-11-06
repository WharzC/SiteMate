package com.wharzc.prime.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.wharzc.prime.admin.AdminDashboard
import com.wharzc.prime.auth.SignUpScreen
import com.wharzc.prime.farmer.FarmerDashboard
import com.wharzc.prime.auth.LoginScreen
import com.wharzc.prime.viewmodel.AdminViewModel
import com.wharzc.prime.viewmodel.AuthViewModel
import com.wharzc.prime.viewmodel.SensorViewModel

@Composable
fun AppNavigation(
    authViewModel: AuthViewModel,
    sensorViewModel: SensorViewModel,
    adminViewModel: AdminViewModel
) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "login_screen") {

        composable("login_screen") {
            LoginScreen(
                viewModel = authViewModel,
                onNavigateAdmin = {
                    navController.navigate("admin_dashboard") {
                        popUpTo("login_screen") { inclusive = true }
                    }
                },
                onNavigateFarmer = {
                    navController.navigate("farmer_dashboard") {
                        popUpTo("login_screen") { inclusive = true }
                    }
                },
                onSignUpClick = { navController.navigate("signup_screen") }
            )
        }

        composable("signup_screen") {
            SignUpScreen(
                viewModel = authViewModel,
                onNavigateFarmer = {
                    navController.navigate("farmer_dashboard") {
                        popUpTo("login_screen") { inclusive = true }
                    }
                },
                onNavigateBackToLogin = { navController.popBackStack() }
            )
        }

        composable("farmer_dashboard") {
            FarmerDashboard(
                viewModel = sensorViewModel,
                onLogout = {
                    navController.navigate("login_screen") {
                        popUpTo(0)
                    }
                }
            )
        }

        composable("admin_dashboard") {
            AdminDashboard(
                viewModel = adminViewModel,
                onLogout = {
                    navController.navigate("login_screen") {
                        popUpTo(0)
                    }
                }
            )
        }
    }
}