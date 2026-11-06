package com.wharzc.sentry

import android.app.Application
import com.google.firebase.FirebaseApp

class SentryApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // This guarantees Firebase boots up before EVERYTHING else
        FirebaseApp.initializeApp(this)
    }
}