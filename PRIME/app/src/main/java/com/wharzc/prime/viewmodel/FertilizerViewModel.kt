package com.wharzc.prime.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.wharzc.prime.data.FertilizerNotif
import com.wharzc.prime.data.PrimeDatabase
import com.wharzc.prime.notifications.AlarmScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


class FertilizerViewModel(application: Application) : AndroidViewModel(application) {
    private val notifDao = PrimeDatabase.getDatabase(application).notifDao()
    private val alarmScheduler = AlarmScheduler(application)

    private val _notifications = MutableStateFlow<List<FertilizerNotif>>(emptyList())
    val notifications: StateFlow<List<FertilizerNotif>> = _notifications.asStateFlow()

    init {
        viewModelScope.launch {
            notifDao.getAllNotifs().collect { notifs ->
                _notifications.value = notifs
            }
        }
    }

    fun addNotification(notif: FertilizerNotif) {
        viewModelScope.launch {
            notifDao.insertNotif(notif)
            alarmScheduler.schedule(notif)
        }
    }

    fun deleteNotification(notif: FertilizerNotif) {
        viewModelScope.launch {
            notifDao.deleteNotif(notif)
            alarmScheduler.cancel(notif)
        }
    }
}