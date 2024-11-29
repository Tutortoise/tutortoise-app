package com.tutortoise.tutortoise.presentation.main.learner.explore

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.tutortoise.tutortoise.data.repository.TutoriesRepository
import com.tutortoise.tutortoise.databinding.FragmentLearnerExploreBinding
import com.tutortoise.tutortoise.presentation.main.learner.explore.adapter.ExploreAdapter
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


// TODO: Handle the filters thing
class ExploreLearnerFragment : Fragment() {
    private var _binding: FragmentLearnerExploreBinding? = null
    private val binding get() = _binding!!
    private lateinit var exploreAdapter: ExploreAdapter
    private lateinit var tutoriesRepository: TutoriesRepository
    private var searchJob: Job? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLearnerExploreBinding.inflate(inflater, container, false)
        tutoriesRepository = TutoriesRepository(requireContext())

        setupSearch()
        fetchTutories()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.etSearch.imeOptions = EditorInfo.IME_ACTION_SEARCH
        binding.etSearch.inputType = InputType.TYPE_CLASS_TEXT

        binding.btnFilter.setOnClickListener {
            FilterBottomSheet().show(childFragmentManager, "FilterDialog")
        }
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                searchJob?.cancel()
                searchJob = lifecycleScope.launch {
                    delay(300) // Debounce for 300ms
                    if (s.isNullOrBlank()) {
                        fetchTutories()
                    } else {
                        fetchTutories(s.toString())
                    }
                }
            }
        })

        binding.etSearch.setOnEditorActionListener { _, actionId, _ ->

            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchJob?.cancel()
                val query = binding.etSearch.text.toString()
                fetchTutories(query)
                hideKeyboard()
                return@setOnEditorActionListener true
            }
            false
        }
    }

    private fun fetchTutories(query: String? = null) {
        lifecycleScope.launch {
            // Hide both states initially
            showLoading(true)
            showEmptyState(false)
            binding.rvTutories.isVisible = false

            val tutoriesItems = tutoriesRepository.searchTutories(query = query)

            // Hide loading first
            showLoading(false)

            if (tutoriesItems?.data != null) {
                if (tutoriesItems.data.isEmpty()) {
                    // Show empty state if no data
                    binding.rvTutories.isVisible = false
                    showEmptyState(true)
                } else {
                    // Show recycler view with data
                    binding.rvTutories.isVisible = true
                    showEmptyState(false)

                    if (::exploreAdapter.isInitialized) {
                        exploreAdapter.updateItems(tutoriesItems.data)
                    } else {
                        exploreAdapter = ExploreAdapter(tutoriesItems.data)
                        binding.rvTutories.apply {
                            layoutManager = LinearLayoutManager(requireContext())
                            adapter = exploreAdapter
                        }
                    }
                }
            } else {
                binding.rvTutories.isVisible = false
                showEmptyState(true)
            }
        }
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.isVisible = show
    }

    private fun showEmptyState(show: Boolean) {
        binding.emptyStateLayout.isVisible = show
    }

    private fun hideKeyboard() {
        val imm =
            requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.etSearch.windowToken, 0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}