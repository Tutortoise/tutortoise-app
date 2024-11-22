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
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.navigation.fragment.findNavController
import com.tutortoise.tutortoise.R
import com.tutortoise.tutortoise.databinding.FragmentOnboarding1Binding

class OnboardingFragment1 : BaseOnboardingFragment() {

    private var _binding: FragmentOnboarding1Binding? = null
    private val binding get() = _binding!!
    private var isNavigating = false


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentOnboarding1Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Animate content area on entry
        animateContentArea(true)

        val indicators = binding.pageIndicator.let {
            listOf<ImageView>(
                it.getChildAt(0) as ImageView,
                it.getChildAt(1) as ImageView,
                it.getChildAt(2) as ImageView,
                it.getChildAt(3) as ImageView
            )
        }

        // Set initial state with animation
        val activeWidth = resources.getDimensionPixelSize(R.dimen.indicator_width_active)
        val inactiveWidth = resources.getDimensionPixelSize(R.dimen.indicator_width_inactive)

        indicators[0].animateIndicatorWidth(inactiveWidth, activeWidth)

        binding.skipButton.setOnClickListener {
            if (isNavigating) return@setOnClickListener
            isNavigating = true

            animateAndNavigateToLogin {
                findNavController().navigate(R.id.action_onboardingFragment1_to_loginRegisterFragment)
            }
        }

        binding.continueButton.setOnClickListener {
            if (isNavigating) return@setOnClickListener
            isNavigating = true

            // Start animations but navigate immediately
            indicators[0].animateIndicatorWidth(activeWidth, inactiveWidth)
            animateContentArea(false)
            findNavController().navigate(R.id.action_onboardingFragment1_to_onboardingFragment2)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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