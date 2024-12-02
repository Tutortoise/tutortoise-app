package com.tutortoise.tutortoise.presentation.main.learner.explore

import FilterChipAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.tutortoise.tutortoise.R
import com.tutortoise.tutortoise.data.model.CategoryResponse
import com.tutortoise.tutortoise.data.model.LessonType
import com.tutortoise.tutortoise.data.repository.CategoryRepository
import com.tutortoise.tutortoise.data.repository.TutoriesRepository
import com.tutortoise.tutortoise.databinding.FragmentFilterSheetBinding
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class FilterBottomSheet : BottomSheetDialogFragment() {
    private var _binding: FragmentFilterSheetBinding? = null
    private val binding get() = _binding!!

    private val categoryAdapter = FilterChipAdapter<CategoryResponse> { updateCategoryChips() }
    private val locationAdapter = FilterChipAdapter<String> { updateLocationChips() }

    private lateinit var categoryRepository: CategoryRepository
    private lateinit var tutoriesRepository: TutoriesRepository

    private var categories: List<CategoryResponse> = emptyList()
    private var locations: List<String> = emptyList()
    private val priceRanges = listOf(
        "Rp30rb-Rp50rb",
        "Rp50rb-Rp80rb",
        "Rp80rb-Rp119rb",
        "Rp120rb++"
    )

    // Add properties to track selected items
    private var selectedCategories = mutableSetOf<CategoryResponse>()
    private var selectedLocations = mutableSetOf<String>()

    private var currentFilterState = FilterState()
    private var onFilterChanged: ((FilterState) -> Unit)? = null

    companion object {
        fun newInstance(
            currentState: FilterState?,
            onFilterChanged: (FilterState) -> Unit
        ): FilterBottomSheet {
            return FilterBottomSheet().apply {
                this.currentFilterState = currentState ?: FilterState()
                this.onFilterChanged = onFilterChanged
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFilterSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        categoryRepository = CategoryRepository(requireContext())
        tutoriesRepository = TutoriesRepository(requireContext())

        // Restore selected states from currentFilterState
        selectedCategories.addAll(currentFilterState.categories)
        selectedLocations.addAll(currentFilterState.locations)

        setupInitialData()
        setupViews()
    }

    private fun setupInitialData() {
        lifecycleScope.launch {
            val categoriesDeferred = async { categoryRepository.fetchCategories() }
            val locationsDeferred = async { tutoriesRepository.fetchLocations() }

            val categoriesResponse = categoriesDeferred.await()?.data
            val locationsResponse = locationsDeferred.await()?.data

            categoriesResponse?.let { data ->
                categories = data
                categoryAdapter.setItems(categories)
                updateCategoryChips()
            }

            locationsResponse?.let { data ->
                locations = data.cities
                locationAdapter.setItems(locations)
                updateLocationChips()
            }
        }
    }


    private fun setupViews() {
        binding.apply {
            btnReset.setOnClickListener {
                resetAllFilters()
            }

            // Setup Category section
            updateCategoryChips()
            btnShowAllCategory.setOnClickListener {
                categoryAdapter.toggleExpanded()
            }

            // Setup Location section
            updateLocationChips()
            btnShowAllLocations.setOnClickListener {
                locationAdapter.toggleExpanded()
            }

            // Setup Price Range chips
            setupPriceRangeChips()
            // Restore price range selection
            currentFilterState.priceRange?.let { range ->
                priceRanges.forEachIndexed { index, price ->
                    if (PriceRange.fromString(price)?.min == range.min) {
                        chipGroupPriceRange.check(
                            chipGroupPriceRange.getChildAt(index).id
                        )
                    }
                }
            }

            // Setup Rating chips
            setupRatingChips()
            // Restore rating selection
            currentFilterState.rating?.let { rating ->
                chipGroupRating.children.forEach { chip ->
                    if ((chip as Chip).text.toString().replace("+", "").toFloatOrNull() == rating) {
                        chip.isChecked = true
                    }
                }
            }

            // Setup Lesson Type chips and restore selection
            chipGroupLessonType.setOnCheckedStateChangeListener { _, _ ->
                updateFilters()
            }
            // Restore lesson type selection
            currentFilterState.lessonType?.let { type ->
                val chipId = when (type) {
                    LessonType.ONLINE -> R.id.chipOnline
                    LessonType.OFFLINE -> R.id.chipOffline
                    LessonType.BOTH -> R.id.chipBoth
                    else -> null
                }
                chipId?.let { chipGroupLessonType.check(it) }
            }

            // Update show all buttons visibility
            btnShowAllCategory.isVisible = categoryAdapter.shouldShowViewAll()
            btnShowAllLocations.isVisible = locationAdapter.shouldShowViewAll()
        }
    }

    private fun updateCategoryChips() {
        binding.apply {
            chipGroupCategory.removeAllViews()
            categoryAdapter.getVisibleItems().forEach { category ->
                addChip(chipGroupCategory, category.toString()).apply {
                    isCheckable = true
                    isChecked = selectedCategories.contains(category)
                    setOnCheckedChangeListener { _, isChecked ->
                        if (isChecked) {
                            selectedCategories.add(category)
                        } else {
                            selectedCategories.remove(category)
                        }
                        // Trigger filter update
                        updateFilters()
                    }
                }
            }
            btnShowAllCategory.text =
                if (categoryAdapter.getExpandedState()) "Show Less" else "Show All"
            btnShowAllCategory.isVisible = categories.size > categoryAdapter.initialLimit
        }
    }

    private fun updateLocationChips() {
        binding.apply {
            chipGroupLocation.removeAllViews()
            locationAdapter.getVisibleItems().forEach { location ->
                addChip(chipGroupLocation, location).apply {
                    isCheckable = true
                    isChecked = selectedLocations.contains(location)
                    setOnCheckedChangeListener { _, isChecked ->
                        if (isChecked) {
                            selectedLocations.add(location)
                        } else {
                            selectedLocations.remove(location)
                        }
                        // Trigger filter update
                        updateFilters()
                    }
                }
            }
            btnShowAllLocations.text =
                if (locationAdapter.getExpandedState()) "Show Less" else "Show All"
            btnShowAllLocations.isVisible = locations.size > locationAdapter.initialLimit
        }
    }

    private fun addChip(chipGroup: ChipGroup, text: String): Chip {
        val chip = layoutInflater.inflate(
            R.layout.item_filter_chip,
            chipGroup,
            false
        ) as Chip
        chip.text = text
        chipGroup.addView(chip)
        return chip
    }

    private fun setupPriceRangeChips() {
        binding.chipGroupPriceRange.removeAllViews()
        priceRanges.forEach { price ->
            addChip(binding.chipGroupPriceRange, price).apply {
                isCheckable = true
                setOnCheckedChangeListener { _, _ ->
                    // Trigger filter update
                    updateFilters()
                }
            }
        }
    }

    private fun setupRatingChips() {
        binding.chipGroupRating.setOnCheckedStateChangeListener { _, _ ->
            // Trigger filter update
            updateFilters()
        }
    }

    private fun getSelectedPriceRange(): PriceRange? {
        val checkedChipId = binding.chipGroupPriceRange.checkedChipId
        if (checkedChipId != View.NO_ID) {
            val chip = binding.chipGroupPriceRange.findViewById<Chip>(checkedChipId)
            return PriceRange.fromString(chip.text.toString())
        }
        return null
    }

    private fun getSelectedRating(): Float? {
        val checkedChipId = binding.chipGroupRating.checkedChipId
        if (checkedChipId != View.NO_ID) {
            val chip = binding.chipGroupRating.findViewById<Chip>(checkedChipId)
            return chip.text.toString().replace("+", "").toFloatOrNull()
        }
        return null
    }

    private fun getSelectedLessonType(): String? {
        val checkedChipId = binding.chipGroupLessonType?.checkedChipId
        return when (checkedChipId) {
            R.id.chipOnline -> LessonType.ONLINE
            R.id.chipOffline -> LessonType.OFFLINE
            R.id.chipBoth -> LessonType.BOTH
            else -> null
        }
    }

    private fun updateFilters() {
        val newFilterState = FilterState(
            categories = selectedCategories,
            locations = selectedLocations,
            priceRange = getSelectedPriceRange(),
            rating = getSelectedRating(),
            lessonType = getSelectedLessonType()
        )
        currentFilterState = newFilterState
        onFilterChanged?.invoke(newFilterState)
        updateFilterBadge()
    }

    private fun resetAllFilters() {
        selectedCategories.clear()
        selectedLocations.clear()
        binding.apply {
            chipGroupPriceRange.clearCheck()
            chipGroupRating.clearCheck()
            chipGroupLessonType.clearCheck()
            updateCategoryChips()
            updateLocationChips()
        }

        currentFilterState = FilterState()
        onFilterChanged?.invoke(currentFilterState)
        updateFilterBadge()
    }

    private fun updateFilterBadge() {
        val activeFilters = listOfNotNull(
            selectedCategories.takeIf { it.isNotEmpty() },
            selectedLocations.takeIf { it.isNotEmpty() },
            getSelectedPriceRange(),
            getSelectedRating(),
            getSelectedLessonType()
        ).size

        // Update filter badge in parent fragment
        (parentFragment as? ExploreLearnerFragment)?.updateFilterBadge(activeFilters)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}