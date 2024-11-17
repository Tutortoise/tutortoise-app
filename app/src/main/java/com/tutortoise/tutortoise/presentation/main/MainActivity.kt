package com.tutortoise.tutortoise.presentation.main

import android.R
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.tutortoise.tutortoise.presentation.onboarding.OnboardingActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Install splash screen
        val splashScreen = installSplashScreen()

        // Keep splash screen visible for longer
        var keepSplashOnScreen = true
        splashScreen.setKeepOnScreenCondition { keepSplashOnScreen }

//        setContentView(R.layout.activity_main)

        // Add custom exit animation
        splashScreen.setOnExitAnimationListener { splashScreenView ->
            // Fade out animation
            val fadeOut = ObjectAnimator.ofFloat(
                splashScreenView.view,
                View.ALPHA,
                1f,
                0f
            ).apply {
                duration = 500
            }

            // Run animations together
            AnimatorSet().run {
//                playTogether(slideUp, fadeOut)
                play(fadeOut)
                doOnEnd {
                    splashScreenView.remove()
                    // Navigate to onboarding with animation
                    startOnboarding()
                }
                start()
            }
        }

        // Simulate some loading work
        lifecycleScope.launch {
            delay(2000) // 2 seconds
            keepSplashOnScreen = false
        }
    }

    private fun startOnboarding() {
        val intent = Intent(this, OnboardingActivity::class.java)

        // Create custom transition animation
        val options = ActivityOptions.makeCustomAnimation(
            this,
            R.anim.fade_in,
            R.anim.fade_out
        )

        startActivity(intent, options.toBundle())
        finish()
    }
}


//        val sharedPref = getSharedPreferences("AppPreferences", MODE_PRIVATE)
//        val isFirstRun = sharedPref.getBoolean("isFirstRun", true)
//
//        if (isFirstRun) {
//            startActivity(Intent(this, OnboardingActivity::class.java))
//            finish()
//        } else {
//            setContentView(R.layout.activity_main)
//        }