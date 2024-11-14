package com.tutortoise.tutortoise

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.tutortoise.tutortoise.onboarding.OnboardingActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()

        startActivity(Intent(this, OnboardingActivity::class.java))
        finish()

//        val sharedPref = getSharedPreferences("AppPreferences", MODE_PRIVATE)
//        val isFirstRun = sharedPref.getBoolean("isFirstRun", true)
//
//        if (isFirstRun) {
//            startActivity(Intent(this, OnboardingActivity::class.java))
//            finish()
//        } else {
//            setContentView(R.layout.activity_main)
//        }

    }

}