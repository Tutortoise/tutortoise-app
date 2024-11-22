package com.tutortoise.tutortoise.presentation.onboarding.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.navigation.fragment.findNavController
import com.tutortoise.tutortoise.R
import com.tutortoise.tutortoise.presentation.main.MainActivity
import com.tutortoise.tutortoise.databinding.FragmentLoginRegisterBinding
import com.tutortoise.tutortoise.presentation.login.LoginActivity

class LoginRegisterFragment : Fragment() {

    private var _binding: FragmentLoginRegisterBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set initial states for animation
        binding.onboardingIllust1.alpha = 0f
        binding.onboardingIllust1.translationY = 50f

        binding.welcomeText.alpha = 0f
        binding.welcomeText.translationY = 50f

        binding.continueButton.alpha = 0f
        binding.continueButton.translationY = 50f

        binding.registButton.alpha = 0f
        binding.registButton.translationY = 50f

        // Start animations with delays
        binding.onboardingIllust1.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(500)
            .setInterpolator(FastOutSlowInInterpolator())
            .start()

        binding.welcomeText.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(500)
            .setStartDelay(200)
            .setInterpolator(FastOutSlowInInterpolator())
            .start()

        binding.continueButton.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(500)
            .setStartDelay(400)
            .setInterpolator(FastOutSlowInInterpolator())
            .start()

        binding.registButton.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(500)
            .setStartDelay(600)
            .setInterpolator(FastOutSlowInInterpolator())
            .start()

        binding.continueButton.setOnClickListener {
            val intent = Intent(activity, LoginActivity::class.java)
            startActivity(intent)
            activity?.finish()
        }

        binding.registButton.setOnClickListener {
            findNavController().navigate(R.id.action_loginRegisterFragment_to_registerAsFragment)
        }

        val sharedPref = requireActivity().getSharedPreferences("AppPreferences", AppCompatActivity.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putBoolean("isFirstRun", false)
        editor.apply()
    }

    private fun animateAndNavigate() {
        // Animate out
        binding.onboardingIllust1.animate()
            .alpha(0f)
            .translationY(-50f)
            .setDuration(300)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()

        binding.welcomeText.animate()
            .alpha(0f)
            .translationY(-50f)
            .setDuration(300)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()

        binding.continueButton.animate()
            .alpha(0f)
            .translationY(50f)
            .setDuration(300)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()

        binding.registButton.animate()
            .alpha(0f)
            .translationY(50f)
            .setDuration(300)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}