package com.tutortoise.tutortoise.presentation.main.learner.home

import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tutortoise.tutortoise.R
import com.tutortoise.tutortoise.data.repository.CategoryRepository
import com.tutortoise.tutortoise.databinding.FragmentLearnerHomeBinding
import com.tutortoise.tutortoise.presentation.main.MainActivity
import com.tutortoise.tutortoise.presentation.main.learner.categories.adapter.CategoriesAdapter
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class HomeLearnerFragment : Fragment() {
    private var _binding: FragmentLearnerHomeBinding? = null
    private val binding get() = _binding!!
    private val navController by lazy { findNavController() }

    private var fetchJob: Job? = null

    private lateinit var categoryRepository: CategoryRepository
    private lateinit var categoriesAdapter: CategoriesAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLearnerHomeBinding.inflate(inflater, container, false)

        categoryRepository = CategoryRepository(requireContext())

        setupRecyclerView()
        fetchCategories()

        return binding.root
    }

    private fun setupRecyclerView() {
        binding.rvHomeCategories.apply {
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
            categoriesAdapter = CategoriesAdapter(emptyList()) { clickedCategory ->
                (activity as? MainActivity)?.navigateToExploreWithCategory(clickedCategory)
            }
            adapter = categoriesAdapter
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.notification.setOnClickListener {
            navController.navigate(R.id.action_home_to_notification)
        }

        binding.chat.setOnClickListener {
            navController.navigate(R.id.action_home_to_chat)
        }

        binding.seemore.setOnClickListener {
            navController.navigate(R.id.action_home_to_categories)
        }

        binding.etSearch.apply {
            imeOptions = EditorInfo.IME_ACTION_SEARCH
            setOnEditorActionListener { v, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    (event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)
                ) {
                    val query = text.toString()
                    if (query.isNotEmpty()) {
                        (activity as? MainActivity)?.navigateToExploreWithSearch(query)
                        return@setOnEditorActionListener true
                    }
                }
                false
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        fetchJob?.cancel()
        _binding = null
    }

    override fun onPause() {
        super.onPause()
        binding.etSearch.text?.clear()
    }


    private fun fetchCategories() {
        fetchJob?.cancel()
        fetchJob = lifecycleScope.launch {
            try {
                _binding?.let { binding ->
                    binding.categoriesShimmerLayout.visibility = View.VISIBLE
                    binding.categoriesShimmerLayout.startShimmer()
                    binding.rvHomeCategories.visibility = View.GONE

                    val categories = categoryRepository.fetchPopularCategories()

                    // Recheck binding after async operation
                    _binding?.let { binding ->
                        binding.categoriesShimmerLayout.stopShimmer()
                        binding.categoriesShimmerLayout.visibility = View.GONE
                        binding.rvHomeCategories.visibility = View.VISIBLE

                        categories?.data?.let { categoryList ->
                            categoriesAdapter.updateData(categoryList)
                        }
                    }
                }
            } catch (e: Exception) {
                _binding?.let { binding ->
                    binding.categoriesShimmerLayout.stopShimmer()
                    binding.categoriesShimmerLayout.visibility = View.GONE
                }
            }
        }
    }

}