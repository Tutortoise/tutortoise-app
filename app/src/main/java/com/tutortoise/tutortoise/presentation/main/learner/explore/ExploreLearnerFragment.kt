package com.tutortoise.tutortoise.presentation.main.learner.explore

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.tutortoise.tutortoise.R
import com.tutortoise.tutortoise.data.model.CategoryResponse
import com.tutortoise.tutortoise.data.model.ExploreTutoriesResponse
import com.tutortoise.tutortoise.data.repository.TutoriesRepository
import com.tutortoise.tutortoise.databinding.FragmentLearnerExploreBinding
import com.tutortoise.tutortoise.presentation.main.learner.detail.DetailTutorActivity
import com.tutortoise.tutortoise.presentation.main.learner.explore.adapter.ExploreAdapter
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch


class ExploreLearnerFragment : Fragment() {
    private val exploreViewModel: ExploreViewModel by activityViewModels()
    private var _binding: FragmentLearnerExploreBinding? = null
    private val binding
        get() = _binding
            ?: throw IllegalStateException("Binding should not be accessed after Fragment is destroyed")
    private lateinit var exploreAdapter: ExploreAdapter
    private lateinit var tutoriesRepository: TutoriesRepository
    private var searchJob: Job? = null

    private var currentFilterState: FilterState? = null
    private var currentSearchQuery: String? = null

    private var pendingSearchQuery: String? = null
    private var pendingCategoriesFilter: CategoryResponse? = null

    override fun onResume() {
        super.onResume()
        // Handle any pending actions when fragment resumes
        handlePendingActions()
    }


    private fun handlePendingActions() {
        // Handle pending search query
        pendingSearchQuery?.let { query ->
            setSearchQuery(query)
            pendingSearchQuery = null
        }

        // Handle pending categories filter
        pendingCategoriesFilter?.let { category ->
            setCategoryFilter(category)
            pendingCategoriesFilter = null
        }
    }

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
        observeViewModel()
        fetchTutories()
    }

    private fun observeViewModel() {
        // Observe search query changes
        exploreViewModel.searchQuery.observe(viewLifecycleOwner) { query ->
            if (!query.isNullOrEmpty()) {
                binding.etSearch.setText(query)
                currentSearchQuery = query
                fetchTutories(query)
            }
        }

        // Observe category selection changes
        exploreViewModel.selectedCategory.observe(viewLifecycleOwner) { category ->
            category?.let {
                // Clear any existing search
                binding.etSearch.text?.clear()
                currentSearchQuery = null

                // Update filter state with the selected category
                currentFilterState = FilterState(
                    categories = setOf(it),
                    locations = emptySet(),
                    priceRange = null,
                    rating = null,
                    lessonType = null
                )

                // Update UI
                updateFilterBadge(1)

                // Log the filter state
                println("Setting category filter: ${it.name}")
                println("Current filter state categories: ${currentFilterState?.categories?.map { sub -> sub.id }}")

                // Fetch tutories with new filter
                fetchTutories()
            }
        }
    }

    private fun setupUI() {
        binding.etSearch.imeOptions = EditorInfo.IME_ACTION_SEARCH
        binding.etSearch.inputType = InputType.TYPE_CLASS_TEXT

        // Initialize RecyclerView and adapter
        exploreAdapter = ExploreAdapter(emptyList()) { tutor ->
            navigateToTutorDetail(tutor)
        }
        binding.rvTutories.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = exploreAdapter
        }

        binding.btnFilter.setOnClickListener {
            showFilterBottomSheet()
        }
    }

    private fun showFilterBottomSheet() {
        FilterBottomSheet.newInstance(
            currentState = currentFilterState,
            onFilterChanged = { filterState ->
                currentFilterState = filterState
                fetchTutories(currentSearchQuery)
            }
        ).show(childFragmentManager, "FilterDialog")
    }

    // Navigate to DetailTutorActivity
    private fun navigateToTutorDetail(tutories: ExploreTutoriesResponse) {
        val intent = Intent(requireContext(), DetailTutorActivity::class.java).apply {
            putExtra("TUTORIES_ID", tutories.id)
            putExtra("TUTOR_NAME", tutories.tutorName)
            putExtra("CATEGORY_NAME", tutories.categoryName)
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
        currentSearchQuery = query
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                showLoading(true)
                showEmptyState(false)
                binding.rvTutories.visibility = View.GONE

                // Debug logging
                println("Fetching tutories with:")
                println("Query: $query")
                println("Category IDs: ${currentFilterState?.categories?.map { it.id }}")

                val tutoriesItems = tutoriesRepository.searchTutories(
                    query = query?.takeIf { it.isNotEmpty() },
                    categoryIds = currentFilterState?.categories?.map { it.id },
                    cities = currentFilterState?.locations?.toList(),
                    minPrice = currentFilterState?.priceRange?.min,
                    maxPrice = currentFilterState?.priceRange?.max,
                    minRating = currentFilterState?.rating,
                    lessonType = currentFilterState?.lessonType
                )

                if (!isActive) return@launch

                showLoading(false)

                if (tutoriesItems?.data != null) {
                    binding.rvTutories.visibility = View.VISIBLE
                    showEmptyState(tutoriesItems.data.isEmpty())
                    exploreAdapter.updateItems(tutoriesItems.data)
                } else {
                    binding.rvTutories.visibility = View.GONE
                    showEmptyState(true)
                }
            } catch (e: Exception) {
                println("Error fetching tutories: ${e.message}")
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

    fun updateFilterBadge(activeFilters: Int) {
        binding.btnFilter.apply {
            if (activeFilters > 0) {
                setTextColor(ContextCompat.getColor(context, R.color.darkgreen))
                iconTint =
                    ColorStateList.valueOf(ContextCompat.getColor(context, R.color.darkgreen))
            } else {
                setTextColor(ContextCompat.getColor(context, R.color.colorInactive))
                iconTint =
                    ColorStateList.valueOf(ContextCompat.getColor(context, R.color.colorInactive))
            }
        }
    }

    fun setSearchQuery(query: String) {
        if (!isResumed) {
            // If fragment is not resumed, save for later
            pendingSearchQuery = query
            return
        }

        // Update search UI and trigger search
        _binding?.let { binding ->
            binding.etSearch.setText(query)
            currentSearchQuery = query
            fetchTutories(query)
        }
    }

    fun setCategoryFilter(category: CategoryResponse) {
        if (!isResumed) {
            // If fragment is not resumed, save for later
            pendingCategoriesFilter = category
            return
        }

        // Update filter state
        currentFilterState = FilterState(
            categories = setOf(category),
            locations = currentFilterState?.locations ?: emptySet(),
            priceRange = currentFilterState?.priceRange,
            rating = currentFilterState?.rating,
            lessonType = currentFilterState?.lessonType
        )

        // Update UI
        updateFilterBadge(1)

        // Trigger search with new filter
        fetchTutories(currentSearchQuery)
    }

    override fun onDestroyView() {
        searchJob?.cancel() // Cancel any ongoing search job
        _binding = null
        super.onDestroyView()
    }

    override fun onPause() {
        super.onPause()
        binding.etSearch.text?.clear()
        exploreViewModel.clearData()
    }
}