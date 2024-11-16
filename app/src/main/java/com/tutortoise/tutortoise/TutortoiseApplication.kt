package com.tutortoise.tutortoise

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate

class TutortoiseApplication : Application() {
    override fun onCreate() {
        // Set the default theme to light mode
        super.onCreate()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }
}