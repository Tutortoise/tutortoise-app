package com.tutortoise.tutortoise.presentation.questioner

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.flexbox.AlignItems
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.tutortoise.tutortoise.data.repository.CategoryRepository
import com.tutortoise.tutortoise.data.repository.LearnerRepository
import com.tutortoise.tutortoise.databinding.FragmentQuestionerInterestBinding
import com.tutortoise.tutortoise.presentation.questioner.adapter.CategoryAdapter
import kotlinx.coroutines.launch

class QuestionerInterestFragment : Fragment() {
    private var _binding: FragmentQuestionerInterestBinding? = null
    private val binding get() = _binding!!

    private val viewModel: QuestionerInterestViewModel by viewModels {
        QuestionerInterestViewModel.provideFactory(
            LearnerRepository(requireContext()),
            CategoryRepository(requireContext())
        )
    }

    private val selectedInterests = mutableListOf<String>()
    private lateinit var categoryAdapter: CategoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQuestionerInterestBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupObservers()
        setupListeners()
        viewModel.fetchCategories()
    }

    private fun setupRecyclerView() {
        categoryAdapter = CategoryAdapter(
            maxSelections = 3,
            onSelectionChanged = { selected ->
                selectedInterests.clear()
                selectedInterests.addAll(selected)
                binding.continueButton.isEnabled = selected.isNotEmpty()
            }
        )

        val flexboxLayoutManager = FlexboxLayoutManager(context).apply {
            flexDirection = FlexDirection.ROW
            justifyContent = JustifyContent.CENTER
            alignItems = AlignItems.CENTER
        }

        binding.rvCategories.apply {
            layoutManager = flexboxLayoutManager
            adapter = categoryAdapter
        }
    }

    private fun setupObservers() {
        viewModel.categories.observe(viewLifecycleOwner) { categories ->
            categoryAdapter.submitList(categories)
        }
    }

    private fun setupListeners() {
        binding.continueButton.isEnabled = false
        binding.continueButton.setOnClickListener {
            if (selectedInterests.isNotEmpty()) {
                lifecycleScope.launch {
                    viewModel.updateInterests(selectedInterests).collect { result ->
                        result.fold(
                            onSuccess = {
                                (activity as? QuestionnaireActivity)?.navigateToNext()
                            },
                            onFailure = {
                                Toast.makeText(
                                    context,
                                    "Failed to update interests",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    (activity as? QuestionnaireActivity)?.showExitConfirmationDialog()
                }
            }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}