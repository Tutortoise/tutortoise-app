package com.tutortoise.tutortoise.presentation.main.learner.home

import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.ProgressBar
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
import kotlinx.coroutines.launch

class HomeLearnerFragment : Fragment() {
    private var _binding: FragmentLearnerHomeBinding? = null
    private val binding get() = _binding!!
    private val navController by lazy { findNavController() }
    private lateinit var progressBar: ProgressBar

    private lateinit var categoryRepository: CategoryRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLearnerHomeBinding.inflate(inflater, container, false)

        progressBar = binding.progressBar

        categoryRepository = CategoryRepository(requireContext())

        // Fetch categories
        fetchCategories()

        return binding.root
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
        _binding = null
    }

    override fun onPause() {
        super.onPause()
        binding.etSearch.text?.clear()
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }


    private fun fetchCategories() {
        val recyclerView: RecyclerView = binding.rvHomeCategories
        recyclerView.layoutManager =
            LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
        showLoading(true) // Show ProgressBar
        lifecycleScope.launch {
            try {
                val categories = categoryRepository.fetchPopularCategories()
                binding.rvHomeCategories.adapter = categories?.data?.let { categoryList ->
                    CategoriesAdapter(categoryList) { clickedCategory ->
                        (activity as? MainActivity)?.navigateToExploreWithCategory(clickedCategory)
                    }
                }
            } catch (e: Exception) {
                // Handle error
            } finally {
                showLoading(false) // Hide ProgressBar
            }
        }
    }

}