package com.wharzc.motoguard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import androidx.compose.ui.platform.LocalLocale

data class NotificationData(
    val id: String,
    val title: String,
    val message: String,
    val timestamp: Long,
    val read: Boolean,
    val type: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(onNavigateBack: () -> Unit) {
    val backgroundColor = Color(0xFFF8F9FB)
    val primaryDarkBlue = Color(0xFF10213E)
    val headerBlue = Color(0xFF1976D2)

    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val currentUser = auth.currentUser

    var notifications by remember { mutableStateOf<List<NotificationData>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Load notifications from Firestore
    LaunchedEffect(Unit) {
        if (currentUser != null) {
            try {
                val snapshot = db.collection("users")
                    .document(currentUser.uid)
                    .collection("notifications")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .get()
                    .await()
                notifications = snapshot.documents.mapNotNull { doc ->
                    val title = doc.getString("title") ?: return@mapNotNull null
                    val message = doc.getString("message") ?: return@mapNotNull null
                    val timestamp = doc.getLong("timestamp") ?: return@mapNotNull null
                    val read = doc.getBoolean("read") ?: false
                    val type = doc.getString("type") ?: "general"
                    NotificationData(doc.id, title, message, timestamp, read, type)
                }
                isLoading = false
            } catch (e: Exception) {
                e.printStackTrace()
                isLoading = false
            }
        }
    }

    // Mark a notification as read when clicked
    suspend fun markAsRead(notificationId: String) {
        if (currentUser != null) {
            try {
                db.collection("users")
                    .document(currentUser.uid)
                    .collection("notifications")
                    .document(notificationId)
                    .update("read", true)
                    .await()
                // Update local list
                notifications = notifications.map {
                    if (it.id == notificationId) it.copy(read = true) else it
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 22.sp) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.KeyboardArrowLeft, "Back", tint = Color.White, modifier = Modifier.size(32.dp))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = headerBlue)
            )
        },
        containerColor = backgroundColor
    ) { innerPadding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = primaryDarkBlue)
            }
        } else if (notifications.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No notifications yet", color = Color.Gray, fontSize = 16.sp)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 20.dp),
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(notifications) { notification ->
                    NotificationCard(
                        item = notification,
                        onMarkRead = { markAsRead(notification.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun NotificationCard(item: NotificationData, onMarkRead: suspend () -> Unit) {
    val iconAndColor = when (item.type) {
        "geofence" -> Icons.Outlined.Map to Color(0xFFFF9800)
        "movement" -> Icons.Outlined.Warning to Color(0xFFF44336)
        "battery" -> Icons.Outlined.BatteryAlert to Color(0xFFFFC107)
        "alert" -> Icons.Outlined.NotificationsActive to Color(0xFF2196F3)
        else -> Icons.Outlined.Info to Color(0xFF9C27B0)
    }
    val scope = rememberCoroutineScope()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                if (!item.read) {
                    scope.launch { onMarkRead() }
                }
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (item.read) Color.White else Color(0xFFE3F2FD)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(56.dp).background(color = iconAndColor.second, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = iconAndColor.first, contentDescription = item.title, tint = Color.White, modifier = Modifier.size(28.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = item.title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10213E))
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = item.message, fontSize = 14.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = java.text.SimpleDateFormat("dd MMM yyyy, HH:mm", LocalLocale.current.platformLocale)
                        .format(java.util.Date(item.timestamp)),
                    fontSize = 10.sp,
                    color = Color.LightGray
                )
            }
            if (!item.read) {
                Box(modifier = Modifier.size(12.dp).background(Color.Red, shape = CircleShape))
            }
        }
    }
}