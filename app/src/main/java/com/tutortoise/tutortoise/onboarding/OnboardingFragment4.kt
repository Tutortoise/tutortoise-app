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
import androidx.appcompat.app.AppCompatActivity
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.navigation.fragment.findNavController
import com.tutortoise.tutortoise.R
import com.tutortoise.tutortoise.databinding.FragmentOnboarding4Binding

class OnboardingFragment4 : Fragment() {

    private var _binding: FragmentOnboarding4Binding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentOnboarding4Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val sharedPref = requireActivity().getSharedPreferences("AppPreferences", AppCompatActivity.MODE_PRIVATE)
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
            // Animate current indicator back to dot
            indicators[3].animateIndicatorWidth(activeWidth, inactiveWidth) {
                // Navigate after animation completes
                findNavController().navigate(R.id.action_onboardingFragment4_to_loginRegisterFragment)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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