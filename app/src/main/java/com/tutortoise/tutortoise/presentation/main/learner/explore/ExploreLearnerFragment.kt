package com.tutortoise.tutortoise.presentation.main.learner.explore

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.tutortoise.tutortoise.data.repository.TutoriesRepository
import com.tutortoise.tutortoise.databinding.FragmentLearnerExploreBinding
import com.tutortoise.tutortoise.presentation.main.learner.explore.adapter.ExploreAdapter
import kotlinx.coroutines.launch

class ExploreLearnerFragment : Fragment() {

    private var _binding: FragmentLearnerExploreBinding? = null
    private val binding get() = _binding!!
    private lateinit var exploreAdapter: ExploreAdapter
    private lateinit var tutoriesRepository: TutoriesRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLearnerExploreBinding.inflate(inflater, container, false)
        tutoriesRepository = TutoriesRepository(requireContext())
        fetchTutories()


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnFilter.setOnClickListener {
            FilterBottomSheet().show(childFragmentManager, "FilterDialog")
        }
    }

    private fun fetchTutories() {
        lifecycleScope.launch {
            val tutoriesItems = tutoriesRepository.searchTutories()

            if (tutoriesItems?.data != null) {
                exploreAdapter = ExploreAdapter(tutoriesItems.data)
                binding.rvTutories.apply {
                    layoutManager = LinearLayoutManager(requireContext())
                    adapter = exploreAdapter
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}