package com.wharzc.sitemate.screens.project.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.wharzc.sitemate.model.Project
import com.wharzc.sitemate.model.SingleProjectViewModel
import com.wharzc.sitemate.ui.theme.SiteMateBlue
import com.wharzc.sitemate.ui.theme.SiteMateOrange
import kotlinx.coroutines.delay
import com.wharzc.sitemate.model.ProjectDailyViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.wharzc.sitemate.model.ProjectProgressViewModel
import com.wharzc.sitemate.model.Milestone
import androidx.compose.foundation.clickable
import com.wharzc.sitemate.model.ProjectAssignViewModel
import com.wharzc.sitemate.model.ProjectReportsViewModel

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.wharzc.sitemate.model.LoggedWork
import com.wharzc.sitemate.model.SelectableUser

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.ui.platform.LocalContext
import com.wharzc.sitemate.model.ResourceLog
import java.util.UUID

import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.layout.ContentScale

import android.app.DatePickerDialog
import java.util.Calendar

import android.app.DownloadManager
import android.os.Environment
import android.content.Context
import androidx.compose.material.icons.automirrored.filled.TrendingUp

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableIntStateOf


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SingleProjectDashboardScreen(
    projectId: String,
    navController: NavController,
    viewModel: SingleProjectViewModel = viewModel(),
    // 1. ADD THE PROGRESS VIEWMODEL HERE TO SHARE IT BETWEEN TABS
    progressViewModel: ProjectProgressViewModel = viewModel()
) {
    // 0=Home, 1=Progress, 2=Daily, 3=Reports, 4=Assign
    var selectedTab by remember { mutableIntStateOf(0) }

    // 2. FETCH EVERYTHING WHEN HOME OR PROGRESS TAB IS OPENED
    LaunchedEffect(selectedTab, projectId) {
        viewModel.fetchProjectDetails(projectId)
        viewModel.fetchProjectVitals(projectId)

        // Wait for the project data to load, then trigger the Progress math
        val currentProject = viewModel.project
        if (currentProject != null) {
            progressViewModel.loadProgress(projectId, currentProject.type)
        }
    }

    if (viewModel.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = SiteMateOrange)
        }
        return
    }

    val project = viewModel.project
    if (project == null || viewModel.errorMessage != null) {
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(project.name, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 18.sp)
                        Text(project.location, color = Color.LightGray, fontSize = 12.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SiteMateBlue)
            )
        },
        bottomBar = {
            // This Column contains the Nav Bar and the Footer together
            Column(modifier = Modifier.background(Color.White)) {
                NavigationBar(
                    containerColor = Color.White,
                    contentColor = SiteMateBlue,
                    tonalElevation = 0.dp // Set to 0 to prevent a gray tint line above footer
                ) {
                    val tabs = listOf(
                        Triple("Home", Icons.Filled.Home, 0),
                        Triple("Progress", Icons.AutoMirrored.Filled.TrendingUp, 1),
                        Triple("Daily", Icons.Filled.EventNote, 2),
                        Triple("Reports", Icons.Filled.Assessment, 3),
                        Triple("Assign", Icons.Filled.AssignmentInd, 4)
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

                // ULTRA-SLIM FOOTER (Sits safely below the Nav Bar)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(bottom = 6.dp), // Tiny padding for the very bottom edge
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
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            when (selectedTab) {
                0 -> ProjectHomeScreen(
                    project = project,
                    viewModel = viewModel,
                    // 3. PASS THE EXACT PERCENTAGE TO THE HOME TAB
                    overallProgress = progressViewModel.overallProgress,
                    onNavigateToDaily = { selectedTab = 2 }
                )
                // 4. SHARE THE VIEWMODEL SO THE PROGRESS TAB DOESN'T RE-CALCULATE
                1 -> ProjectProgressScreen(project = project, viewModel = progressViewModel)
                2 -> ProjectDailyScreen(project = project)
                3 -> ProjectReportsScreen(projectId = project.id)
                4 -> ProjectAssignScreen(projectId = project.id)
            }
        }
    }
}

// ==========================================
// 1. HOME TAB (COMMAND CENTER)
// ==========================================
@Composable
fun ProjectHomeScreen(
    project: Project,
    viewModel: SingleProjectViewModel,
    overallProgress: Float,
    onNavigateToDaily: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SiteMateBlue),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // LEFT COLUMN
                Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
                    val displayDate = java.text.SimpleDateFormat("EEEE, MMM dd, yyyy", java.util.Locale.getDefault()).format(java.util.Date())
                    Text(displayDate, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = project.name,
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    Text(
                        text = project.location,
                        color = Color.LightGray,
                        fontSize = 12.sp,
                        maxLines = 2,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }

                // RIGHT COLUMN - Weather Forecast
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.height(72.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.WbSunny,
                        contentDescription = "Sunny",
                        tint = SiteMateOrange,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "32°C",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("Project Vitals", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = SiteMateBlue)
        Spacer(modifier = Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            // PROGRESS CARD (Now mirrors the true Overall Project Status)
            VitalCard(
                modifier = Modifier.weight(1f),
                title = "Progress",
                value = "${(overallProgress * 100).toInt()}%", // <--- USES THE SHARED MATH!
                subtitle = if (overallProgress == 1f) "Completed" else "On Track",
                icon = Icons.Filled.TrendingUp,
                iconColor = Color(0xFF4CAF50)
            )

            // ON-SITE TODAY CARD
            VitalCard(
                modifier = Modifier.weight(1f),
                title = "On-Site Today",
                value = "${viewModel.totalWorkersToday}",
                subtitle = "Workers",
                icon = Icons.Filled.People,
                iconColor = SiteMateOrange
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            // PENDING INVITES CARD
            VitalCard(
                modifier = Modifier.weight(1f),
                title = "Pending Invites",
                value = "${viewModel.pendingInvites}",
                subtitle = if (viewModel.pendingInvites > 0) "Requires Action" else "All cleared",
                icon = Icons.Filled.AssignmentInd,
                iconColor = if (viewModel.pendingInvites > 0) Color(0xFFE53935) else Color.Gray
            )

            // LAST REPORT CARD
            val isNoReport = viewModel.lastReportText == "None"
            VitalCard(
                modifier = Modifier.weight(1f),
                title = "Last Report",
                value = if (isNoReport) "-" else viewModel.lastReportText,
                subtitle = if (isNoReport) "No reports yet" else "Ago",
                icon = Icons.Filled.Assessment,
                iconColor = SiteMateBlue
            )
        }

        // --- PENDING TASKS HAVE BEEN COMPLETELY REMOVED FROM HERE! ---

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onNavigateToDaily,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SiteMateBlue),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Filled.AddCircleOutline, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Log Today's Activity", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}
@Composable
fun VitalCard(modifier: Modifier = Modifier, title: String, value: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector, iconColor: Color) {
    Card(
        modifier = modifier.height(110.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Text(title, fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
            }
            Column {
                Text(value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = SiteMateBlue)
                Text(subtitle, fontSize = 10.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
fun AlertCard(message: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.3f)),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(message, color = color.copy(alpha = 0.9f), fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

// ==========================================
// 2. PROGRESS TAB
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectProgressScreen(
    project: Project,
    viewModel: ProjectProgressViewModel = viewModel()
) {
    LaunchedEffect(project.id) {
        viewModel.loadProgress(project.id, project.type)
    }

    if (viewModel.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = SiteMateOrange)
        }
        return
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val breadcrumb = listOf(project.subCategory, project.category, project.type)
            .filter { it.isNotBlank() }
            .joinToString(" > ")

        Text(
            text = breadcrumb.ifEmpty { "Unclassified Project" },
            fontSize = 12.sp,
            color = Color.Gray,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.align(Alignment.Start).padding(bottom = 8.dp)
        )

        Text("Project Status", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = SiteMateBlue, modifier = Modifier.align(Alignment.Start))
        Spacer(modifier = Modifier.height(16.dp))

        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(140.dp)) {
            CircularProgressIndicator(progress = { 1f }, modifier = Modifier.fillMaxSize(), color = Color.LightGray.copy(alpha = 0.3f), strokeWidth = 12.dp, trackColor = Color.Transparent)
            CircularProgressIndicator(progress = { viewModel.overallProgress }, modifier = Modifier.fillMaxSize(), color = SiteMateOrange, strokeWidth = 12.dp, trackColor = Color.Transparent)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "${(viewModel.overallProgress * 100).toInt()}%", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = SiteMateBlue)
                val statusColor = if (viewModel.overallProgress == 1f) Color(0xFF4CAF50) else SiteMateOrange
                Text(text = if (viewModel.overallProgress == 1f) "Completed" else "In Progress", fontSize = 12.sp, color = statusColor, fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

// THE UPDATED DEADLINE CARD
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (viewModel.hasStarted) {
                    ProgressStat("Days Elapsed", viewModel.daysElapsed)
                    ProgressStat("Days Remaining", viewModel.daysRemaining)
                } else {
                    ProgressStat("Start Date", viewModel.startDateText)
                    ProgressStat("Days until Start", viewModel.daysUntilStart)
                }

                // Target Deadline always stays on the right
                ProgressStat("Target Deadline", viewModel.targetDeadline, isHighlight = true)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text("Phase Milestones", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = SiteMateBlue, modifier = Modifier.align(Alignment.Start))
        Spacer(modifier = Modifier.height(16.dp))

        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (viewModel.milestones.isEmpty()) {
                    Text("No milestones found.", color = Color.Gray, fontSize = 14.sp)
                }

                var previousIsComplete = true // Used to enforce sequential locking

                viewModel.milestones.forEachIndexed { index, milestone ->
                    // Logic to find the active milestone
                    val isLocked = !previousIsComplete
                    val isCurrent = previousIsComplete && milestone.progress < 1f

                    MilestoneItem(
                        milestone = milestone,
                        isLast = index == viewModel.milestones.size - 1,
                        isLocked = isLocked,
                        isCurrent = isCurrent
                    )

                    if (milestone.progress < 1f) {
                        previousIsComplete = false
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun MilestoneItem(milestone: Milestone, isLast: Boolean, isLocked: Boolean, isCurrent: Boolean) {
    var expanded by remember { mutableStateOf(false) }

    // Grey out locked phases completely
    val alpha = if (isLocked) 0.4f else 1f
    // Highlight the active milestone
    val highlightBg = if (isCurrent) SiteMateBlue.copy(alpha = 0.05f) else Color.Transparent

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(highlightBg, shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().clickable(enabled = !isLocked) { expanded = !expanded },
            verticalAlignment = Alignment.Top
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(top = 4.dp)) {
                val circleColor = when {
                    milestone.progress == 1f -> Color(0xFF4CAF50)
                    milestone.progress > 0f -> SiteMateOrange
                    else -> Color.LightGray
                }
                Box(modifier = Modifier.size(16.dp).background(circleColor.copy(alpha = alpha), shape = androidx.compose.foundation.shape.CircleShape))

                if (!isLast) {
                    Box(modifier = Modifier.width(2.dp).height(40.dp).background(Color.LightGray.copy(alpha = alpha)))
                }
            }
            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(text = milestone.title, fontSize = 14.sp, fontWeight = if (isCurrent) FontWeight.ExtraBold else FontWeight.Bold, color = SiteMateBlue.copy(alpha = alpha))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "${(milestone.progress * 100).toInt()}%", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (milestone.progress == 1f) Color(0xFF4CAF50).copy(alpha = alpha) else SiteMateOrange.copy(alpha = alpha))
                        if (!isLocked) {
                            Icon(imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp).padding(start = 4.dp))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = if (isLocked) "Locked" else milestone.status, fontSize = 12.sp, color = if (isCurrent) SiteMateOrange else Color.Gray.copy(alpha = alpha), fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal)

                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(progress = { milestone.progress }, modifier = Modifier.fillMaxWidth().height(6.dp), color = if (milestone.progress == 1f) Color(0xFF4CAF50).copy(alpha = alpha) else SiteMateOrange.copy(alpha = alpha), trackColor = Color.LightGray.copy(alpha = 0.3f))

                // THE DROPDOWN WORKS LIST
                AnimatedVisibility(visible = expanded && !isLocked, enter = fadeIn() + expandVertically()) {
                    Column(modifier = Modifier.padding(top = 12.dp, bottom = 8.dp)) {
                        milestone.works.forEach { workItem ->
                            val isDone = milestone.completedWorks.contains(workItem)
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                                Icon(
                                    imageVector = if (isDone) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                                    contentDescription = null,
                                    tint = if (isDone) Color(0xFF4CAF50) else Color.LightGray,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = workItem,
                                    color = if (isDone) Color.DarkGray else Color.Gray,
                                    fontSize = 12.sp,
                                    textDecoration = if (isDone) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProgressStat(label: String, value: String, isHighlight: Boolean = false) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, fontSize = 12.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (isHighlight) SiteMateOrange else SiteMateBlue)
    }
}

// ==========================================
// 3. DAILY TAB (SITE DIARY)
// ==========================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectDailyScreen(
    project: Project,
    viewModel: ProjectDailyViewModel = viewModel()
) {
    LaunchedEffect(project.id) {
        viewModel.loadDailyLog(project.id, project.type)
    }

    val displayDate = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date())

    // --- NEW: Unified Photo Viewer State ---
    // Holds either an Android local Uri OR a Firebase String URL to view!
    var photoUriToView by remember { mutableStateOf<Any?>(null) }

    // --- State Variables ---
    var isWorkChangeMode by remember { mutableStateOf(false) }
    var workToDelete by remember { mutableStateOf<LoggedWork?>(null) }
    var showWorkModal by remember { mutableStateOf(false) }
    val context = LocalContext.current
    var tempPhotoUriForModal by remember { mutableStateOf<Uri?>(null) }

    var isManpowerChangeMode by remember { mutableStateOf(false) }
    var manpowerToDelete by remember { mutableStateOf<Pair<String, Int>?>(null) }
    var showManpowerModal by remember { mutableStateOf(false) }
    val activeManpower = viewModel.manpowerList.filter { it.second > 0 }

    // --- EQUIPMENT & MATERIALS STATES ---
    var isEquipmentChangeMode by remember { mutableStateOf(false) }
    var equipmentToDelete by remember { mutableStateOf<ResourceLog?>(null) }
    var showEquipmentModal by remember { mutableStateOf(false) }

    var isMaterialChangeMode by remember { mutableStateOf(false) }
    var materialToDelete by remember { mutableStateOf<ResourceLog?>(null) }
    var showMaterialModal by remember { mutableStateOf(false) }

    // Helper data class for the temporary dynamic inputs inside the modals
    data class ResourceInput(val id: String = UUID.randomUUID().toString(), val type: String, var name: String = "", var qty: String = "", var cost: String = "", var photoUri: Uri? = null)

    // Shared Photo Picker for dynamic items
    var activeInputIdForPhoto by remember { mutableStateOf<String?>(null) }
    var equipmentInputs by remember { mutableStateOf(listOf<ResourceInput>()) }
    var materialInputs by remember { mutableStateOf(listOf<ResourceInput>()) }

    val dynamicPhotoPickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null && activeInputIdForPhoto != null) {
            equipmentInputs = equipmentInputs.map { if (it.id == activeInputIdForPhoto) it.copy(photoUri = uri) else it }
            materialInputs = materialInputs.map { if (it.id == activeInputIdForPhoto) it.copy(photoUri = uri) else it }
        }
    }

    if (viewModel.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = SiteMateOrange)
        }
        return
    }

    var tempManpowerPhotoUri by remember { mutableStateOf<Uri?>(null) }
    val manpowerPhotoPickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) tempManpowerPhotoUri = uri
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            tempPhotoUriForModal = uri
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Today's Log", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = SiteMateBlue)
            OutlinedButton(onClick = { }) {
                Icon(Icons.Filled.DateRange, contentDescription = "Select Date", modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(displayDate)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- 1. WORK ACCOMPLISHED CARD ---
        DailyLogCard(
            title = "Work Accomplished",
            icon = Icons.Filled.Construction,
            hasItems = viewModel.loggedWorks.isNotEmpty(),
            isChangeMode = isWorkChangeMode,
            onChangeModeToggle = { isWorkChangeMode = !isWorkChangeMode },
            onAddClick = { showWorkModal = true }
        ) {
            if (viewModel.loggedWorks.isEmpty()) {
                Text("No activities added.", color = Color.Gray, fontSize = 14.sp)
            }

            viewModel.loggedWorks.forEach { work ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Text(text = "• ${work.name}", fontSize = 14.sp, color = Color.DarkGray)

                        if (work.hasPhoto) {
                            Spacer(modifier = Modifier.width(8.dp))
                            // UPDATED: Now triggers the unified image viewer
                            IconButton(onClick = { photoUriToView = work.localUri ?: work.photoUrl }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Filled.Image, contentDescription = "View Photo", tint = SiteMateBlue, modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    if (isWorkChangeMode) {
                        IconButton(onClick = { workToDelete = work }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = Color.Red, modifier = Modifier.size(18.dp))
                        }
                    }

                }
            }
        }

        // --- 2. MANPOWER PRESENT CARD ---
        DailyLogCard(
            title = "Manpower Present",
            icon = Icons.Filled.People,
            hasItems = activeManpower.isNotEmpty(),
            isChangeMode = isManpowerChangeMode,
            onChangeModeToggle = { isManpowerChangeMode = !isManpowerChangeMode },
            onAddClick = { showManpowerModal = true }
        ) {
            if (activeManpower.isEmpty()) {
                Text("No manpower added yet.", color = Color.Gray, fontSize = 14.sp)
            } else {
                activeManpower.forEach { (role, count) ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = role, fontSize = 14.sp, color = Color.DarkGray)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val headText = if (count == 1) "1 Head" else "$count Heads"
                            Text(text = headText, fontSize = 14.sp, fontWeight = FontWeight.Bold)

                            if (isManpowerChangeMode) {
                                Spacer(modifier = Modifier.width(16.dp))
                                IconButton(onClick = { manpowerToDelete = role to count }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = Color.Red, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }

                if (viewModel.hasManpowerPhoto) {
                    Divider(modifier = Modifier.padding(vertical = 8.dp), color = Color.LightGray.copy(alpha = 0.3f))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Image, contentDescription = "Photo Attached", tint = SiteMateBlue, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Workforce Photo Attached", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                        // UPDATED: Now triggers the unified image viewer
                        TextButton(onClick = { photoUriToView = viewModel.manpowerLocalUri ?: viewModel.manpowerPhotoUrl }) {
                            Text("View", color = SiteMateOrange, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // --- 3. EQUIPMENT CARD ---
        DailyLogCard(
            title = "Equipment Rented/Used",
            icon = Icons.Filled.PrecisionManufacturing,
            hasItems = viewModel.equipmentList.isNotEmpty(),
            isChangeMode = isEquipmentChangeMode,
            onChangeModeToggle = { isEquipmentChangeMode = !isEquipmentChangeMode },
            onAddClick = {
                equipmentInputs = listOf(ResourceInput(type = "Equipment"))
                showEquipmentModal = true
            }
        ) {
            if (viewModel.equipmentList.isEmpty()) Text("No equipment added.", color = Color.Gray, fontSize = 14.sp)
            viewModel.equipmentList.forEach { item ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = item.name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = SiteMateBlue)
                        Text(text = "${item.quantity} units | ₱${item.cost}", fontSize = 12.sp, color = Color.Gray)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // UPDATED: Now triggers the unified image viewer
                        if (item.hasPhoto) {
                            IconButton(onClick = { photoUriToView = item.localUri ?: item.photoUrl }, modifier = Modifier.size(28.dp)) {
                                Icon(Icons.Filled.Image, contentDescription = "View Photo", tint = SiteMateBlue, modifier = Modifier.size(20.dp))
                            }
                        }
                        if (isEquipmentChangeMode) {
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(onClick = { equipmentToDelete = item }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = Color.Red, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }

        // --- 4. MATERIALS & EXPENSES CARD ---
        DailyLogCard(
            title = "Materials & Expenses",
            icon = Icons.Filled.ReceiptLong,
            hasItems = viewModel.materialList.isNotEmpty(),
            isChangeMode = isMaterialChangeMode,
            onChangeModeToggle = { isMaterialChangeMode = !isMaterialChangeMode },
            onAddClick = {
                materialInputs = listOf(ResourceInput(type = "Material"))
                showMaterialModal = true
            }
        ) {
            if (viewModel.materialList.isEmpty()) Text("No materials added.", color = Color.Gray, fontSize = 14.sp)
            viewModel.materialList.forEach { item ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = item.name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = if(item.type == "Expense") Color(0xFFD32F2F) else SiteMateBlue)
                        Text(text = "${item.quantity} units | ₱${item.cost}", fontSize = 12.sp, color = Color.Gray)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // UPDATED: Now triggers the unified image viewer
                        if (item.hasPhoto) {
                            IconButton(onClick = { photoUriToView = item.localUri ?: item.photoUrl }, modifier = Modifier.size(28.dp)) {
                                Icon(Icons.Filled.Receipt, contentDescription = "View Receipt", tint = SiteMateBlue, modifier = Modifier.size(20.dp))
                            }
                        }
                        if (isMaterialChangeMode) {
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(onClick = { materialToDelete = item }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = Color.Red, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { viewModel.saveDailyLog(project.id) { println("Saved!") } },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SiteMateBlue),
            enabled = !viewModel.isSaving
        ) {
            if (viewModel.isSaving) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
            } else {
                Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save Daily Report")
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }

    // --- WORK ACCOMPLISHED MODAL ---
    if (showWorkModal) {
        var selectedWorksWithPhotos by remember { mutableStateOf(mapOf<String, Uri?>()) }

        val loggedWorkNames = viewModel.loggedWorks.map { it.name }
        val pendingWorksToShow = viewModel.availableWorksForModal.filter { it !in loggedWorkNames }

        AlertDialog(
            onDismissRequest = { showWorkModal = false },
            title = { Text("Log Accomplished Work", color = SiteMateBlue, fontWeight = FontWeight.Bold) },
            text = {
                if (pendingWorksToShow.isEmpty()) {
                    Text("No pending works for the current phase.", color = Color.Gray)
                } else {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text("Current Phase: ${viewModel.currentActiveMilestoneId ?: "Loading..."}", fontSize = 12.sp, color = SiteMateOrange, modifier = Modifier.padding(bottom = 8.dp))

                        LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                            items(pendingWorksToShow) { workName ->
                                val isChecked = selectedWorksWithPhotos.containsKey(workName)

                                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(
                                        checked = isChecked,
                                        onCheckedChange = { checked ->
                                            val newMap = selectedWorksWithPhotos.toMutableMap()
                                            if (checked) newMap[workName] = null
                                            else newMap.remove(workName)
                                            selectedWorksWithPhotos = newMap
                                        },
                                        colors = CheckboxDefaults.colors(checkedColor = SiteMateOrange)
                                    )
                                    Text(text = workName, fontSize = 14.sp, modifier = Modifier.weight(1f))

                                    if (isChecked) {
                                        val workPhotoUri = selectedWorksWithPhotos[workName]
                                        val hasPhoto = workPhotoUri != null

                                        IconButton(
                                            onClick = {
                                                tempPhotoUriForModal = null
                                                photoPickerLauncher.launch(
                                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                                )
                                                Toast.makeText(context, "Select a photo for '$workName'", Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (hasPhoto) Icons.Filled.CheckCircle else Icons.Filled.AddAPhoto,
                                                contentDescription = "Attach Photo",
                                                tint = if (hasPhoto) Color(0xFF4CAF50) else Color.Gray,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }

                                        if (tempPhotoUriForModal != null && !hasPhoto) {
                                            val newMap = selectedWorksWithPhotos.toMutableMap()
                                            newMap[workName] = tempPhotoUriForModal
                                            selectedWorksWithPhotos = newMap
                                            tempPhotoUriForModal = null
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        selectedWorksWithPhotos.forEach { (name, uri) ->
                            val hasPhoto = uri != null
                            // UPDATED: Now passes the localUri into the LoggedWork object!
                            viewModel.loggedWorks.add(LoggedWork(name = name, hasPhoto = hasPhoto, localUri = uri))
                        }
                        viewModel.autoSaveDraft(project.id) // <--- AUTO SAVE DRAFT
                        tempPhotoUriForModal = null
                        showWorkModal = false
                        Toast.makeText(context, "Logged ${selectedWorksWithPhotos.size} items", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SiteMateBlue)
                ) {
                    Text("Submit")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showWorkModal = false
                    tempPhotoUriForModal = null
                }) { Text("Cancel", color = Color.Gray) }
            }
        )
    }

    // --- MANPOWER ADD MODAL ---
    if (showManpowerModal) {
        val allManpowerTypes = listOf(
            "Project Engineer", "Site Engineer", "Foreman", "Carpenter", "Mason",
            "Steelman / Rebar Worker", "Electrician", "Plumber", "Laborer",
            "Equipment Operator", "Welder", "Estimator"
        )
        var manpowerInputs by remember { mutableStateOf(mapOf<String, String>()) }

        AlertDialog(
            onDismissRequest = { showManpowerModal = false },
            title = { Text("HEADCOUNTS", color = SiteMateBlue, fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { manpowerPhotoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                    ) {
                        val hasPhoto = tempManpowerPhotoUri != null || viewModel.hasManpowerPhoto
                        Icon(
                            imageVector = if (hasPhoto) Icons.Filled.CheckCircle else Icons.Filled.AddAPhoto,
                            contentDescription = "Attach Photo",
                            tint = if (hasPhoto) Color(0xFF4CAF50) else SiteMateOrange
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (hasPhoto) "Group Photo Attached" else "Attach Group Photo", color = SiteMateBlue)
                    }

                    LazyColumn(modifier = Modifier.heightIn(max = 350.dp)) {
                        items(allManpowerTypes) { role ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = role, fontSize = 14.sp, modifier = Modifier.weight(1f))

                                OutlinedTextField(
                                    value = manpowerInputs[role] ?: "",
                                    onValueChange = { input ->
                                        val cleanInput = input.filter { it.isDigit() }
                                        if (cleanInput.isEmpty()) {
                                            val newMap = manpowerInputs.toMutableMap()
                                            newMap.remove(role)
                                            manpowerInputs = newMap
                                        } else {
                                            val count = cleanInput.toIntOrNull()
                                            if (count != null && count <= 1000) {
                                                val newMap = manpowerInputs.toMutableMap()
                                                newMap[role] = count.toString()
                                                manpowerInputs = newMap
                                            }
                                        }
                                    },
                                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                                    modifier = Modifier.width(70.dp).height(50.dp),
                                    singleLine = true,
                                    textStyle = androidx.compose.ui.text.TextStyle(textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        manpowerInputs.forEach { (role, countStr) ->
                            val count = countStr.toIntOrNull() ?: 0
                            if (count > 0) {
                                val existingIndex = viewModel.manpowerList.indexOfFirst { it.first == role }
                                if (existingIndex != -1) {
                                    val current = viewModel.manpowerList[existingIndex].second
                                    viewModel.manpowerList[existingIndex] = role to (current + count)
                                } else {
                                    viewModel.manpowerList.add(role to count)
                                }
                            }
                        }
                        if (tempManpowerPhotoUri != null) {
                            viewModel.hasManpowerPhoto = true
                            viewModel.manpowerLocalUri = tempManpowerPhotoUri // UPDATED: Save local Uri
                        }
                        viewModel.autoSaveDraft(project.id) // <--- AUTO SAVE DRAFT
                        tempManpowerPhotoUri = null
                        showManpowerModal = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SiteMateBlue)
                ) {
                    Text("Submit")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showManpowerModal = false
                    tempManpowerPhotoUri = null
                }) { Text("Cancel", color = Color.Gray) }
            }
        )
    }

    // --- EQUIPMENT MODAL ---
    if (showEquipmentModal) {
        AlertDialog(
            onDismissRequest = { showEquipmentModal = false },
            title = { Text("Log Equipment", color = SiteMateBlue, fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    LazyColumn(modifier = Modifier.heightIn(max = 350.dp)) {
                        items(equipmentInputs) { input ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                IconButton(
                                    onClick = { equipmentInputs = equipmentInputs.filter { it.id != input.id } },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Filled.Close, contentDescription = "Remove", tint = Color.Red, modifier = Modifier.size(16.dp))
                                }

                                OutlinedTextField(
                                    value = input.name,
                                    onValueChange = { n -> equipmentInputs = equipmentInputs.map { if (it.id == input.id) it.copy(name = n) else it } },
                                    label = { Text("Equipment", fontSize = 10.sp) },
                                    modifier = Modifier.weight(1.4f).height(56.dp),
                                    singleLine = true,
                                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp)
                                )

                                OutlinedTextField(
                                    value = input.qty,
                                    onValueChange = { q -> val clean = q.filter { it.isDigit() }; equipmentInputs = equipmentInputs.map { if (it.id == input.id) it.copy(qty = clean) else it } },
                                    label = { Text("Qty", fontSize = 10.sp) },
                                    modifier = Modifier.weight(0.8f).height(56.dp),
                                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                                    singleLine = true,
                                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp)
                                )

                                OutlinedTextField(
                                    value = input.cost,
                                    onValueChange = { c -> val clean = c.filter { it.isDigit() }; equipmentInputs = equipmentInputs.map { if (it.id == input.id) it.copy(cost = clean) else it } },
                                    label = { Text("Cost", fontSize = 10.sp) },
                                    prefix = { Text("₱ ", fontSize = 12.sp, color = SiteMateBlue) },
                                    modifier = Modifier.weight(1.1f).height(56.dp),
                                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                                    singleLine = true,
                                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp)
                                )

                                IconButton(
                                    onClick = { activeInputIdForPhoto = input.id; dynamicPhotoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = if (input.photoUri != null) Icons.Filled.CheckCircle else Icons.Filled.AddAPhoto,
                                        contentDescription = "Attach Photo",
                                        tint = if (input.photoUri != null) Color(0xFF4CAF50) else SiteMateOrange,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                        item {
                            TextButton(onClick = { equipmentInputs = equipmentInputs + ResourceInput(type = "Equipment") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                                Icon(Icons.Filled.Add, contentDescription = null, tint = SiteMateBlue)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add Construction Equipment", color = SiteMateBlue, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        equipmentInputs.forEach { inp ->
                            val qty = inp.qty.toIntOrNull() ?: 0
                            if (inp.name.isNotBlank() && qty > 0) {
                                // UPDATED: Passes the localUri to ResourceLog
                                viewModel.equipmentList.add(ResourceLog(type = inp.type, name = inp.name, quantity = qty, cost = inp.cost.ifEmpty { "0" }, hasPhoto = inp.photoUri != null, localUri = inp.photoUri))
                            }
                        }
                        viewModel.autoSaveDraft(project.id) // <--- AUTO SAVE DRAFT
                        showEquipmentModal = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SiteMateBlue)
                ) { Text("Submit") }
            },
            dismissButton = { TextButton(onClick = { showEquipmentModal = false }) { Text("Cancel", color = Color.Gray) } }
        )
    }

    // --- MATERIALS & EXPENSES MODAL ---
    if (showMaterialModal) {
        AlertDialog(
            onDismissRequest = { showMaterialModal = false },
            title = { Text("Log Materials & Expenses", color = SiteMateBlue, fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    LazyColumn(modifier = Modifier.heightIn(max = 350.dp)) {
                        items(materialInputs) { input ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                IconButton(
                                    onClick = { materialInputs = materialInputs.filter { it.id != input.id } },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Filled.Close, contentDescription = "Remove", tint = Color.Red, modifier = Modifier.size(16.dp))
                                }

                                OutlinedTextField(
                                    value = input.name,
                                    onValueChange = { n -> materialInputs = materialInputs.map { if (it.id == input.id) it.copy(name = n) else it } },
                                    label = { Text(input.type, fontSize = 10.sp) },
                                    modifier = Modifier.weight(1.4f).height(56.dp),
                                    singleLine = true,
                                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp)
                                )

                                OutlinedTextField(
                                    value = input.qty,
                                    onValueChange = { q -> val clean = q.filter { it.isDigit() }; materialInputs = materialInputs.map { if (it.id == input.id) it.copy(qty = clean) else it } },
                                    label = { Text("Qty", fontSize = 10.sp) },
                                    modifier = Modifier.weight(0.8f).height(56.dp),
                                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                                    singleLine = true,
                                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp)
                                )

                                OutlinedTextField(
                                    value = input.cost,
                                    onValueChange = { c -> val clean = c.filter { it.isDigit() }; materialInputs = materialInputs.map { if (it.id == input.id) it.copy(cost = clean) else it } },
                                    label = { Text("Cost", fontSize = 10.sp) },
                                    prefix = { Text("₱ ", fontSize = 12.sp, color = SiteMateBlue) },
                                    modifier = Modifier.weight(1.1f).height(56.dp),
                                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                                    singleLine = true,
                                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp)
                                )

                                IconButton(
                                    onClick = { activeInputIdForPhoto = input.id; dynamicPhotoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = if (input.photoUri != null) Icons.Filled.CheckCircle else Icons.Filled.AddAPhoto,
                                        contentDescription = "Attach Receipt",
                                        tint = if (input.photoUri != null) Color(0xFF4CAF50) else SiteMateOrange,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                        item {
                            Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                                TextButton(onClick = { materialInputs = materialInputs + ResourceInput(type = "Material") }) {
                                    Icon(Icons.Filled.Add, contentDescription = null, tint = SiteMateBlue, modifier = Modifier.size(16.dp))
                                    Text(" Add Material", color = SiteMateBlue, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                                TextButton(onClick = { materialInputs = materialInputs + ResourceInput(type = "Expense") }) {
                                    Icon(Icons.Filled.Add, contentDescription = null, tint = Color(0xFFD32F2F), modifier = Modifier.size(16.dp))
                                    Text(" Add Expense", color = Color(0xFFD32F2F), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        materialInputs.forEach { inp ->
                            val qty = inp.qty.toIntOrNull() ?: 0
                            if (inp.name.isNotBlank() && qty > 0) {
                                // UPDATED: Passes the localUri to ResourceLog
                                viewModel.materialList.add(ResourceLog(type = inp.type, name = inp.name, quantity = qty, cost = inp.cost.ifEmpty { "0" }, hasPhoto = inp.photoUri != null, localUri = inp.photoUri))
                            }
                        }
                        viewModel.autoSaveDraft(project.id) // <--- AUTO SAVE DRAFT
                        showMaterialModal = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SiteMateBlue)
                ) { Text("Submit") }
            },
            dismissButton = { TextButton(onClick = { showMaterialModal = false }) { Text("Cancel", color = Color.Gray) } }
        )
    }

    // --- SHARED DELETE WARNINGS ---
    if (workToDelete != null) {
        AlertDialog(
            onDismissRequest = { workToDelete = null },
            title = { Text("Delete Log?", color = Color.Red, fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete '${workToDelete?.name}'? This action cannot be recovered.", color = Color.DarkGray) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.loggedWorks.remove(workToDelete)
                        workToDelete = null
                        if (viewModel.loggedWorks.isEmpty()) isWorkChangeMode = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Delete Permanently")
                }
            },
            dismissButton = {
                TextButton(onClick = { workToDelete = null }) { Text("Cancel", color = Color.Gray) }
            }
        )
    }

    if (manpowerToDelete != null) {
        AlertDialog(
            onDismissRequest = { manpowerToDelete = null },
            title = { Text("Delete Manpower?", color = Color.Red, fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to remove '${manpowerToDelete?.first}' from today's log? This action cannot be recovered.", color = Color.DarkGray) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.manpowerList.remove(manpowerToDelete)
                        manpowerToDelete = null
                        if (viewModel.manpowerList.isEmpty()) isManpowerChangeMode = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Delete Permanently")
                }
            },
            dismissButton = {
                TextButton(onClick = { manpowerToDelete = null }) { Text("Cancel", color = Color.Gray) }
            }
        )
    }

    if (equipmentToDelete != null) {
        AlertDialog(onDismissRequest = { equipmentToDelete = null }, title = { Text("Delete Equipment?", color = Color.Red, fontWeight = FontWeight.Bold) }, text = { Text("Remove '${equipmentToDelete?.name}' from log?") }, confirmButton = { Button(onClick = { viewModel.equipmentList.remove(equipmentToDelete); equipmentToDelete = null; if(viewModel.equipmentList.isEmpty()) isEquipmentChangeMode = false }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) { Text("Delete") } }, dismissButton = { TextButton(onClick = { equipmentToDelete = null }) { Text("Cancel", color = Color.Gray) } })
    }
    if (materialToDelete != null) {
        AlertDialog(onDismissRequest = { materialToDelete = null }, title = { Text("Delete Material/Expense?", color = Color.Red, fontWeight = FontWeight.Bold) }, text = { Text("Remove '${materialToDelete?.name}' from log?") }, confirmButton = { Button(onClick = { viewModel.materialList.remove(materialToDelete); materialToDelete = null; if(viewModel.materialList.isEmpty()) isMaterialChangeMode = false }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) { Text("Delete") } }, dismissButton = { TextButton(onClick = { materialToDelete = null }) { Text("Cancel", color = Color.Gray) } })
    }

    // --- NEW: REAL IMAGE VIEWER DIALOG ---
    if (photoUriToView != null) {
        AlertDialog(
            onDismissRequest = { photoUriToView = null },
            title = { Text("Attached Image", color = SiteMateBlue, fontWeight = FontWeight.Bold) },
            text = {
                Box(
                    modifier = Modifier.fillMaxWidth().height(350.dp).background(Color.Black, shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    // Coil AsyncImage seamlessly loads BOTH local device URIs and remote Firebase URLs!
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(photoUriToView)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Attached Document",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }
            },
            confirmButton = {
                Button(onClick = { photoUriToView = null }, colors = ButtonDefaults.buttonColors(containerColor = SiteMateBlue)) {
                    Text("Close")
                }
            }
        )
    }
}
// THE UPDATED CARD
@Composable
fun DailyLogCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    hasItems: Boolean,
    isChangeMode: Boolean,
    onChangeModeToggle: () -> Unit,
    onAddClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(icon, contentDescription = null, tint = SiteMateOrange, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = title, fontWeight = FontWeight.Bold, color = SiteMateBlue)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (hasItems) {
                        TextButton(onClick = onChangeModeToggle) {
                            Text(
                                text = if (isChangeMode) "X" else "Change",
                                color = if (isChangeMode) Color.Red else Color.Gray,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    if (!isChangeMode) {
                        IconButton(onClick = onAddClick, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Filled.Add, contentDescription = "Add", tint = SiteMateOrange)
                        }
                    }
                }
            }
            Divider(modifier = Modifier.padding(vertical = 8.dp), color = Color.LightGray.copy(alpha = 0.5f))
            content()
        }
    }
}



// ==========================================
// 4. REPORTS TAB
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectReportsScreen(
    projectId: String,
    viewModel: ProjectReportsViewModel = viewModel()
) {
    val context = LocalContext.current

    LaunchedEffect(projectId) {
        viewModel.loadReports(projectId)
    }

    var selectedCalendar by remember { mutableStateOf(Calendar.getInstance()) }

    val displayFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val displayDate = displayFormatter.format(selectedCalendar.time)

    val dbFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val dbDateString = dbFormatter.format(selectedCalendar.time)

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val newCalendar = Calendar.getInstance()
            newCalendar.set(year, month, dayOfMonth)
            selectedCalendar = newCalendar
        },
        selectedCalendar.get(Calendar.YEAR),
        selectedCalendar.get(Calendar.MONTH),
        selectedCalendar.get(Calendar.DAY_OF_MONTH)
    )

    if (viewModel.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = SiteMateOrange)
        }
        return
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
        Text("Generate New Report", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = SiteMateBlue)
        Spacer(modifier = Modifier.height(16.dp))

        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {

                OutlinedTextField(
                    value = "Daily Accomplishment Report",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Report Type") },
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = SiteMateBlue,
                        disabledBorderColor = Color.LightGray,
                        disabledLabelColor = Color.Gray
                    ),
                    enabled = false,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = { datePickerDialog.show() },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
                ) {
                    Icon(Icons.Filled.DateRange, contentDescription = null, tint = SiteMateOrange)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = displayDate, color = SiteMateBlue, fontSize = 16.sp)
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        viewModel.generateReport(
                            context = context,
                            projectId = projectId,
                            type = "Daily Accomplishment Report",
                            dateString = dbDateString
                        ) { success ->
                            if (success) {
                                Toast.makeText(context, "PDF saved to your Downloads folder!", Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(context, "Failed to generate report. Please try again.", Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SiteMateBlue),
                    enabled = !viewModel.isGenerating
                ) {
                    if (viewModel.isGenerating) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Compiling PDF...")
                    } else {
                        Icon(Icons.Filled.PictureAsPdf, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Generate PDF")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text("Report Archive", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = SiteMateBlue)
        Spacer(modifier = Modifier.height(16.dp))

        if (viewModel.reports.isEmpty()) {
            Text("No reports generated yet.", color = Color.Gray, fontSize = 14.sp)
        } else {
            viewModel.reports.forEach { report ->
                ReportArchiveCard(
                    title = report.title,
                    date = report.date,
                    type = report.type,
                    onDownload = {
                        // DYNAMIC GENERATION: Because we aren't using the cloud, clicking download simply
                        // triggers the exact same function to recreate the PDF locally!
                        viewModel.generateReport(
                            context = context,
                            projectId = projectId,
                            type = report.type,
                            dateString = report.id // Since we made the ID the date string!
                        ) { success ->
                            if (success) {
                                Toast.makeText(context, "Old report re-downloaded successfully!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    onShare = { /* Future PDF Share logic */ }
                )
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun ReportToggleRow(label: String, isChecked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(text = label, fontSize = 14.sp, color = Color.DarkGray)
        Switch(checked = isChecked, onCheckedChange = onCheckedChange, colors = SwitchDefaults.colors(checkedThumbColor = SiteMateOrange, checkedTrackColor = SiteMateOrange.copy(alpha = 0.5f)), modifier = Modifier.scale(0.8f))
    }
}

@Composable
fun ReportArchiveCard(title: String, date: String, type: String, onDownload: () -> Unit, onShare: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(modifier = Modifier.size(40.dp).background(Color(0xFFE57373).copy(alpha = 0.2f), shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.PictureAsPdf, contentDescription = null, tint = Color(0xFFD32F2F))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(text = title, fontWeight = FontWeight.Bold, color = SiteMateBlue, fontSize = 14.sp)
                    Text(text = type, color = Color.Gray, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "Generated: $date", color = SiteMateOrange, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                }
            }
            Row {
                IconButton(onClick = onDownload, modifier = Modifier.size(36.dp)) { Icon(Icons.Filled.Download, contentDescription = "Download", tint = SiteMateBlue) }
                IconButton(onClick = onShare, modifier = Modifier.size(36.dp)) { Icon(Icons.Filled.Share, contentDescription = "Share", tint = SiteMateBlue) }
            }
        }
    }
}

// ==========================================
// 5. ASSIGN TAB
// ==========================================
@Composable
fun ProjectAssignScreen(
    projectId: String,
    viewModel: ProjectAssignViewModel = viewModel()
) {
    // Fetch team AND available users from the database as soon as the tab opens
    LaunchedEffect(projectId) {
        viewModel.loadTeam(projectId)
        viewModel.fetchAvailableUsers()
    }

    // Filter the master list into our two categories
    val clients = viewModel.teamMembers.filter { it.type == "Client" }

    var isClientDeleteMode by remember { mutableStateOf(false) }

    // Triggers for the new multi-select modals
    var showClientModal by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {

        Spacer(modifier = Modifier.height(32.dp))

        // --- CLIENT SECTION ---
        SectionHeader(
            title = "Client Access",
            isDeleteMode = isClientDeleteMode,
            onAddClick = { showClientModal = true },
            onDeleteToggle = { isClientDeleteMode = !isClientDeleteMode }
        )

        if (clients.isEmpty()) {
            Text("No clients invited.", color = Color.Gray, fontSize = 14.sp, modifier = Modifier.padding(bottom = 16.dp))
        } else {
            clients.forEach { member ->
                MemberCard(
                    member = member,
                    isDeleteMode = isClientDeleteMode,
                    isClient = true,
                    // Pass all required parameters to completely remove them from the project
                    onDeleteClick = { viewModel.removeMember(projectId, member.id, member.email, "Client") },
                    onPermissionToggle = { isChecked ->
                        viewModel.toggleBudgetPermission(projectId, member.id, isChecked)
                    }
                )
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
    }


    if (showClientModal) {
        UserSelectionModal(
            title = "Invite Client / Stakeholder",
            users = viewModel.availableClients,
            isLoading = viewModel.isLoading,
            onDismiss = { showClientModal = false },
            onAssign = { selectedUsers ->
                viewModel.assignSelectedUsers(projectId, "Client", selectedUsers) {
                    showClientModal = false
                }
            }
        )
    }
}

@Composable
fun SectionHeader(title: String, isDeleteMode: Boolean, onAddClick: () -> Unit, onDeleteToggle: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = SiteMateBlue)
        Row {
            IconButton(onClick = onAddClick) {
                Icon(Icons.Filled.Add, contentDescription = "Add", tint = SiteMateOrange)
            }
            IconButton(onClick = onDeleteToggle) {
                Icon(imageVector = if (isDeleteMode) Icons.Filled.Close else Icons.Filled.Delete,
                    contentDescription = "Toggle Delete Mode",
                    tint = if (isDeleteMode) Color.Red else Color.Gray)
            }
        }
    }
}

@Composable
fun MemberCard(
    member: com.wharzc.sitemate.model.TeamMember,
    isDeleteMode: Boolean,
    isClient: Boolean = false,
    onDeleteClick: () -> Unit,
    onPermissionToggle: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier.size(40.dp).background(SiteMateBlue.copy(alpha = 0.1f), shape = androidx.compose.foundation.shape.CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    // Fallback to "?" if name is empty to prevent crashes
                    Text(text = member.name.takeIf { it.isNotEmpty() }?.take(1)?.uppercase() ?: "?", color = SiteMateBlue, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(text = member.name, fontWeight = FontWeight.Bold, color = SiteMateBlue, fontSize = 16.sp)
                    // Displaying the email instead of the manual role text for better identification
                    Text(text = member.email.ifEmpty { member.role }, color = Color.Gray, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))

                    val badgeColor = if (member.status == "Active") Color(0xFF4CAF50) else SiteMateOrange
                    Box(modifier = Modifier.background(badgeColor.copy(alpha = 0.1f), shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp))) {
                        Text(text = member.status, color = badgeColor, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                }
            }
            if (isDeleteMode) {
                IconButton(onClick = onDeleteClick) {
                    Icon(Icons.Filled.Delete, contentDescription = "Remove", tint = Color.Red)
                }
            }
        }
    }
}

@Composable
fun UserSelectionModal(
    title: String,
    users: List<SelectableUser>,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onAssign: (List<SelectableUser>) -> Unit
) {
    var selectedUsers by remember { mutableStateOf(setOf<SelectableUser>()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, color = SiteMateBlue, fontWeight = FontWeight.Bold) },
        text = {
            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = SiteMateOrange)
                }
            } else if (users.isEmpty()) {
                Text("No registered users found for this role.", color = Color.Gray)
            } else {
                LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                    items(users) { user ->
                        val isSelected = selectedUsers.contains(user)
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    selectedUsers = if (isSelected) selectedUsers - user else selectedUsers + user
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) SiteMateBlue.copy(alpha = 0.1f) else Color.White
                            ),
                            border = BorderStroke(1.dp, if (isSelected) SiteMateBlue else Color.LightGray)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(user.name, fontWeight = FontWeight.Bold, color = SiteMateBlue)
                                Text(user.email, fontSize = 12.sp, color = Color.Gray)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onAssign(selectedUsers.toList()) },
                colors = ButtonDefaults.buttonColors(containerColor = SiteMateOrange)
            ) {
                Text("Assign Selected", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = Color.Gray) }
        }
    )
}