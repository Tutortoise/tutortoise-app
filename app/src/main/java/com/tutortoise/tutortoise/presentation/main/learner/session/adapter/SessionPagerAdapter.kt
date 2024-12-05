package com.tutortoise.tutortoise.presentation.main.learner.session.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.tutortoise.tutortoise.presentation.main.learner.session.CompletedLearnerSessionFragment
import com.tutortoise.tutortoise.presentation.main.learner.session.PendingLearnerSessionFragment
import com.tutortoise.tutortoise.presentation.main.learner.session.ScheduledLearnerSessionFragment

class SessionPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> ScheduledLearnerSessionFragment()
            1 -> PendingLearnerSessionFragment()
            2 -> CompletedLearnerSessionFragment()
            else -> throw IllegalStateException("Invalid position $position")
        }
    }
}