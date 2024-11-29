package com.tutortoise.tutortoise.presentation.main.learner.explore

import FilterChipAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.tutortoise.tutortoise.R
import com.tutortoise.tutortoise.databinding.FragmentFilterSheetBinding

class FilterBottomSheet : BottomSheetDialogFragment() {
    private var _binding: FragmentFilterSheetBinding? = null
    private val binding get() = _binding!!

    private val subjectAdapter = FilterChipAdapter { updateSubjectChips() }
    private val locationAdapter = FilterChipAdapter { updateLocationChips() }

    // TODO: Replace with actual data
    private val subjects =
        listOf("Piano", "English", "Mathematics", "Computer", "Photography", "Driving")
    private val locations =
        listOf("Medan", "Bandung", "DKI Jakarta", "Samarinda", "Surabaya", "Semarang")
    private val priceRanges = listOf(
        "Rp30rb-Rp50rb",
        "Rp50rb-Rp80rb",
        "Rp80rb-Rp119rb",
        "Rp120rb++"
    )

    // Add properties to track selected items
    private var selectedSubjects = mutableSetOf<String>()
    private var selectedLocations = mutableSetOf<String>()

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

        setupInitialData()
        setupViews()
    }

    private fun setupInitialData() {
        subjectAdapter.setItems(subjects)
        locationAdapter.setItems(locations)
    }

    private fun setupViews() {
        binding.apply {
            btnReset.setOnClickListener {
                resetAllFilters()
            }

            // Setup Subject section
            updateSubjectChips()
            btnShowAllSubjects.setOnClickListener {
                subjectAdapter.toggleExpanded()
            }

            // Setup Location section
            updateLocationChips()
            btnShowAllLocations.setOnClickListener {
                locationAdapter.toggleExpanded()
            }

            // Setup Price Range chips
            setupPriceRangeChips()

            // Update show all buttons visibility
            btnShowAllSubjects.isVisible = subjectAdapter.shouldShowViewAll()
            btnShowAllLocations.isVisible = locationAdapter.shouldShowViewAll()
        }
    }

    private fun updateSubjectChips() {
        binding.apply {
            chipGroupSubject.removeAllViews()
            subjectAdapter.getVisibleItems().forEach { subject ->
                addChip(chipGroupSubject, subject).apply {
                    isCheckable = true
                    isChecked = selectedSubjects.contains(subject)
                    setOnCheckedChangeListener { _, isChecked ->
                        if (isChecked) {
                            selectedSubjects.add(subject)
                        } else {
                            selectedSubjects.remove(subject)
                        }
                    }
                }
            }
            btnShowAllSubjects.text =
                if (subjectAdapter.getExpandedState()) "Show Less" else "Show All"
            btnShowAllSubjects.isVisible = subjects.size > subjectAdapter.initialLimit
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
            }
        }
    }

    private fun resetAllFilters() {
        // Clear selected items
        selectedSubjects.clear()
        selectedLocations.clear()

        // Reset all chip groups
        binding.apply {
            // Reset subject chips
            updateSubjectChips()

            // Reset location chips
            updateLocationChips()

            // Reset price range chips
            setupPriceRangeChips()

            // Reset rating chip
            chipGroupRating.clearCheck()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}