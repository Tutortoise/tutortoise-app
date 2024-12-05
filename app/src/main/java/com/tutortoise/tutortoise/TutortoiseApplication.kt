package com.tutortoise.tutortoise

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.google.firebase.Firebase
import com.google.firebase.database.database
import com.google.firebase.initialize
import com.tutortoise.tutortoise.domain.AuthManager
import com.tutortoise.tutortoise.domain.FCMTokenManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class TutortoiseApplication : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        AuthManager.initialize(this)

        Firebase.initialize(this)
        Firebase.database.setPersistenceEnabled(true) // Enable offline capabilities

        applicationScope.launch {
            FCMTokenManager.initialize(this@TutortoiseApplication)
        }
    }

    override fun onTerminate() {
        super.onTerminate()
    }
}