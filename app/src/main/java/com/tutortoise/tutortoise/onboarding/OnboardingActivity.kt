package com.tutortoise.tutortoise.onboarding

import android.os.Bundle
import android.transition.Explode
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI.setupActionBarWithNavController
import androidx.navigation.ui.setupActionBarWithNavController
import com.tutortoise.tutortoise.R

class OnboardingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.enterTransition = Explode().apply {
            duration = 500
        }

        setContentView(R.layout.activity_onboarding)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment

        navHostFragment.view?.let { view ->
            view.alpha = 0f
            view.animate()
                .alpha(1f)
                .setDuration(500)
                .setInterpolator(FastOutSlowInInterpolator())
                .start()
        }

        val navController = navHostFragment.navController

        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.onboardingFragment4) {
                val sharedPref = getSharedPreferences("AppPreferences", MODE_PRIVATE)
                val editor = sharedPref.edit()
                editor.putBoolean("isFirstRun", false)
                editor.apply()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = (supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment).navController
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}