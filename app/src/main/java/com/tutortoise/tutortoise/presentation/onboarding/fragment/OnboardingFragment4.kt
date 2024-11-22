package com.tutortoise.tutortoise.presentation.onboarding.fragment

import android.animation.Animator
import android.animation.Animator.AnimatorListener
import android.animation.ValueAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.navigation.fragment.findNavController
import com.tutortoise.tutortoise.R
import com.tutortoise.tutortoise.databinding.FragmentOnboarding4Binding

class OnboardingFragment4 : Fragment() {

    private var _binding: FragmentOnboarding4Binding? = null
    private val binding get() = _binding!!
    private var isNavigating = false


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentOnboarding4Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val previousDestination = findNavController().previousBackStackEntry?.destination?.id

        if (previousDestination == R.id.onboardingFragment3) {
            // Only animate when coming from Fragment3
            binding.appName.alpha = 0f
            binding.appName.translationY = -50f
            binding.contentLayout.alpha = 0f  // Set initial content opacity
            animateTopBarComponents(true)
            animateContentArea(true)
        } else {
            // Coming from loginRegisterFragment or initial load, set everything visible without animation
            binding.appName.alpha = 1f
            binding.appName.translationY = 0f
            binding.contentLayout.alpha = 1f  // Make content immediately visible
        }

        val sharedPref = requireActivity().getSharedPreferences("AppPreferences", AppCompatActivity.MODE_PRIVATE)

        animateContentArea(true)

        val editor = sharedPref.edit()
        editor.putBoolean("isFirstRun", false)
        editor.apply()

        val indicators = binding.pageIndicator.let {
            listOf<ImageView>(
                it.getChildAt(0) as ImageView,
                it.getChildAt(1) as ImageView,
                it.getChildAt(2) as ImageView,
                it.getChildAt(3) as ImageView  // Fourth indicator (active)
            )
        }

        // Set initial state with animation
        val activeWidth = resources.getDimensionPixelSize(R.dimen.indicator_width_active)
        val inactiveWidth = resources.getDimensionPixelSize(R.dimen.indicator_width_inactive)

        // Make sure previous indicators are in inactive state
        indicators.forEachIndexed { index, indicator ->
            if (index != 3) {
                indicator.layoutParams.width = inactiveWidth
                indicator.requestLayout()
            }
        }

        // Animate the active indicator
        indicators[3].animateIndicatorWidth(inactiveWidth, activeWidth)

        binding.continueButton.setOnClickListener {
            if (isNavigating) return@setOnClickListener
            isNavigating = true

            findNavController().navigate(R.id.action_onboardingFragment4_to_loginRegisterFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun animateTopBarComponents(entering: Boolean, onAnimationEnd: () -> Unit = {}) {
        val appName = binding.appName

        // App name animation
        appName.animate()
            .alpha(if (entering) 1f else 0f)
            .translationY(if (entering) 0f else -50f)
            .setDuration(300)
            .setInterpolator(FastOutSlowInInterpolator())
            .start()
    }

    private fun animateContentArea(entering: Boolean, onAnimationEnd: () -> Unit = {}) {
        val contentLayout = binding.contentLayout

        contentLayout.animate()
            .alpha(if (entering) 1f else 0f)
            .setDuration(300)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .withEndAction {
                onAnimationEnd()
                isNavigating = false
            }
            .start()
    }

    private fun ImageView.animateIndicatorWidth(from: Int, to: Int, onEnd: () -> Unit = {}) {
        val animator = ValueAnimator.ofInt(from, to).apply {
            duration = 300
            interpolator = FastOutSlowInInterpolator()
            addUpdateListener { animation ->
                layoutParams.width = animation.animatedValue as Int
                requestLayout()
            }
            addListener(object : AnimatorListener {
                override fun onAnimationStart(animation: Animator) {}
                override fun onAnimationEnd(animation: Animator) { onEnd() }
                override fun onAnimationCancel(animation: Animator) {}
                override fun onAnimationRepeat(animation: Animator) {}
            })
        }
        animator.start()
    }
}