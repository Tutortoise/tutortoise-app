package com.tutortoise.tutortoise.presentation.onboarding.fragment

import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.tutortoise.tutortoise.R


abstract class BaseOnboardingFragment : Fragment() {
    protected fun animateAndNavigateToLogin(onAnimationEnd: () -> Unit) {
        // Animate app name
        view?.findViewById<TextView>(R.id.appName)?.animate()?.apply {
            alpha(0f)
            translationY(-50f)
            duration = 300
            interpolator = FastOutSlowInInterpolator()
            start()
        }

        // Animate skip button
        view?.findViewById<TextView>(R.id.skipButton)?.animate()?.apply {
            alpha(0f)
            translationX(50f)
            duration = 300
            interpolator = FastOutSlowInInterpolator()
            start()
        }

        // Animate content
        view?.findViewById<LinearLayout>(R.id.contentLayout)?.animate()?.apply {
            alpha(0f)
            scaleX(0.8f)
            scaleY(0.8f)
            duration = 300
            interpolator = FastOutSlowInInterpolator()
            start()
        }

        // Animate footer
        view?.findViewById<LinearLayout>(R.id.footerLayout)?.animate()?.apply {
            alpha(0f)
            translationY(50f)
            duration = 300
            interpolator = FastOutSlowInInterpolator()
            withEndAction {
                onAnimationEnd()
            }
            start()
        }
    }
}