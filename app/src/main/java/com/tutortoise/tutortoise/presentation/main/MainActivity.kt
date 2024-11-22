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
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.firebase.auth.FirebaseAuth
import com.tutortoise.tutortoise.R
import com.tutortoise.tutortoise.databinding.ActivityMainBinding
import com.tutortoise.tutortoise.presentation.login.LoginActivity
import com.tutortoise.tutortoise.presentation.onboarding.OnboardingActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    // Declare the binding object
    private lateinit var binding: ActivityMainBinding

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        // Install splash screen
        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)

        // Initialize the binding object with the inflated layout
        binding = ActivityMainBinding.inflate(layoutInflater)

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
                }
                start()
            }
        }

        // Simulate some loading work
        lifecycleScope.launch {
            delay(1000) // Short delay for splash screen
            keepSplashOnScreen = false

            proceedAfterSplash()
        }

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment
        val navController = navHostFragment.navController
        binding.bottomNav.setupWithNavController(navController)

        val startFragment = intent.getStringExtra("startFragment")
        if (startFragment == "profile") {
            binding.bottomNav.selectedItemId = R.id.profileFragment
        }
    }

    private fun proceedAfterSplash() {
        if (isFirstRun()) {
            startOnboarding()
            return
        }

        if (isUserLoggedIn()) {
            setContentView(binding.root)
        } else {
            redirectToLogin()
        }
    }

    private fun isFirstRun(): Boolean {
        val sharedPref = getSharedPreferences("AppPreferences", MODE_PRIVATE)
        return sharedPref.getBoolean("isFirstRun", true)
    }

    private fun isUserLoggedIn(): Boolean {
        return firebaseAuth.currentUser != null
    }

    private fun redirectToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
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
