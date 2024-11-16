package com.tutortoise.tutortoise.onboarding

import android.animation.Animator
import android.animation.Animator.AnimatorListener
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.navigation.fragment.findNavController
import com.tutortoise.tutortoise.R
import com.tutortoise.tutortoise.databinding.FragmentOnboarding3Binding

class OnboardingFragment3 : Fragment() {

    private var _binding: FragmentOnboarding3Binding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentOnboarding3Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get the previous destination to determine if animation is needed
        val previousDestination = findNavController().previousBackStackEntry?.destination?.id

        if (previousDestination == R.id.onboardingFragment4) {
            // Coming back from Fragment4, animate the top bar
            binding.appName.alpha = 0f
            binding.appName.translationY = -50f
            binding.skipButton.alpha = 0f
            binding.skipButton.translationX = 50f
            animateTopBarComponents(true)
        } else {
            // Coming from Fragment2, just set the components visible without animation
            binding.appName.alpha = 1f
            binding.appName.translationY = 0f
            binding.skipButton.alpha = 1f
            binding.skipButton.translationX = 0f
        }

        animateContentArea(true)

        val indicators = binding.pageIndicator.let {
            listOf<ImageView>(
                it.getChildAt(0) as ImageView,
                it.getChildAt(1) as ImageView,
                it.getChildAt(2) as ImageView,  // Third indicator (active)
                it.getChildAt(3) as ImageView
            )
        }

        // Set initial state with animation
        val activeWidth = resources.getDimensionPixelSize(R.dimen.indicator_width_active)
        val inactiveWidth = resources.getDimensionPixelSize(R.dimen.indicator_width_inactive)

        indicators[2].animateIndicatorWidth(inactiveWidth, activeWidth)

        binding.skipButton.setOnClickListener {
            animateTopBarComponents(false)
            animateContentArea(false) {
                findNavController().navigate(R.id.action_onboardingFragment3_to_loginRegisterFragment)
            }
        }

        binding.continueButton.setOnClickListener {
            indicators[2].animateIndicatorWidth(activeWidth, inactiveWidth)
            animateTopBarComponents(false)
            animateContentArea(false) {
                findNavController().navigate(R.id.action_onboardingFragment3_to_onboardingFragment4)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun animateTopBarComponents(entering: Boolean, onAnimationEnd: () -> Unit = {}) {
        val appName = binding.appName
        val skipButton = binding.skipButton // Note: OnboardingFragment4 doesn't have skipButton

        // App name animation
        appName.animate()
            .alpha(if (entering) 1f else 0f)
            .translationY(if (entering) 0f else -50f)
            .setDuration(300)
            .setInterpolator(FastOutSlowInInterpolator())
            .start()

        // Skip button animation (only for OnboardingFragment3)
        skipButton.animate()?.alpha(if (entering) 1f else 0f)?.translationX(if (entering) 0f else 50f)
            ?.setDuration(300)?.setInterpolator(FastOutSlowInInterpolator())?.withEndAction { onAnimationEnd() }
            ?.start()
            ?: onAnimationEnd()
    }

    private fun animateContentArea(entering: Boolean, onAnimationEnd: () -> Unit = {}) {
        val contentLayout = binding.contentLayout

        contentLayout.animate()
            .alpha(if (entering) 1f else 0f)
            .setDuration(300)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .withEndAction {
                onAnimationEnd()
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