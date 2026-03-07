package com.wharzc.sitemate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.wharzc.sitemate.model.AuthViewModel

// --- 1. IMPORT MODELS ---
import com.wharzc.sitemate.model.ProjectCreationViewModel

// --- 2. IMPORT GENERAL SCREENS ---
import com.wharzc.sitemate.screens.EngineerDashboardScreen
import com.wharzc.sitemate.screens.EngineerWelcomeScreen
import com.wharzc.sitemate.screens.auth.LoginScreen
import com.wharzc.sitemate.screens.auth.RoleSelectionScreen
import com.wharzc.sitemate.screens.auth.SignUpScreen
import com.wharzc.sitemate.screens.auth.SplashScreen

// --- 3. IMPORT PROJECT WIZARD SCREENS (FROM NEW FOLDER) ---
import com.wharzc.sitemate.screens.project.CreateProjectScreen
import com.wharzc.sitemate.screens.project.ProfileScreen
import com.wharzc.sitemate.screens.project.ProjectDetailsScreen
import com.wharzc.sitemate.screens.project.ProjectLocationScreen
import com.wharzc.sitemate.screens.project.ProjectNameScreen
import com.wharzc.sitemate.screens.project.ProjectReviewScreen
import com.wharzc.sitemate.screens.project.ProjectUploadScreen
import com.wharzc.sitemate.screens.project.ProjectsScreen
import com.wharzc.sitemate.screens.project.dashboard.SingleProjectDashboardScreen
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.wharzc.sitemate.screens.client.ClientDashboardScreen
import com.wharzc.sitemate.screens.client.ClientWelcomeScreen
import com.wharzc.sitemate.screens.client.ClientProjectsScreen


import com.wharzc.sitemate.ui.theme.SiteMateTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SiteMateTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    val navController = rememberNavController()
                    val viewModel = remember { ProjectCreationViewModel() }

                    NavHost(navController = navController, startDestination = "splash") {



                        // --- SPLASH & AUTH ---
                        composable("splash") {
                            SplashScreen(onTimeout = {
                                navController.navigate("login")
                                { popUpTo("splash") { inclusive = true } }
                            })
                        }

                        composable(route = "login") { LoginScreen(navController) }
                        composable(route = "signup") { SignUpScreen(navController) }

                        // 2. Changed "project_list" to "projects" to match our Login gatekeeper!
                        composable(route = "projects") { ProjectsScreen(navController) }
                        composable(route = "profile") { ProfileScreen(navController) }





                        composable(route = "role_selection") {
                            val authViewModel: AuthViewModel = viewModel()

                            RoleSelectionScreen(
                                onRoleSelected = { selectedRole, stopLoadingCallback ->
                                    // Calling our updated ViewModel function
                                    authViewModel.saveUserRole(selectedRole) { success, errorMessage ->
                                        if (success) {
                                            when (selectedRole) {
                                                "Engineer" -> navController.navigate("engineer_welcome") {
                                                    popUpTo("role_selection") { inclusive = true }
                                                }
                                                "Foreman" -> navController.navigate("foreman_welcome") {
                                                    popUpTo("role_selection") { inclusive = true }
                                                }
                                                "Client" -> navController.navigate("client_welcome") {
                                                    popUpTo("role_selection") { inclusive = true }
                                                }
                                            }
                                        } else {
                                            // THIS FREES THE UI AND SHOWS THE ERROR!
                                            stopLoadingCallback(false, errorMessage)
                                        }
                                    }
                                }
                            )
                        }

                        

                        // --- DASHBOARDS ---
                        composable("engineer_dashboard") {
                            EngineerDashboardScreen(navController = navController)
                        }

                        composable("engineer_welcome") {
                            EngineerWelcomeScreen(
                                onWelcomeComplete = {
                                    // FIX: Route to the wrapper, NOT the raw content!
                                    navController.navigate("engineer_dashboard") {
                                        popUpTo("engineer_welcome") { inclusive = true }
                                    }
                                }
                            )
                        }


                        composable("client_welcome") {
                            ClientWelcomeScreen(
                                onWelcomeComplete = {
                                    navController.navigate("client_dashboard") {
                                        popUpTo("client_welcome") { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable("client_dashboard") {
                            ClientDashboardScreen(navController)
                        }

                        // Find this block around line 158
                        composable("client_project/{projectId}") { backStackEntry ->
                            val projectId = backStackEntry.arguments?.getString("projectId")
                            if (projectId != null) {
                                // CHANGE THIS LINE from ClientProjectsScreen to ClientProjectDetailScreen
                                ClientProjectsScreen(
                                    projectId = projectId,
                                    navController = navController
                                )
                            }
                        }

                        composable(
                            route = "project_dashboard/{projectId}",
                            arguments = listOf(navArgument("projectId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            // Extract the ID of the project we just clicked
                            val projectId = backStackEntry.arguments?.getString("projectId") ?: ""

                            // Send them to the dashboard screen with that ID
                            SingleProjectDashboardScreen(navController = navController, projectId = projectId)
                        }

                        // --- SINGLE PROJECT DASHBOARD ---
                        composable("single_project/{projectId}") { backStackEntry ->
                            // Extract the ID from the route
                            val projectId = backStackEntry.arguments?.getString("projectId")

                            if (projectId != null) {
                                SingleProjectDashboardScreen(
                                    projectId = projectId,
                                    navController = navController
                                )
                            }
                        }

                        // --- PROJECT WIZARD (CONNECTED TO VIEWMODEL) ---
                        composable("project_name") {
                            ProjectNameScreen(
                                viewModel = viewModel,
                                onProceed = { navController.navigate("create_project") },
                                onBack = { navController.popBackStack("engineer_dashboard", inclusive = false) }
                            )
                        }

                        composable("create_project") {
                            CreateProjectScreen(
                                viewModel = viewModel,
                                onProceed = { navController.navigate("project_details") },
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable("project_details") {
                            ProjectDetailsScreen(
                                viewModel = viewModel,
                                onProceed = { navController.navigate("project_location") },
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable("project_location") {
                            ProjectLocationScreen(
                                viewModel = viewModel,
                                onProceed = { navController.navigate("project_upload") },
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable("project_upload") {
                            ProjectUploadScreen(
                                viewModel = viewModel,
                                onProceed = { navController.navigate("project_review") },
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable("project_review") {
                            ProjectReviewScreen(
                                viewModel = viewModel,
                                onConfirmSave = {
                                    navController.navigate("engineer_dashboard") {
                                        popUpTo("engineer_dashboard") { inclusive = true }
                                    }
                                },
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }


                }
            }
        }
    }
}