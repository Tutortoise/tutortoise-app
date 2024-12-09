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
import com.tutortoise.tutortoise.data.repository.OrderRepository
import com.tutortoise.tutortoise.data.repository.ReviewRepository
import com.tutortoise.tutortoise.databinding.FragmentLearnerHomeBinding
import com.tutortoise.tutortoise.presentation.main.MainActivity
import com.tutortoise.tutortoise.presentation.main.learner.categories.adapter.CategoriesAdapter
import com.tutortoise.tutortoise.presentation.main.learner.home.adapter.UnreviewedOrderAdapter
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class HomeLearnerFragment : Fragment() {
    private var _binding: FragmentLearnerHomeBinding? = null
    private val binding get() = _binding!!
    private val navController by lazy { findNavController() }

    private var fetchJob: Job? = null

    private lateinit var categoryRepository: CategoryRepository
    private lateinit var categoriesAdapter: CategoriesAdapter
    private lateinit var orderRepository: OrderRepository
    private lateinit var reviewRepository: ReviewRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLearnerHomeBinding.inflate(inflater, container, false)

        categoryRepository = CategoryRepository(requireContext())
        orderRepository = OrderRepository(requireContext())
        reviewRepository = ReviewRepository(requireContext())

        setupRecyclerView()
        fetchCategories()
        fetchUnreviewedOrders()

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

        binding.tvRetryCategories.setOnClickListener {
            fetchCategories()
        }

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
            setOnEditorActionListener { _, actionId, event ->
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
                    binding.tvRetryCategories.visibility = View.GONE

                    val categories = categoryRepository.fetchPopularCategories()

                    // Recheck binding after async operation
                    _binding?.let { binding ->
                        binding.categoriesShimmerLayout.stopShimmer()
                        binding.categoriesShimmerLayout.visibility = View.GONE
                        binding.rvHomeCategories.visibility = View.VISIBLE
                        binding.tvRetryCategories.visibility = View.GONE

                        categories?.data?.let { categoryList ->
                            categoriesAdapter.updateData(categoryList)
                        } ?: run {
                            binding.rvHomeCategories.visibility = View.GONE
                            binding.tvRetryCategories.visibility = View.VISIBLE
                        }
                    }
                }
            } catch (e: Exception) {
                _binding?.let { binding ->
                    binding.categoriesShimmerLayout.stopShimmer()
                    binding.categoriesShimmerLayout.visibility = View.GONE
                    binding.rvHomeCategories.visibility = View.GONE
                    binding.tvRetryCategories.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun fetchUnreviewedOrders() {
        lifecycleScope.launch {
            val unreviewedOrders = orderRepository.getUnreviewedOrders()

            unreviewedOrders.fold(
                onSuccess = { orders ->
                    if (orders.isNotEmpty()) {
                        binding.groupRateTutoring.visibility = View.VISIBLE
                    }

                    binding.rvUnreviewed.adapter =
                        UnreviewedOrderAdapter(
                            onClosed = { orderId, size ->
                                lifecycleScope.launch {
                                    reviewRepository.dismissOrderPrompt(orderId)
                                }

                                if (size == 0) {
                                    binding.groupRateTutoring.visibility = View.GONE
                                }
                            },
                            onCardClicked = { orderId ->
                                showRatingDialog(orderId)
                            },
                            onRatingClicked = { orderId, rating ->
                                showRatingDialog(orderId, rating)
                            }
                        )
                    (binding.rvUnreviewed.adapter as UnreviewedOrderAdapter).submitList(orders)
                },
                onFailure = { }
            )

        }
    }

    private fun showRatingDialog(orderId: String, rating: Float = 0f) {
        RatingTutorBottomSheetFragment.newInstance(
            orderId = orderId,
            initialRating = rating
        ) { ratedOrderId ->
            val adapter = binding.rvUnreviewed.adapter as UnreviewedOrderAdapter
            adapter.removeItem(ratedOrderId)
            if (adapter.items.size == 0) {
                binding.groupRateTutoring.visibility = View.GONE
            }
        }.show(childFragmentManager, "RatingBottomSheet")
    }
}