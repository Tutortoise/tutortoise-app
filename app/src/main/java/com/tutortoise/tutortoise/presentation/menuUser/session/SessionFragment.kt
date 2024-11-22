package com.tutortoise.tutortoise.presentation.menuUser.session

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.tutortoise.tutortoise.databinding.FragmentLearnerSessionBinding

class SessionFragment : Fragment() {
    private lateinit var binding: FragmentLearnerSessionBinding
    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLearnerSessionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewPager = binding.viewPager
        tabLayout = binding.tabLayout

        val adapter = SessionPagerAdapter(this)
        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Scheduled"
                1 -> "Pending"
                2 -> "Completed"
                else -> ""
            }
        }.attach()
    }
}

class SessionPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> ScheduledSessionFragment()
            1 -> PendingSessionFragment()
            2 -> CompletedSessionFragment()
            else -> throw IllegalStateException("Invalid position $position")
        }
    }
}