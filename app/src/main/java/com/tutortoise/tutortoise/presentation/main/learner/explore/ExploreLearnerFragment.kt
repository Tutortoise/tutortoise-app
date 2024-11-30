package com.tutortoise.tutortoise.presentation.main.learner.explore

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.tutortoise.tutortoise.data.model.ExploreTutoriesResponse
import com.tutortoise.tutortoise.data.repository.TutoriesRepository
import com.tutortoise.tutortoise.databinding.FragmentLearnerExploreBinding
import com.tutortoise.tutortoise.presentation.main.learner.detail.DetailTutorActivity
import com.tutortoise.tutortoise.presentation.main.learner.explore.adapter.ExploreAdapter
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch


// TODO: Handle the filters thing
class ExploreLearnerFragment : Fragment() {
    private var _binding: FragmentLearnerExploreBinding? = null
    private val binding
        get() = _binding
            ?: throw IllegalStateException("Binding should not be accessed after Fragment is destroyed")
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
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupSearch()
        setupUI()
        fetchTutories()
    }

    private fun setupUI() {
        binding.etSearch.imeOptions = EditorInfo.IME_ACTION_SEARCH
        binding.etSearch.inputType = InputType.TYPE_CLASS_TEXT

        binding.btnFilter.setOnClickListener {
            FilterBottomSheet().show(childFragmentManager, "FilterDialog")
        }

        // Initialize RecyclerView and adapter
        exploreAdapter = ExploreAdapter(emptyList()) { tutor ->
            navigateToTutorDetail(tutor)
        }
        binding.rvTutories.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = exploreAdapter
        }
    }

    // Navigate to DetailTutorActivity
    private fun navigateToTutorDetail(tutories: ExploreTutoriesResponse) {
        val intent = Intent(requireContext(), DetailTutorActivity::class.java).apply {
            putExtra("TUTORIES_ID", tutories.id)
            putExtra("TUTOR_NAME", tutories.tutorName)
            putExtra("SUBJECT_NAME", tutories.subjectName)
            putExtra("RATING", tutories.avgRating)
            putExtra("HOURLY_RATE", tutories.hourlyRate)
            putExtra("CITY", tutories.city)
            putExtra("TUTOR_ID", tutories.tutorId)
        }
        startActivity(intent)
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                searchJob?.cancel()
                searchJob = viewLifecycleOwner.lifecycleScope.launch {
                    delay(300) // Debounce for 300ms
                    if (isActive) { // Check if coroutine is still active
                        fetchTutories(s?.toString())
                    }
                }
            }
        })

        binding.etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchJob?.cancel()
                val query = binding.etSearch.text.toString()
                viewLifecycleOwner.lifecycleScope.launch {
                    if (isActive) {
                        fetchTutories(query)
                    }
                }
                hideKeyboard()
                return@setOnEditorActionListener true
            }
            false
        }
    }

    private fun fetchTutories(query: String? = null) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                showLoading(true)
                showEmptyState(false)
                binding.rvTutories.visibility = View.GONE

                val tutoriesItems = tutoriesRepository.searchTutories(query = query)

                if (!isActive) return@launch // Check if coroutine is still active

                showLoading(false)

                if (tutoriesItems?.data != null) {
                    if (tutoriesItems.data.isEmpty()) {
                        binding.rvTutories.visibility = View.GONE
                        showEmptyState(true)
                    } else {
                        binding.rvTutories.visibility = View.VISIBLE
                        showEmptyState(false)
                        exploreAdapter.updateItems(tutoriesItems.data)
                    }
                } else {
                    binding.rvTutories.visibility = View.GONE
                    showEmptyState(true)
                }
            } catch (e: Exception) {
                if (isActive) {
                    showLoading(false)
                    binding.rvTutories.visibility = View.GONE
                    showEmptyState(true)
                }
            }
        }
    }

    private fun showLoading(show: Boolean) {
        _binding?.progressBar?.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun showEmptyState(show: Boolean) {
        _binding?.emptyStateLayout?.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun hideKeyboard() {
        val imm =
            requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.etSearch.windowToken, 0)
    }

    override fun onDestroyView() {
        searchJob?.cancel() // Cancel any ongoing search job
        _binding = null
        super.onDestroyView()
    }
}