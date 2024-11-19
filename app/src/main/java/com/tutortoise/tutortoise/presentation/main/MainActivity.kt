package com.tutortoise.tutortoise.presentation.main

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
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.tutortoise.tutortoise.R
import com.tutortoise.tutortoise.databinding.ActivityMainBinding
import com.tutortoise.tutortoise.presentation.onboarding.OnboardingActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    // Declare the binding object
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize the binding object with the inflated layout
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Install splash screen
        val splashScreen = installSplashScreen()

        // Keep splash screen visible for longer
        var keepSplashOnScreen = true
        splashScreen.setKeepOnScreenCondition { keepSplashOnScreen }

        // Add custom exit animation
        splashScreen.setOnExitAnimationListener { splashScreenView ->
            val fadeOut = ObjectAnimator.ofFloat(
                splashScreenView.view,
                View.ALPHA,
                1f,
                0f
            ).apply {
                duration = 500
            }

            AnimatorSet().run {
                play(fadeOut)
                doOnEnd {
                    splashScreenView.remove()
                    checkFirstRunAndNavigate()
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

    private fun checkFirstRunAndNavigate() {
        val sharedPref = getSharedPreferences("AppPreferences", MODE_PRIVATE)
        val isFirstRun = sharedPref.getBoolean("isFirstRun", true)

        if (isFirstRun) {
            startOnboarding()
        } else {
            setupBottomNavigation()
        }
    }

// TODO: Fix Bottom Navigation
    private fun setupBottomNavigation() {
        val bottomNav: BottomNavigationView = binding.bottomNav
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.homeFragment, R.id.exploreFragment, R.id.sessionFragment, R.id.profileFragment
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        bottomNav.setupWithNavController(navController)
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
