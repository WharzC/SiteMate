package com.wharzc.motoguard

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object NotificationRepository {
    private const val CHANNEL_ID = "motoguard_notifications"
    private const val CHANNEL_NAME = "MotoGuard Alerts"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Real-time motorcycle alerts"
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    suspend fun addNotification(
        context: Context,
        title: String,
        message: String,
        type: String,
        showSystemNotification: Boolean = true,
        showPopup: Boolean = true
    ) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()
        val notificationData = hashMapOf(
            "title" to title,
            "message" to message,
            "timestamp" to System.currentTimeMillis(),
            "read" to false,
            "type" to type
        )
        db.collection("users").document(userId).collection("notifications")
            .add(notificationData).await()

        if (showSystemNotification) {
            showSystemNotification(context, title, message)
        }
        if (showPopup) {
            onNewNotification?.invoke(title, message)
        }
    }

    private fun showSystemNotification(context: Context, title: String, message: String) {
        val manager = context.getSystemService(NotificationManager::class.java)
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        manager.notify(System.currentTimeMillis().toInt(), notification)
    }

    var onNewNotification: ((String, String) -> Unit)? = null
}