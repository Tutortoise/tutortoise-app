package com.tutortoise.tutortoise

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.tutortoise.tutortoise.domain.AuthManager

class TutortoiseApplication : Application() {
    override fun onCreate() {
        // Set the default theme to light mode
        super.onCreate()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        AuthManager.initialize(this)
    }

    override fun onTerminate() {
        super.onTerminate()
    }
}