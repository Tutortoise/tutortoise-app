package com.tutortoise.tutortoise.presentation.main.learner.home

import android.graphics.Rect
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.tutortoise.tutortoise.R
import com.tutortoise.tutortoise.data.repository.CategoryRepository
import com.tutortoise.tutortoise.data.repository.OrderRepository
import com.tutortoise.tutortoise.data.repository.ReviewRepository
import com.tutortoise.tutortoise.data.repository.TutoriesRepository
import com.tutortoise.tutortoise.databinding.FragmentLearnerHomeBinding
import com.tutortoise.tutortoise.presentation.main.MainActivity
import com.tutortoise.tutortoise.presentation.main.learner.categories.adapter.CategoriesAdapter
import com.tutortoise.tutortoise.presentation.main.learner.home.adapter.RecommendationAdapter
import com.tutortoise.tutortoise.presentation.main.learner.home.adapter.UnreviewedOrderAdapter
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class HomeLearnerFragment : Fragment() {
    private var _binding: FragmentLearnerHomeBinding? = null
    private val binding get() = _binding!!
    private val navController by lazy { findNavController() }

    private var fetchCategoriesJob: Job? = null  // Changed to separate job
    private var fetchTutoriesJob: Job? = null  // Changed to separate job

    private lateinit var categoryRepository: CategoryRepository
    private lateinit var categoriesAdapter: CategoriesAdapter
    private lateinit var orderRepository: OrderRepository
    private lateinit var reviewRepository: ReviewRepository
    private lateinit var tutoriesRepository: TutoriesRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLearnerHomeBinding.inflate(inflater, container, false)

        categoryRepository = CategoryRepository(requireContext())
        orderRepository = OrderRepository(requireContext())
        reviewRepository = ReviewRepository(requireContext())
        tutoriesRepository = TutoriesRepository(requireContext())

        setupRecyclerView()
        fetchCategories()
        fetchUnreviewedOrders()
        fetchTutoriesRecommendation()

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

    private fun setupUnreviewedRecyclerView() {
        val snapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(binding.rvUnreviewed)

        val layoutManager = object : LinearLayoutManager(requireContext(), HORIZONTAL, false) {
            override fun checkLayoutParams(lp: RecyclerView.LayoutParams): Boolean {
                lp.width = (width * 0.85).toInt()
                return true
            }

            override fun smoothScrollToPosition(
                recyclerView: RecyclerView,
                state: RecyclerView.State,
                position: Int
            ) {
                val smoothScroller = object : LinearSmoothScroller(requireContext()) {
                    override fun calculateDxToMakeVisible(view: View, snapPreference: Int): Int {
                        val out = super.calculateDxToMakeVisible(view, snapPreference)
                        return out + (recyclerView.width - view.width) / 2
                    }

                    override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics): Float {
                        return 100f / displayMetrics.densityDpi
                    }
                }
                smoothScroller.targetPosition = position
                startSmoothScroll(smoothScroller)
            }
        }

        binding.rvUnreviewed.apply {
            this.layoutManager = layoutManager
            setPadding(
                (resources.displayMetrics.widthPixels * 0.075).toInt(), // 15% of screen width
                paddingTop,
                (resources.displayMetrics.widthPixels * 0.075).toInt(),
                paddingBottom
            )
            clipToPadding = false // Allow items to enter padding area

            addItemDecoration(object : RecyclerView.ItemDecoration() {
                override fun getItemOffsets(
                    outRect: Rect,
                    view: View,
                    parent: RecyclerView,
                    state: RecyclerView.State
                ) {
                    val position = parent.getChildAdapterPosition(view)
                    if (position != state.itemCount - 1) {
                        outRect.right = resources.getDimensionPixelSize(R.dimen.rating_item_spacing)
                    }
                }
            })

            // Post a delayed center scroll
            post {
                val targetPosition = 0
                layoutManager?.scrollToPosition(targetPosition)
                postDelayed({
                    snapHelper.findSnapView(layoutManager)?.let { snapView ->
                        val snapDistance = snapHelper.calculateDistanceToFinalSnap(
                            layoutManager,
                            snapView
                        )
                        snapDistance?.let { (x, y) ->
                            if (x != 0 || y != 0) {
                                smoothScrollBy(x, y)
                            }
                        }
                    }
                }, 100)
            }
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
        fetchTutoriesJob?.cancel()
        fetchCategoriesJob?.cancel()
        _binding = null
    }

    override fun onPause() {
        super.onPause()
        binding.etSearch.text?.clear()
    }

    private fun fetchCategories() {
        fetchCategoriesJob?.cancel() // Cancel the existing job for categories
        fetchCategoriesJob = lifecycleScope.launch {
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
                        setupUnreviewedRecyclerView()

                        binding.rvUnreviewed.adapter = UnreviewedOrderAdapter(
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
                    }
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

    private fun fetchTutoriesRecommendation() {
        fetchTutoriesJob?.cancel() // Cancel the existing job for tutories recommendation
        fetchTutoriesJob = lifecycleScope.launch {
            try {
                _binding?.let { binding ->
                    binding.rvRecommendedTutors.visibility = View.GONE

                    val tutories = tutoriesRepository.getTutoriesRecommendation()

                    // Recheck binding after async operation
                    _binding?.let { binding ->
                        binding.rvRecommendedTutors.visibility = View.VISIBLE

                        tutories?.data?.recommendations?.let { recommendations ->
                            binding.rvRecommendedTutors.adapter =
                                RecommendationAdapter(recommendations)
                            binding.rvRecommendedTutors.layoutManager = LinearLayoutManager(
                                requireContext(),
                                RecyclerView.VERTICAL,
                                false
                            )
                        } ?: run {
                            binding.rvRecommendedTutors.visibility = View.GONE
                        }
                    }
                }
            } catch (e: Exception) {
                _binding?.let { binding ->
                    binding.rvRecommendedTutors.visibility = View.GONE
                }
            }
        }
    }
}