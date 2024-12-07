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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
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
import kotlin.random.Random


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

    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

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

        // Bind SwipeRefreshLayout
        swipeRefreshLayout = binding.swipeRefreshLayout
        // Set refresh listener
        swipeRefreshLayout.setOnRefreshListener {
            refreshTutories()
        }
        // For customizing swipe refresh indicator if needed
        /*swipeRefreshLayout.setColorSchemeResources(
            R.color.colorPrimary,
            R.color.colorAccent,
            R.color.colorPrimaryDark
        )*/
        setupSearch()
        setupUI()
        observeViewModel()
    }

    private fun observeViewModel() {
        // Handle initial load only if not navigating from category
        exploreViewModel.initialLoadTrigger.observe(viewLifecycleOwner) { event ->
            if (!exploreViewModel.isNavigatingFromCategory() && !exploreViewModel.isNavigatingFromSearch()) {
                event.getContentIfNotHandled()?.let {
                    fetchTutories()
                }
            }
        }

        exploreViewModel.searchQuery.observe(viewLifecycleOwner) { query ->
            if (!query.isNullOrEmpty()) {
                binding.etSearch.setText(query)
                currentSearchQuery = query
                if (exploreViewModel.isNavigatingFromSearch()) {
                    fetchTutories(query)
                }
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
            putExtra("TUTOR_ID", tutories.tutorId)
            putExtra("TUTOR_NAME", tutories.tutorName)
            putExtra("TUTORIES_ID", tutories.id)
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

    private fun refreshTutories() {
        // Trigger fetching data
        fetchTutories(currentSearchQuery)

        // Stop the refreshing animation after completion
        swipeRefreshLayout.isRefreshing = false
    }


    private fun fetchTutories(query: String? = null) {
        // Prevent duplicate fetches if already loading
        if (_binding?.shimmerFrameLayout?.visibility == View.VISIBLE) {
            return
        }

        currentSearchQuery = query
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                showLoading(true)
                showEmptyState(false)
                binding.rvTutories.visibility = View.GONE

                delay(
                    Random(1000).nextLong(100, 200)
                ) // Prevent flickering by adding a delay

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

                if (tutoriesItems?.data != null) {
                    if (tutoriesItems.data.isEmpty()) {
                        showEmptyState(true)
                        showLoading(false)
                    } else {
                        showEmptyState(false)
                        exploreAdapter.updateItems(tutoriesItems.data)
                        showLoading(false)
                    }
                } else {
                    showEmptyState(true)
                    showLoading(false)
                }
            } catch (e: Exception) {
                if (isActive) {
                    showEmptyState(true)
                    showLoading(false)
                }
            }
        }
    }

    private fun showLoading(show: Boolean) {
        _binding?.apply {
            if (show) {
                shimmerFrameLayout.visibility = View.VISIBLE
                shimmerFrameLayout.startShimmer()
                rvTutories.visibility = View.GONE
            } else {
                shimmerFrameLayout.stopShimmer()
                shimmerFrameLayout.visibility = View.GONE
            }
        }
    }

    private fun showEmptyState(show: Boolean) {
        _binding?.apply {
            if (show) {
                emptyStateLayout.visibility = View.VISIBLE
                rvTutories.visibility = View.GONE  // Hide RecyclerView when showing empty state
            } else {
                emptyStateLayout.visibility = View.GONE
                rvTutories.visibility = View.VISIBLE  // Show RecyclerView when hiding empty state
            }
        }
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