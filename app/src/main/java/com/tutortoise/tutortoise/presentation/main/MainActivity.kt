package com.tutortoise.tutortoise.presentation.main

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.tutortoise.tutortoise.R
import com.tutortoise.tutortoise.data.model.CategoryResponse
import com.tutortoise.tutortoise.data.repository.AuthRepository
import com.tutortoise.tutortoise.databinding.ActivityMainBinding
import com.tutortoise.tutortoise.presentation.auth.login.LoginActivity
import com.tutortoise.tutortoise.presentation.main.learner.explore.ExploreViewModel
import com.tutortoise.tutortoise.presentation.onboarding.OnboardingActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val exploreViewModel: ExploreViewModel by viewModels()
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private lateinit var authRepository: AuthRepository
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        // Install splash screen
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        setContentView(binding.root) // Set content view first

        authRepository = AuthRepository(this)

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
            delay(1000)
            keepSplashOnScreen = false
            proceedAfterSplash()
        }

        setupNavigation()
    }

    private fun setupNavigation() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment
        navController = navHostFragment.navController

        // Get user role
        val userRole = authRepository.getUserRole()

        // Set appropriate navigation graph based on role
        val navGraph = if (userRole == "tutor") {
            R.navigation.mobile_tutor_navigation // tutor navigation
        } else {
            R.navigation.mobile_learner_navigation // learner navigation
        }

        // Handle deep links
        intent?.extras?.getString("startFragment")?.let { fragmentName ->
            when (fragmentName) {
                "profile" -> navController.navigate(R.id.profileLearnerFragment)
                "home" -> navController.navigate(R.id.homeLearnerFragment)
            }
        }

        // Set the navigation graph
        val graphInflater = navController.navInflater
        val graph = graphInflater.inflate(navGraph)
        navController.graph = graph

        // Set appropriate menu based on role
        binding.bottomNav.menu.clear()
        binding.bottomNav.inflateMenu(
            if (userRole == "tutor") {
                R.menu.bottom_nav_menu_tutor
            } else {
                R.menu.bottom_nav_menu_learner
            }
        )

        binding.bottomNav.setupWithNavController(navController)

        // Handle deep linking or specific fragment start
        val startFragment = intent.getStringExtra("startFragment")
        when (startFragment) {
            "profile" -> binding.bottomNav.selectedItemId = R.id.profileLearnerFragment
            "home" -> binding.bottomNav.selectedItemId = R.id.homeLearnerFragment
            "explore" -> navigateToExploreWithIntentData()
        }
    }

    private fun navigateToExploreWithIntentData() {
        val categoryId = intent.getStringExtra("categoryId")
        val categoryName = intent.getStringExtra("categoryName")

        if (categoryId != null) {
            exploreViewModel.clearData() // Clear any existing data
            exploreViewModel.setSelectedCategory(
                CategoryResponse(
                    categoryId,
                    categoryName ?: "",
                    ""
                ) // Empty iconUrl since it's not needed
            )

            // Navigate to ExploreLearnerFragment
            binding.bottomNav.selectedItemId = R.id.exploreLearnerFragment
            navController.navigate(R.id.exploreLearnerFragment)
        }
    }

    private fun proceedAfterSplash() {
        if (isFirstRun()) {
            startOnboarding()
            return
        }

        if (!isUserLoggedIn()) {
            redirectToLogin()
        }
    }

    private fun isFirstRun(): Boolean {
        val sharedPref = getSharedPreferences("AppPreferences", MODE_PRIVATE)
        return sharedPref.getBoolean("isFirstRun", true)
    }

    private fun isUserLoggedIn(): Boolean {
        val authRepository = AuthRepository(applicationContext)
        return authRepository.getToken() != null
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

    fun navigateToExploreWithSearch(query: String) {
        // Set the query in ViewModel
        exploreViewModel.setSearchQuery(query)

        // Navigate to explore
        binding.bottomNav.selectedItemId = R.id.exploreLearnerFragment
        navController.navigate(R.id.exploreLearnerFragment)
    }

    fun navigateToExploreWithCategory(category: CategoryResponse) {
        exploreViewModel.clearData() // Clear any existing data
        exploreViewModel.setSelectedCategory(category)

        // Then navigate
        binding.bottomNav.selectedItemId = R.id.exploreLearnerFragment
        navController.navigate(R.id.exploreLearnerFragment)
    }
}