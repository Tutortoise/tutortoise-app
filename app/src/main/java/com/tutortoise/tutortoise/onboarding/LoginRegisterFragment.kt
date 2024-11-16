package com.tutortoise.tutortoise.onboarding

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.google.android.material.button.MaterialButton
import com.tutortoise.tutortoise.MainActivity
import com.tutortoise.tutortoise.R
import com.tutortoise.tutortoise.databinding.FragmentLoginRegisterBinding

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
            animateAndNavigate()
        }

        binding.registButton.setOnClickListener {
            animateAndNavigate()
        }
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
            .withEndAction {
                navigateToMainActivity()
            }
            .start()
    }

    private fun navigateToMainActivity() {
        val intent = Intent(activity, MainActivity::class.java)
        startActivity(intent)
        activity?.finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}