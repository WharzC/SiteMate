package com.wharzc.sitemate.screens.project.dashboard

import android.R.attr.top
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.automirrored.filled.TrendingUp

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import com.wharzc.sitemate.model.ReportDoc

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign


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
            Column(modifier = Modifier.background(Color.White)) {
                NavigationBar(
                    // FIX: Removed the forced 68.dp height so the text doesn't get cut off!
                    containerColor = Color.White,
                    contentColor = SiteMateBlue,
                    tonalElevation = 0.dp
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
                                selectedIconColor = SiteMateOrange, selectedTextColor = SiteMateOrange,
                                unselectedIconColor = Color.Gray, unselectedTextColor = Color.Gray, indicatorColor = Color.Transparent
                            )
                        )
                    }
                }

                // --- ULTRA-SLIM FOOTER ---
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        // Kept the padding tight so it still sits perfectly below the icons!
                        .padding(top = 2.dp, bottom = 12.dp),
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
                    val displayDate = remember {java.text.SimpleDateFormat("EEEE, MMM dd, yyyy", Locale.getDefault()).format(java.util.Date()) }
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

                    // --- NEW: DYNAMICALLY HIDE PROVINCE & REGION ---
                    val locationParts = project.location.split(",")
                    val displayLocation = if (locationParts.size >= 4) {
                        // Drops the last two items (e.g., Oriental Mindoro and Region 4-B)
                        locationParts.dropLast(2).joinToString(",").trim()
                    } else {
                        project.location
                    }

                    Text(
                        text = displayLocation,
                        color = Color.LightGray,
                        fontSize = 12.sp,
                        maxLines = 2,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }

                // RIGHT COLUMN - Weather Forecast (DYNAMIC)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.height(72.dp)
                ) {
                    // Map tailored specifically for the Philippine / Oriental Mindoro climate
                    val weatherData = when (viewModel.weatherCode) {
                        0 -> Pair(Icons.Filled.WbSunny, SiteMateOrange) // Clear & Sunny
                        1, 2, 3 -> Pair(Icons.Filled.Cloud, Color.LightGray) // Partly Cloudy to Overcast
                        45 -> Pair(Icons.Filled.Cloud, Color.Gray) // Foggy (e.g., morning mountain fog)
                        51, 53, 55, 61, 63, 65, 80, 81, 82 -> Pair(Icons.Filled.WaterDrop, Color(0xFF4FC3F7)) // Drizzle, Showers, and Monsoon Rain
                        95, 96, 99 -> Pair(Icons.Filled.Bolt, Color(0xFFFFD54F)) // Thunderstorms & Typhoon conditions
                        -1 -> Pair(Icons.Filled.CloudOff, Color.DarkGray) // OFFLINE / API ERROR ICON
                        else -> Pair(Icons.Filled.Cloud, Color.LightGray) // Fallback for weird data
                    }

                    Icon(
                        imageVector = weatherData.first,
                        contentDescription = "Weather",
                        tint = weatherData.second,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = viewModel.weatherTemp, // DYNAMIC TEMPERATURE
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
enum class MilestoneFilter {
    ALL,
    ACCOMPLISHED,
    PENDING
}

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

    var filter by remember { mutableStateOf(MilestoneFilter.ALL) }
    var expandedFilterMenu by remember { mutableStateOf(false) }

    val allMilestones = viewModel.milestones
    val filteredMilestones = remember(allMilestones, filter) {
        when (filter) {
            MilestoneFilter.ALL -> allMilestones
            MilestoneFilter.ACCOMPLISHED -> allMilestones.filter { it.progress == 1f }
            MilestoneFilter.PENDING -> allMilestones.filter { it.progress < 1f }
        }
    }

    val lockStatusMap = remember(allMilestones) {
        val map = mutableMapOf<String, Boolean>()
        var previousComplete = true
        allMilestones.forEach { milestone ->
            val isLocked = !previousComplete
            map[milestone.id] = isLocked
            if (milestone.progress < 1f) previousComplete = false
        }
        map
    }

    val currentMilestoneId = remember(allMilestones) {
        allMilestones.find { it.progress < 1f }?.id
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Breadcrumb
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

        Text(
            "Project Status",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = SiteMateBlue,
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(16.dp))

        // --- PROGRESS WHEEL (restored) ---
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(140.dp)
        ) {
            CircularProgressIndicator(
                progress = { 1f },
                modifier = Modifier.fillMaxSize(),
                color = Color.LightGray.copy(alpha = 0.3f),
                strokeWidth = 12.dp,
                trackColor = Color.Transparent
            )
            CircularProgressIndicator(
                progress = { viewModel.overallProgress },
                modifier = Modifier.fillMaxSize(),
                color = SiteMateOrange,
                strokeWidth = 12.dp,
                trackColor = Color.Transparent
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${(viewModel.overallProgress * 100).toInt()}%",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = SiteMateBlue
                )
                val statusColor = if (viewModel.overallProgress == 1f) Color(0xFF4CAF50) else SiteMateOrange
                Text(
                    text = if (viewModel.overallProgress == 1f) "Completed" else "In Progress",
                    fontSize = 12.sp,
                    color = statusColor,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))

        // Deadline Card
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
                ProgressStat("Target Deadline", viewModel.targetDeadline, isHighlight = true)
            }
        }
        Spacer(modifier = Modifier.height(32.dp))

        // Phase Milestones header with filter dropdown
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Phase Milestones",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = SiteMateBlue
            )
            Box {
                OutlinedButton(
                    onClick = { expandedFilterMenu = true },
                    modifier = Modifier.height(32.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                ) {
                    Text(
                        text = when (filter) {
                            MilestoneFilter.ALL -> "Show All"
                            MilestoneFilter.ACCOMPLISHED -> "Accomplished"
                            MilestoneFilter.PENDING -> "Pending"
                        },
                        fontSize = 12.sp
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
                DropdownMenu(
                    expanded = expandedFilterMenu,
                    onDismissRequest = { expandedFilterMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Show All") },
                        onClick = { filter = MilestoneFilter.ALL; expandedFilterMenu = false }
                    )
                    DropdownMenuItem(
                        text = { Text("Show Accomplished") },
                        onClick = { filter = MilestoneFilter.ACCOMPLISHED; expandedFilterMenu = false }
                    )
                    DropdownMenuItem(
                        text = { Text("Show Pending") },
                        onClick = { filter = MilestoneFilter.PENDING; expandedFilterMenu = false }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Milestones list
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (filteredMilestones.isEmpty()) {
                    Text("No milestones to show.", color = Color.Gray, fontSize = 14.sp)
                }
                filteredMilestones.forEachIndexed { index, milestone ->
                    val isLocked = lockStatusMap[milestone.id] ?: false
                    val isCurrent = milestone.id == currentMilestoneId
                    val isLast = index == filteredMilestones.lastIndex
                    MilestoneItem(
                        milestone = milestone,
                        isLast = isLast,
                        isLocked = isLocked,
                        isCurrent = isCurrent
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun MilestoneItem(milestone: Milestone, isLast: Boolean, isLocked: Boolean, isCurrent: Boolean) {
    var expanded by remember { mutableStateOf(false) }
    val alpha = if (isLocked) 0.4f else 1f
    val highlightBg = if (isCurrent) SiteMateBlue.copy(alpha = 0.05f) else Color.Transparent

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(highlightBg, RoundedCornerShape(8.dp))
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
                Box(modifier = Modifier.size(16.dp).background(circleColor.copy(alpha = alpha), CircleShape))
                if (!isLast) {
                    Box(modifier = Modifier.width(2.dp).height(40.dp).background(Color.LightGray.copy(alpha = alpha)))
                }
            }
            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = milestone.title,
                        fontSize = 14.sp,
                        fontWeight = if (isCurrent) FontWeight.ExtraBold else FontWeight.Bold,
                        color = SiteMateBlue.copy(alpha = alpha)
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${(milestone.progress * 100).toInt()}%",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (milestone.progress == 1f) Color(0xFF4CAF50).copy(alpha = alpha) else SiteMateOrange.copy(alpha = alpha)
                        )
                        if (!isLocked) {
                            Icon(
                                imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(20.dp).padding(start = 4.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isLocked) "Locked" else milestone.status,
                    fontSize = 12.sp,
                    color = if (isCurrent) SiteMateOrange else Color.Gray.copy(alpha = alpha),
                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                )

                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { milestone.progress },
                    modifier = Modifier.fillMaxWidth().height(6.dp),
                    color = if (milestone.progress == 1f) Color(0xFF4CAF50).copy(alpha = alpha) else SiteMateOrange.copy(alpha = alpha),
                    trackColor = Color.LightGray.copy(alpha = 0.3f)
                )

                // ---- Expandable Works Section ----
                AnimatedVisibility(visible = expanded && !isLocked, enter = fadeIn() + expandVertically()) {
                    Column(modifier = Modifier.padding(top = 12.dp, bottom = 8.dp)) {
                        if (milestone.categorizedWorks != null) {
                            // DEMOLITION: grouped activities
                            milestone.categorizedWorks.forEach { (category, workList) ->
                                Text(
                                    text = category,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = SiteMateBlue.copy(alpha = alpha),
                                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                                )
                                workList.forEach { workItem ->
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
                                            textDecoration = if (isDone) TextDecoration.LineThrough else null
                                        )
                                    }
                                }
                            }
                        } else {
                            // VERTICAL / HORIZONTAL: simple flat list
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
                                        textDecoration = if (isDone) TextDecoration.LineThrough else null
                                    )
                                }
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
// 3. DAILY TAB (SITE DIARY) - CLOUDINARY VERSION
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

    val displayDate = remember {
        SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date())
    }
    val context = LocalContext.current

    // --- Photo viewer state (for Cloudinary URLs) ---
    var imageToView by remember { mutableStateOf<String?>(null) }

    // --- State variables for modals (unchanged) ---
    var isWorkChangeMode by remember { mutableStateOf(false) }
    var workToDelete by remember { mutableStateOf<LoggedWork?>(null) }
    var showWorkModal by remember { mutableStateOf(false) }

    var isManpowerChangeMode by remember { mutableStateOf(false) }
    var manpowerToDelete by remember { mutableStateOf<Pair<String, Int>?>(null) }
    var showManpowerModal by remember { mutableStateOf(false) }

    var isEquipmentChangeMode by remember { mutableStateOf(false) }
    var equipmentToDelete by remember { mutableStateOf<ResourceLog?>(null) }
    var showEquipmentModal by remember { mutableStateOf(false) }

    var isMaterialChangeMode by remember { mutableStateOf(false) }
    var materialToDelete by remember { mutableStateOf<ResourceLog?>(null) }
    var showMaterialModal by remember { mutableStateOf(false) }

    // --- Temporary photo URIs for modals (to be uploaded) ---
    var tempPhotoUriForWork by remember { mutableStateOf<Uri?>(null) }
    var tempManpowerPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var tempEquipmentPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var tempMaterialPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var selectedWorkNameForPhoto by remember { mutableStateOf<String?>(null) }
    var selectedEquipmentForPhoto by remember { mutableStateOf<ResourceLog?>(null) }
    var selectedMaterialForPhoto by remember { mutableStateOf<ResourceLog?>(null) }

    // --- Photo picker launchers ---
    val workPhotoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null && selectedWorkNameForPhoto != null) {
            tempPhotoUriForWork = uri
        }
    }
    val manpowerPhotoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) tempManpowerPhotoUri = uri
    }
    val equipmentPhotoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null && selectedEquipmentForPhoto != null) {
            tempEquipmentPhotoUri = uri
        }
    }
    val materialPhotoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null && selectedMaterialForPhoto != null) {
            tempMaterialPhotoUri = uri
        }
    }

    if (viewModel.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = SiteMateOrange)
        }
        return
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
                        if (work.isUploading) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(text = "• ${work.name}", fontSize = 14.sp, color = Color.DarkGray)
                        if (work.cloudinaryUrl != null && !work.isUploading) {
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(onClick = { imageToView = work.cloudinaryUrl }, modifier = Modifier.size(24.dp)) {
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

        // --- 2. MANPOWER CARD ---
        DailyLogCard(
            title = "Manpower Present",
            icon = Icons.Filled.People,
            hasItems = viewModel.manpowerList.isNotEmpty() || viewModel.manpowerPhotoUrl != null,
            isChangeMode = isManpowerChangeMode,
            onChangeModeToggle = { isManpowerChangeMode = !isManpowerChangeMode },
            onAddClick = { showManpowerModal = true }
        ) {
            if (viewModel.manpowerList.isEmpty()) {
                Text("No manpower added yet.", color = Color.Gray, fontSize = 14.sp)
            } else {
                viewModel.manpowerList.forEach { (role, count) ->
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
                if (viewModel.manpowerPhotoUrl != null) {
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
                        TextButton(onClick = { imageToView = viewModel.manpowerPhotoUrl }) {
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
            onAddClick = { showEquipmentModal = true }
        ) {
            if (viewModel.equipmentList.isEmpty()) Text("No equipment added.", color = Color.Gray, fontSize = 14.sp)
            viewModel.equipmentList.forEach { item ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (item.isUploading) {
                                CircularProgressIndicator(modifier = Modifier.size(12.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(6.dp))
                            }
                            Text(text = item.name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = SiteMateBlue)
                        }
                        Text(text = "${item.quantity} units | ₱${item.cost}", fontSize = 12.sp, color = Color.Gray)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (item.cloudinaryUrl != null && !item.isUploading) {
                            IconButton(onClick = { imageToView = item.cloudinaryUrl }, modifier = Modifier.size(28.dp)) {
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
            onAddClick = { showMaterialModal = true }
        ) {
            if (viewModel.materialList.isEmpty()) Text("No materials added.", color = Color.Gray, fontSize = 14.sp)
            viewModel.materialList.forEach { item ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (item.isUploading) {
                                CircularProgressIndicator(modifier = Modifier.size(12.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(6.dp))
                            }
                            Text(text = item.name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = if(item.type == "Expense") Color(0xFFD32F2F) else SiteMateBlue)
                        }
                        Text(text = "${item.quantity} units | ₱${item.cost}", fontSize = 12.sp, color = Color.Gray)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (item.cloudinaryUrl != null && !item.isUploading) {
                            IconButton(onClick = { imageToView = item.cloudinaryUrl }, modifier = Modifier.size(28.dp)) {
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
            onClick = { viewModel.saveDailyLog(project.id) { Toast.makeText(context, "Saved!", Toast.LENGTH_SHORT).show() } },
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

    // ==========================================
    // WORK ACCOMPLISHED MODAL (with photo upload)
    // ==========================================
    if (showWorkModal) {
        var selectedWorks by remember { mutableStateOf<Map<String, Boolean>>(emptyMap()) }

        AlertDialog(
            onDismissRequest = { showWorkModal = false },
            title = { Text("Log Accomplished Work", color = SiteMateBlue, fontWeight = FontWeight.Bold) },
            text = {
                if (viewModel.availableWorksForModal.isEmpty()) {
                    Text("No pending works for the current phase.", color = Color.Gray)
                } else {
                    Column {
                        Text("Current Phase: ${viewModel.currentActiveMilestoneId ?: "Loading..."}", fontSize = 12.sp, color = SiteMateOrange)
                        LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                            items(viewModel.availableWorksForModal) { workName ->
                                val isChecked = selectedWorks[workName] == true
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = isChecked,
                                        onCheckedChange = { checked ->
                                            selectedWorks = if (checked) selectedWorks + (workName to true)
                                            else selectedWorks - workName
                                        },
                                        colors = CheckboxDefaults.colors(checkedColor = SiteMateOrange)
                                    )
                                    Text(text = workName, fontSize = 14.sp, modifier = Modifier.weight(1f))

                                    if (isChecked) {
                                        val hasPhoto = tempPhotoUriForWork != null && selectedWorkNameForPhoto == workName
                                        IconButton(
                                            onClick = {
                                                selectedWorkNameForPhoto = workName
                                                workPhotoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
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
                        selectedWorks.keys.forEach { workName ->
                            val photoUri = if (selectedWorkNameForPhoto == workName) tempPhotoUriForWork else null
                            if (photoUri != null) {
                                viewModel.addWorkWithPhoto(workName, photoUri, project.id)
                            } else {
                                // No photo – just add the work without image
                                val tempWork = LoggedWork(name = workName, isUploading = false)
                                viewModel.loggedWorks.add(tempWork)
                            }
                        }
                        viewModel.autoSaveDraft(project.id)
                        selectedWorks = emptyMap()
                        tempPhotoUriForWork = null
                        selectedWorkNameForPhoto = null
                        showWorkModal = false
                        Toast.makeText(context, "Logged ${selectedWorks.size} items", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SiteMateBlue)
                ) {
                    Text("Submit")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showWorkModal = false
                    tempPhotoUriForWork = null
                    selectedWorkNameForPhoto = null
                }) { Text("Cancel", color = Color.Gray) }
            }
        )
    }

    // ==========================================
    // MANPOWER MODAL (with group photo)
    // ==========================================
    if (showManpowerModal) {
        val allManpowerTypes = listOf(
            "Project Engineer", "Site Engineer", "Foreman", "Carpenter", "Mason",
            "Steelman / Rebar Worker", "Electrician", "Plumber", "Laborer",
            "Equipment Operator", "Welder", "Estimator"
        )
        var manpowerInputs by remember { mutableStateOf<Map<String, String>>(emptyMap()) }

        AlertDialog(
            onDismissRequest = { showManpowerModal = false },
            title = { Text("Headcounts", color = SiteMateBlue, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedButton(
                        onClick = { manpowerPhotoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                    ) {
                        val hasPhoto = tempManpowerPhotoUri != null || viewModel.manpowerPhotoUrl != null
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
                                        val clean = input.filter { it.isDigit() }
                                        if (clean.isEmpty()) manpowerInputs = manpowerInputs - role
                                        else manpowerInputs = manpowerInputs + (role to clean)
                                    },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.width(70.dp).height(50.dp),
                                    singleLine = true,
                                    textStyle = TextStyle(textAlign = TextAlign.Center)
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
                            viewModel.addManpowerPhoto(tempManpowerPhotoUri!!, project.id)
                        }
                        viewModel.autoSaveDraft(project.id)
                        tempManpowerPhotoUri = null
                        showManpowerModal = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SiteMateBlue)
                ) { Text("Submit") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showManpowerModal = false
                    tempManpowerPhotoUri = null
                }) { Text("Cancel", color = Color.Gray) }
            }
        )
    }

    // ==========================================
    // EQUIPMENT MODAL (with photo per item)
    // ==========================================
    if (showEquipmentModal) {
        var equipmentInputs by remember { mutableStateOf(listOf<EquipmentInput>()) }

        AlertDialog(
            onDismissRequest = { showEquipmentModal = false },
            title = { Text("Log Equipment", color = SiteMateBlue, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    LazyColumn(modifier = Modifier.heightIn(max = 350.dp)) {
                        items(equipmentInputs) { input ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                IconButton(onClick = { equipmentInputs = equipmentInputs.filter { it.id != input.id } }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Filled.Close, contentDescription = "Remove", tint = Color.Red, modifier = Modifier.size(16.dp))
                                }
                                OutlinedTextField(
                                    value = input.name,
                                    onValueChange = { n -> equipmentInputs = equipmentInputs.map { if (it.id == input.id) it.copy(name = n) else it } },
                                    label = { Text("Equipment", fontSize = 10.sp) },
                                    modifier = Modifier.weight(1.4f).height(56.dp),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = input.qty,
                                    onValueChange = { q -> val clean = q.filter { it.isDigit() }; equipmentInputs = equipmentInputs.map { if (it.id == input.id) it.copy(qty = clean) else it } },
                                    label = { Text("Qty", fontSize = 10.sp) },
                                    modifier = Modifier.weight(0.8f).height(56.dp),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = input.cost,
                                    onValueChange = { c -> val clean = c.filter { it.isDigit() }; equipmentInputs = equipmentInputs.map { if (it.id == input.id) it.copy(cost = clean) else it } },
                                    label = { Text("Cost", fontSize = 10.sp) },
                                    prefix = { Text("₱ ", fontSize = 12.sp, color = SiteMateBlue) },
                                    modifier = Modifier.weight(1.1f).height(56.dp),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true
                                )
                                IconButton(
                                    onClick = {
                                        selectedEquipmentForPhoto = ResourceLog(
                                            id = input.id,
                                            type = "Equipment",
                                            name = input.name,
                                            quantity = input.qty.toIntOrNull() ?: 0,
                                            cost = input.cost
                                        )
                                        equipmentPhotoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    val hasPhoto = tempEquipmentPhotoUri != null && selectedEquipmentForPhoto?.id == input.id
                                    Icon(
                                        imageVector = if (hasPhoto) Icons.Filled.CheckCircle else Icons.Filled.AddAPhoto,
                                        contentDescription = "Attach Photo",
                                        tint = if (hasPhoto) Color(0xFF4CAF50) else SiteMateOrange,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                        item {
                            TextButton(onClick = { equipmentInputs = equipmentInputs + EquipmentInput() }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                                Icon(Icons.Filled.Add, contentDescription = null, tint = SiteMateBlue)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add Equipment", color = SiteMateBlue, fontWeight = FontWeight.Bold)
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
                                val newItem = ResourceLog(
                                    type = "Equipment",
                                    name = inp.name,
                                    quantity = qty,
                                    cost = inp.cost.ifEmpty { "0" },
                                    cloudinaryUrl = null,
                                    isUploading = false
                                )
                                viewModel.equipmentList.add(newItem)

                                // If a photo was taken for this item, upload it
                                if (tempEquipmentPhotoUri != null && selectedEquipmentForPhoto?.name == inp.name) {
                                    viewModel.addResourcePhoto(newItem, tempEquipmentPhotoUri!!, project.id)
                                }
                            }
                        }
                        viewModel.autoSaveDraft(project.id)
                        tempEquipmentPhotoUri = null
                        selectedEquipmentForPhoto = null
                        showEquipmentModal = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SiteMateBlue)
                ) { Text("Submit") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showEquipmentModal = false
                    tempEquipmentPhotoUri = null
                    selectedEquipmentForPhoto = null
                }) { Text("Cancel", color = Color.Gray) }
            }
        )
    }

    // ==========================================
    // MATERIALS & EXPENSES MODAL (with receipt photo)
    // ==========================================
    if (showMaterialModal) {
        var materialInputs by remember { mutableStateOf(listOf<MaterialInput>()) }

        AlertDialog(
            onDismissRequest = { showMaterialModal = false },
            title = { Text("Log Materials & Expenses", color = SiteMateBlue, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    LazyColumn(modifier = Modifier.heightIn(max = 350.dp)) {
                        items(materialInputs) { input ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                IconButton(onClick = { materialInputs = materialInputs.filter { it.id != input.id } }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Filled.Close, contentDescription = "Remove", tint = Color.Red, modifier = Modifier.size(16.dp))
                                }
                                OutlinedTextField(
                                    value = input.name,
                                    onValueChange = { n -> materialInputs = materialInputs.map { if (it.id == input.id) it.copy(name = n) else it } },
                                    label = { Text(input.type, fontSize = 10.sp) },
                                    modifier = Modifier.weight(1.4f).height(56.dp),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = input.qty,
                                    onValueChange = { q -> val clean = q.filter { it.isDigit() }; materialInputs = materialInputs.map { if (it.id == input.id) it.copy(qty = clean) else it } },
                                    label = { Text("Qty", fontSize = 10.sp) },
                                    modifier = Modifier.weight(0.8f).height(56.dp),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = input.cost,
                                    onValueChange = { c -> val clean = c.filter { it.isDigit() }; materialInputs = materialInputs.map { if (it.id == input.id) it.copy(cost = clean) else it } },
                                    label = { Text("Cost", fontSize = 10.sp) },
                                    prefix = { Text("₱ ", fontSize = 12.sp, color = SiteMateBlue) },
                                    modifier = Modifier.weight(1.1f).height(56.dp),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true
                                )
                                IconButton(
                                    onClick = {
                                        selectedMaterialForPhoto = ResourceLog(
                                            id = input.id,
                                            type = input.type,
                                            name = input.name,
                                            quantity = input.qty.toIntOrNull() ?: 0,
                                            cost = input.cost
                                        )
                                        materialPhotoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    val hasPhoto = tempMaterialPhotoUri != null && selectedMaterialForPhoto?.id == input.id
                                    Icon(
                                        imageVector = if (hasPhoto) Icons.Filled.CheckCircle else Icons.Filled.AddAPhoto,
                                        contentDescription = "Attach Receipt",
                                        tint = if (hasPhoto) Color(0xFF4CAF50) else SiteMateOrange,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                        item {
                            Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                                TextButton(onClick = { materialInputs = materialInputs + MaterialInput(type = "Material") }) {
                                    Icon(Icons.Filled.Add, contentDescription = null, tint = SiteMateBlue, modifier = Modifier.size(16.dp))
                                    Text(" Add Material", color = SiteMateBlue, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                                TextButton(onClick = { materialInputs = materialInputs + MaterialInput(type = "Expense") }) {
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
                                val newItem = ResourceLog(
                                    type = inp.type,
                                    name = inp.name,
                                    quantity = qty,
                                    cost = inp.cost.ifEmpty { "0" },
                                    cloudinaryUrl = null,
                                    isUploading = false
                                )
                                viewModel.materialList.add(newItem)

                                if (tempMaterialPhotoUri != null && selectedMaterialForPhoto?.name == inp.name) {
                                    viewModel.addResourcePhoto(newItem, tempMaterialPhotoUri!!, project.id)
                                }
                            }
                        }
                        viewModel.autoSaveDraft(project.id)
                        tempMaterialPhotoUri = null
                        selectedMaterialForPhoto = null
                        showMaterialModal = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SiteMateBlue)
                ) { Text("Submit") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showMaterialModal = false
                    tempMaterialPhotoUri = null
                    selectedMaterialForPhoto = null
                }) { Text("Cancel", color = Color.Gray) }
            }
        )
    }

    // ==========================================
    // DELETE CONFIRMATION DIALOGS (unchanged logic)
    // ==========================================
    if (workToDelete != null) {
        AlertDialog(
            onDismissRequest = { workToDelete = null },
            title = { Text("Delete Log?", color = Color.Red, fontWeight = FontWeight.Bold) },
            text = { Text("Delete '${workToDelete?.name}'? This cannot be undone.", color = Color.DarkGray) },
            confirmButton = {
                Button(onClick = { viewModel.loggedWorks.remove(workToDelete); workToDelete = null; if (viewModel.loggedWorks.isEmpty()) isWorkChangeMode = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { workToDelete = null }) { Text("Cancel", color = Color.Gray) } }
        )
    }
    if (manpowerToDelete != null) {
        AlertDialog(
            onDismissRequest = { manpowerToDelete = null },
            title = { Text("Delete Manpower?", color = Color.Red, fontWeight = FontWeight.Bold) },
            text = { Text("Remove '${manpowerToDelete?.first}'? This cannot be undone.", color = Color.DarkGray) },
            confirmButton = {
                Button(onClick = { viewModel.manpowerList.remove(manpowerToDelete); manpowerToDelete = null; if (viewModel.manpowerList.isEmpty()) isManpowerChangeMode = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { manpowerToDelete = null }) { Text("Cancel", color = Color.Gray) } }
        )
    }
    if (equipmentToDelete != null) {
        AlertDialog(
            onDismissRequest = { equipmentToDelete = null },
            title = { Text("Delete Equipment?", color = Color.Red, fontWeight = FontWeight.Bold) },
            text = { Text("Remove '${equipmentToDelete?.name}'?") },
            confirmButton = {
                Button(onClick = { viewModel.equipmentList.remove(equipmentToDelete); equipmentToDelete = null; if (viewModel.equipmentList.isEmpty()) isEquipmentChangeMode = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { equipmentToDelete = null }) { Text("Cancel", color = Color.Gray) } }
        )
    }
    if (materialToDelete != null) {
        AlertDialog(
            onDismissRequest = { materialToDelete = null },
            title = { Text("Delete Material/Expense?", color = Color.Red, fontWeight = FontWeight.Bold) },
            text = { Text("Remove '${materialToDelete?.name}'?") },
            confirmButton = {
                Button(onClick = { viewModel.materialList.remove(materialToDelete); materialToDelete = null; if (viewModel.materialList.isEmpty()) isMaterialChangeMode = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { materialToDelete = null }) { Text("Cancel", color = Color.Gray) } }
        )
    }

    // ==========================================
    // IMAGE VIEWER DIALOG (Cloudinary URL)
    // ==========================================
    if (imageToView != null) {
        AlertDialog(
            onDismissRequest = { imageToView = null },
            title = { Text("Attached Image", color = SiteMateBlue, fontWeight = FontWeight.Bold) },
            text = {
                Box(
                    modifier = Modifier.fillMaxWidth().height(350.dp).background(Color.Black, shape = RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(imageToView)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Attached Photo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }
            },
            confirmButton = {
                Button(onClick = { imageToView = null }, colors = ButtonDefaults.buttonColors(containerColor = SiteMateBlue)) {
                    Text("Close")
                }
            }
        )
    }
}

// Helper data classes for modal inputs (place outside any composable)
data class EquipmentInput(val id: String = UUID.randomUUID().toString(), var name: String = "", var qty: String = "", var cost: String = "")
data class MaterialInput(val id: String = UUID.randomUUID().toString(), val type: String, var name: String = "", var qty: String = "", var cost: String = "")


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

    var selectedReportType by remember { mutableStateOf("Daily Accomplishment") }
    val reportTypes = listOf("Daily Accomplishment", "Overall Progress")

    var selectedCalendar by remember { mutableStateOf(Calendar.getInstance()) }

    val displayFormatter = remember {
        SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date())
    }
    val displayDate = displayFormatter.format(selectedCalendar.time)
    val dbFormatter = remember {
        SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date())
    }
    val dbDateString = dbFormatter.format(selectedCalendar.time)

    var expanded by remember { mutableStateOf(false) }

    //Delete Button
    var isDeleteMode by remember { mutableStateOf(false) }
    var reportToDelete by remember { mutableStateOf<ReportDoc?>(null) }

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
                // Report Type Dropdown

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedReportType,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Report Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        colors = OutlinedTextFieldDefaults.colors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor() // 👈 This anchors the dropdown to the text field
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        reportTypes.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type) },
                                onClick = {
                                    selectedReportType = type
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Date picker only for Daily Accomplishment
                if (selectedReportType == "Daily Accomplishment") {
                    OutlinedButton(
                        onClick = { datePickerDialog.show() },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Icon(Icons.Filled.DateRange, contentDescription = null, tint = SiteMateOrange)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = displayDate, color = SiteMateBlue, fontSize = 16.sp)
                    }
                } else {
                    // For Overall Progress, show info text
                    Text(
                        text = "This will generate a report of the project's overall progress based on all milestones.",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        when (selectedReportType) {
                            "Daily Accomplishment" -> {
                                viewModel.generateReport(
                                    context = context,
                                    projectId = projectId,
                                    type = "Daily Accomplishment Report",
                                    dateString = dbDateString
                                ) { success ->
                                    if (success) Toast.makeText(context, "PDF saved to Downloads!", Toast.LENGTH_LONG).show()
                                    else Toast.makeText(context, "Failed to generate report.", Toast.LENGTH_LONG).show()
                                }
                            }
                            "Overall Progress" -> {
                                viewModel.generateOverallProgressReport(
                                    context = context,
                                    projectId = projectId
                                ) { success ->
                                    if (success) Toast.makeText(context, "Overall progress report saved to Downloads!", Toast.LENGTH_LONG).show()
                                    else Toast.makeText(context, "Failed to generate overall report.", Toast.LENGTH_LONG).show()
                                }
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

        // Report Archive header with delete mode toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Report Archive", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = SiteMateBlue)
            TextButton(
                onClick = { isDeleteMode = !isDeleteMode },
                colors = ButtonDefaults.textButtonColors(contentColor = if (isDeleteMode) Color.Red else SiteMateOrange)
            ) {
                Icon(
                    imageVector = if (isDeleteMode) Icons.Filled.Close else Icons.Filled.Delete,
                    contentDescription = if (isDeleteMode) "Cancel Delete Mode" else "Delete Reports",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(if (isDeleteMode) "Cancel" else "Delete")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        if (viewModel.reports.isEmpty()) {
            Text("No reports generated yet.", color = Color.Gray, fontSize = 14.sp)
        } else {
            viewModel.reports.forEach { report ->
                ReportArchiveCard(
                    title = report.title,
                    date = report.date,
                    type = report.type,
                    isDeleteMode = isDeleteMode,
                    onDownload = {
                        if (report.type == "Overall Progress") {
                            viewModel.generateOverallProgressReport(
                                context = context,
                                projectId = projectId
                            ) { success ->
                                if (success) Toast.makeText(context, "Report re-downloaded", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            viewModel.generateReport(
                                context = context,
                                projectId = projectId,
                                type = report.type,
                                dateString = report.id
                            ) { success ->
                                if (success) Toast.makeText(context, "Report re-downloaded", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    onDelete = {
                        reportToDelete = report
                    }
                )
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
    }

    // Delete confirmation dialog
    if (reportToDelete != null) {
        AlertDialog(
            onDismissRequest = { reportToDelete = null },
            title = { Text("Delete Report", color = Color.Red, fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete '${reportToDelete?.title}'? This action cannot be undone.", color = Color.DarkGray) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteReport(projectId, reportToDelete!!) {
                            Toast.makeText(context, "Report deleted", Toast.LENGTH_SHORT).show()
                            isDeleteMode = false
                        }
                        reportToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Delete Permanently")
                }
            },
            dismissButton = {
                TextButton(onClick = { reportToDelete = null }) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        )
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
fun ReportArchiveCard(
    title: String,
    date: String,
    type: String,
    isDeleteMode: Boolean,
    onDownload: () -> Unit,
    onDelete: () -> Unit
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
                    modifier = Modifier.size(40.dp)
                        .background(Color(0xFFE57373).copy(alpha = 0.2f), shape = RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
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
                if (isDeleteMode) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = Color.Red)
                    }
                } else {
                    IconButton(
                        onClick = onDownload,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Filled.Download, contentDescription = "Download", tint = SiteMateBlue)
                    }
                }
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

    // --- SEARCH & SORT STATE ---
    var searchQuery by remember { mutableStateOf("") }
    var sortOption by remember { mutableStateOf("Name") }
    var showSortMenu by remember { mutableStateOf(false) }

    var isClientDeleteMode by remember { mutableStateOf(false) }
    var showClientModal by remember { mutableStateOf(false) }

    // --- FILTER & SORT LOGIC ---
    // 1. Get only the clients
    val clients = viewModel.teamMembers.filter { it.type == "Client" }

    // 2. Apply the search query instantly
    val searchedClients = if (searchQuery.isEmpty()) {
        clients
    } else {
        clients.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                    it.email.contains(searchQuery, ignoreCase = true)
        }
    }

    // 3. Sort the filtered results instantly
    val finalFilteredClients = when (sortOption) {
        "Email" -> searchedClients.sortedBy { it.email.lowercase() }
        else -> searchedClients.sortedBy { it.name.lowercase() }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {

        Spacer(modifier = Modifier.height(16.dp))

        // --- SEARCH BAR & SORT DROPDOWN ---
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Search clients by name...") },
                leadingIcon = { Icon(Icons.Default.PersonSearch, contentDescription = null, tint = SiteMateOrange) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SiteMateOrange,
                    unfocusedBorderColor = Color.LightGray
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Sort Dropdown Button
            Box {
                IconButton(
                    onClick = { showSortMenu = true },
                    modifier = Modifier
                        .size(56.dp)
                        .background(SiteMateBlue.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                ) {
                    Icon(Icons.Default.FilterList, contentDescription = "Sort", tint = SiteMateBlue)
                }

                DropdownMenu(
                    expanded = showSortMenu,
                    onDismissRequest = { showSortMenu = false },
                    modifier = Modifier.background(Color.White)
                ) {
                    DropdownMenuItem(
                        text = { Text("Sort by Name", fontWeight = if (sortOption == "Name") FontWeight.Bold else FontWeight.Normal) },
                        onClick = { sortOption = "Name"; showSortMenu = false }
                    )
                    DropdownMenuItem(
                        text = { Text("Sort by Email", fontWeight = if (sortOption == "Email") FontWeight.Bold else FontWeight.Normal) },
                        onClick = { sortOption = "Email"; showSortMenu = false }
                    )
                }
            }
        }

        // --- CLIENT SECTION ---
        SectionHeader(
            title = "Client Access",
            isDeleteMode = isClientDeleteMode,
            onAddClick = { showClientModal = true },
            onDeleteToggle = { isClientDeleteMode = !isClientDeleteMode }
        )

        if (clients.isEmpty()) {
            Text("No clients invited.", color = Color.Gray, fontSize = 14.sp, modifier = Modifier.padding(bottom = 16.dp))
        } else if (finalFilteredClients.isEmpty()) {
            Text("No matching clients found.", color = Color.Gray, fontSize = 14.sp, modifier = Modifier.padding(bottom = 16.dp))
        } else {
            // Use the SORTED & FILTERED list here!
            finalFilteredClients.forEach { member ->
                MemberCard(
                    member = member,
                    isDeleteMode = isClientDeleteMode,
                    isClient = true,
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
    users: List<com.wharzc.sitemate.model.SelectableUser>, // Added explicit package assuming it matches your model
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onAssign: (List<com.wharzc.sitemate.model.SelectableUser>) -> Unit
) {
    var selectedUsers by remember { mutableStateOf(setOf<com.wharzc.sitemate.model.SelectableUser>()) }

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