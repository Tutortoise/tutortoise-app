package com.tutortoise.tutortoise.presentation.onboarding.adapter

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.tutortoise.tutortoise.presentation.onboarding.fragment.OnboardingFragment1
import com.tutortoise.tutortoise.presentation.onboarding.fragment.OnboardingFragment2
import com.tutortoise.tutortoise.presentation.onboarding.fragment.OnboardingFragment3
import com.tutortoise.tutortoise.presentation.onboarding.fragment.OnboardingFragment4

class OnboardingAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int {
        return 4
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> OnboardingFragment1()
            1 -> OnboardingFragment2()
            2 -> OnboardingFragment3()
            3 -> OnboardingFragment4()
            else -> throw IllegalStateException("Unexpected position: $position")
        }
    }
}