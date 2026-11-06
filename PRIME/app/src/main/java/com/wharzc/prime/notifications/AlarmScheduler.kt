package com.wharzc.prime.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.wharzc.prime.data.FertilizerNotif
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AlarmScheduler(private val context: Context) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun schedule(notif: FertilizerNotif) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("NOTIF_LABEL", if (notif.label.isNotBlank()) notif.label else "Time to apply fertilizer!")
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context, notif.id.hashCode(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance()
        val timeParts = notif.time.split(":")
        var hour = timeParts[0].toInt()
        val minute = timeParts[1].toInt()

        if (notif.amPm == "PM" && hour < 12) hour += 12
        if (notif.amPm == "AM" && hour == 12) hour = 0

        if (!notif.isRecurring) {
            val format = SimpleDateFormat("MM/dd/yyyy", Locale.US)
            val date = format.parse(notif.dateOrRule)
            if (date != null) {
                calendar.time = date
                calendar.set(Calendar.HOUR_OF_DAY, hour)
                calendar.set(Calendar.MINUTE, minute)
                calendar.set(Calendar.SECOND, 0)

                if (calendar.timeInMillis > System.currentTimeMillis()) {
                    try {
                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
                    } catch (e: SecurityException) {
                        Log.e("AlarmScheduler", "Missing EXACT_ALARM permission")
                    }
                }
            }
        } else {
            // For recurring, set for today's time, or tomorrow if already passed today
            calendar.set(Calendar.HOUR_OF_DAY, hour)
            calendar.set(Calendar.MINUTE, minute)
            calendar.set(Calendar.SECOND, 0)
            if (calendar.timeInMillis <= System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }
            // Simplest implementation for daily/recurring check.
            // Complex rules (Weekends, Nth day) require rescheduling upon triggering.
            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                AlarmManager.INTERVAL_DAY, // Will trigger daily and you can filter in receiver
                pendingIntent
            )
        }
    }

    fun cancel(notif: FertilizerNotif) {
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, notif.id.hashCode(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}