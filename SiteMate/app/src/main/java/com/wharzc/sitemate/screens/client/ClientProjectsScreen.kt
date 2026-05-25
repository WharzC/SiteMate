package com.wharzc.sitemate.screens.client

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

// --- STYLING IMPORTS ---
import com.wharzc.sitemate.ui.theme.SiteMateBlue
import com.wharzc.sitemate.ui.theme.SiteMateOrange

// --- VIEWMODEL IMPORTS ---
import com.wharzc.sitemate.model.SingleProjectViewModel
import com.wharzc.sitemate.model.ProjectProgressViewModel
import com.wharzc.sitemate.model.ProjectReportsViewModel

// --- ENGINEER SCREEN IMPORTS (So the Client can mirror them!) ---
import com.wharzc.sitemate.screens.project.dashboard.ProjectProgressScreen
import com.wharzc.sitemate.screens.project.dashboard.ReportArchiveCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientProjectsScreen(
    projectId: String,
    navController: NavController,
    viewModel: SingleProjectViewModel = viewModel(),
    progressViewModel: ProjectProgressViewModel = viewModel(),
    reportsViewModel: ProjectReportsViewModel = viewModel()
) {
    // 0 = Progress, 1 = Reports
    var selectedTab by remember { mutableIntStateOf(0) }

    // 1. Fetch the project details immediately when the screen opens
    LaunchedEffect(projectId) {
        viewModel.fetchProjectDetails(projectId)
    }

    // 2. Wait for the project to load, THEN dynamically fetch the Progress or Reports data!
    val currentProject = viewModel.project
    LaunchedEffect(currentProject, selectedTab) {
        if (currentProject != null) {
            if (selectedTab == 0) {
                progressViewModel.loadProgress(projectId, currentProject.type)
            } else if (selectedTab == 1) {
                reportsViewModel.loadReports(projectId)
            }
        }
    }

    // 3. Handle Loading State
    if (viewModel.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = SiteMateOrange)
        }
        return
    }

    // 4. Handle Missing/Error State
    if (currentProject == null || viewModel.errorMessage != null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(viewModel.errorMessage ?: "Project not found.", color = Color.Gray)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { navController.popBackStack() }, colors = ButtonDefaults.buttonColors(containerColor = SiteMateBlue)) {
                    Text("Go Back")
                }
            }
        }
        return
    }

    // 5. Build the UI
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(currentProject.name, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 18.sp)
                        Text(currentProject.location, color = Color.LightGray, fontSize = 12.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SiteMateBlue),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                }
            )
        },
        bottomBar = {
            Column(modifier = Modifier.background(Color.White)) {
                NavigationBar(
                    containerColor = Color.White,
                    contentColor = SiteMateBlue,
                    tonalElevation = 0.dp
                ) {
                    val tabs = listOf(
                        Triple("Progress", Icons.AutoMirrored.Filled.TrendingUp, 0),
                        Triple("Reports", Icons.Filled.Assessment, 1)
                    )

                    tabs.forEach { (title, icon, index) ->
                        NavigationBarItem(
                            icon = { Icon(icon, contentDescription = title) },
                            label = { Text(title, fontSize = 10.sp) },
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = SiteMateOrange,
                                selectedTextColor = SiteMateOrange,
                                unselectedIconColor = Color.Gray,
                                unselectedTextColor = Color.Gray,
                                indicatorColor = Color.Transparent
                            )
                        )
                    }
                }

                // ULTRA-SLIM FOOTER
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(bottom = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "SITEMATE @ 2026",
                        fontSize = 8.sp,
                        color = Color.LightGray,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            when (selectedTab) {
                // Completely mirrors the dynamic progress screen!
                0 -> ProjectProgressScreen(project = currentProject, viewModel = progressViewModel)

                // Uses the strict Client Archive screen created below!
                1 -> ClientProjectReportsScreen(projectId = currentProject.id, viewModel = reportsViewModel)
            }
        }
    }
}

// ==========================================
// CLIENT-ONLY REPORTS ARCHIVE
// ==========================================
@Composable
fun ClientProjectReportsScreen(
    projectId: String,
    viewModel: ProjectReportsViewModel
) {
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
        Text("Project Reports Archive", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = SiteMateBlue)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Download official PDF reports generated by the site engineer.", fontSize = 14.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(24.dp))

        if (viewModel.isLoading || viewModel.isGenerating) {
            Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = SiteMateOrange)
            }
        } else if (viewModel.reports.isEmpty()) {
            Text("No reports have been generated for this project yet.", color = Color.Gray, fontSize = 14.sp)
        } else {
            viewModel.reports.forEach { report ->
                // Mirrors the elegant Report UI Cards from the Engineer side!
                ReportArchiveCard(
                    title = report.title,
                    date = report.date,
                    type = report.type,
                    isDeleteMode = false,
                    onDownload = {
                        Toast.makeText(context, "Downloading PDF...", Toast.LENGTH_SHORT).show()
                        when (report.type) {
                            "Overall Progress" -> {
                                viewModel.generateOverallProgressReport(
                                    context = context,
                                    projectId = projectId
                                ) { success ->
                                    if (success) {
                                        Toast.makeText(context, "Overall report saved to Downloads", Toast.LENGTH_LONG).show()
                                    } else {
                                        Toast.makeText(context, "Download failed", Toast.LENGTH_LONG).show()
                                    }
                                }
                            }
                            else -> {
                                viewModel.generateReport(
                                    context = context,
                                    projectId = projectId,
                                    type = report.type,
                                    dateString = report.id
                                ) { success ->
                                    if (success) {
                                        Toast.makeText(context, "PDF saved to Downloads", Toast.LENGTH_LONG).show()
                                    } else {
                                        Toast.makeText(context, "Download failed", Toast.LENGTH_LONG).show()
                                    }
                                }
                            }
                        }
                    },
                    onDelete = {}
                )
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}