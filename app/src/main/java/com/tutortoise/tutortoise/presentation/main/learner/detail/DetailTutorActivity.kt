package com.tutortoise.tutortoise.presentation.main.learner.detail

import android.content.Intent
import android.graphics.Color
import android.graphics.Rect
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.tutortoise.tutortoise.R
import com.tutortoise.tutortoise.data.model.AlsoTeachesResponse
import com.tutortoise.tutortoise.data.model.DetailedTutoriesResponse
import com.tutortoise.tutortoise.data.model.LessonType
import com.tutortoise.tutortoise.data.pref.ApiConfig
import com.tutortoise.tutortoise.data.pref.ApiService
import com.tutortoise.tutortoise.data.repository.ReviewRepository
import com.tutortoise.tutortoise.databinding.ActivityDetailTutorBinding
import com.tutortoise.tutortoise.domain.ChatManager
import com.tutortoise.tutortoise.presentation.main.learner.detail.adapter.AlsoTeachAdapter
import com.tutortoise.tutortoise.presentation.main.learner.detail.adapter.ReviewsAdapter
import com.tutortoise.tutortoise.presentation.main.learner.reservation.ReservationActivity
import com.tutortoise.tutortoise.utils.Constants
import com.tutortoise.tutortoise.utils.formatWithThousandsSeparator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DetailTutorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailTutorBinding
    private val apiService: ApiService = ApiConfig.getApiService(this)
    private val coroutineScope = CoroutineScope(Dispatchers.Main + Job())
    private var isAboutExpanded = false
    private var isMethodologyExpanded = false

    private var currentTutoriesId: String = ""
    private var currentTutorId: String = ""
    private var currentTutorName: String = ""

    private lateinit var reviewRepository: ReviewRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailTutorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentTutoriesId = intent.getStringExtra("TUTORIES_ID") ?: ""
        currentTutorId = intent.getStringExtra("TUTOR_ID") ?: ""

        reviewRepository = ReviewRepository(this)

        fetchTutoriesDetails()
        fetchReviews()

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.groupCategory.setOnClickListener {
            // On-click
        }

        // Toggle About Section
        binding.tvReadMore1.setOnClickListener {
            isAboutExpanded = !isAboutExpanded
            updateAboutText()
        }

        // Toggle Teaching Methodology Section
        binding.tvReadMore2.setOnClickListener {
            isMethodologyExpanded = !isMethodologyExpanded
            updateTeachingMethodologyText()
        }

    }

    private fun fetchReviews() {
        coroutineScope.launch {
            try {
                val reviewsResponse = reviewRepository.getTutoriesReviews(currentTutoriesId)
                reviewsResponse?.data?.let { reviews ->
                    binding.rvReview.layoutManager = LinearLayoutManager(this@DetailTutorActivity)
                    binding.rvReview.adapter = ReviewsAdapter(reviews)
                }
            } catch (e: Exception) {
                Toast.makeText(this@DetailTutorActivity, "Error: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
                Log.e("DetailTutorActivity", "Error fetching reviews", e)
            }
        }
    }

    private fun fetchTutoriesDetails() {
        coroutineScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    apiService.getTutoriesById(currentTutoriesId)
                }

                if (response.isSuccessful && response.body()?.data != null) {
                    val tutories = response.body()?.data!!

                    currentTutorName = tutories.tutorName

                    setupButtons(tutories)

                    binding.tvTutorName.text = tutories.tutorName
                    binding.tvTutoriesName.text = tutories.name
                    binding.tvCategoryName.text = tutories.categoryName

                    // Handle if rating is 0
                    if (tutories.avgRating == 0f) {
                        binding.ratingBar.visibility = View.GONE
                        binding.tvRating.text = getString(R.string.no_rating)
                    } else {
                        binding.ratingBar.visibility = View.VISIBLE
                        binding.ratingBar.rating = tutories.avgRating
                        binding.tvRating.text = tutories.avgRating.toString()

                    }

                    // Review at the bottom
                    binding.tvAvgRating.text = tutories.avgRating.toString()
                    binding.tvTotalReviews.text = resources.getQuantityString(
                        R.plurals.total_reviews_bracket,
                        tutories.totalReviews,
                        tutories.totalReviews,
                    )


                    binding.tvStudent.text = getString(
                        R.string.total_student_orders,
                        tutories.totalLearners,
                        tutories.totalOrders
                    )
                    binding.tvHourlyRate.text =
                        getString(
                            R.string.formatted_price,
                            tutories.hourlyRate.formatWithThousandsSeparator()
                        )
                    binding.tvCity.text = tutories.city

                    Glide.with(this@DetailTutorActivity)
                        .load(Constants.getProfilePictureUrl(tutories.tutorId))
                        .into(binding.ivTutorImage)

                    // Set text and manage "Read More" visibility
                    binding.tvAboutText.text = tutories.aboutYou
                    setupTextWithReadMore(binding.tvAboutText, binding.tvReadMore1)

                    binding.tvTeachingMethodologyText.text = tutories.teachingMethodology
                    setupTextWithReadMore(binding.tvTeachingMethodologyText, binding.tvReadMore2)

                    setupModeIndicators(LessonType.fromString(tutories.typeLesson))
                    setupAlsoTeachSection(tutories.alsoTeaches)
                } else {
                    // Handle error case
                    Toast.makeText(
                        this@DetailTutorActivity,
                        "Failed to load tutor details",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                // Handle network or other errors
                Toast.makeText(this@DetailTutorActivity, "Error: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
                Log.e("DetailTutorActivity", "Error fetching tutor details", e)
            }
        }
    }

    private fun setupModeIndicators(mode: LessonType) {
        when (mode) {
            LessonType.ONLINE -> {
                binding.tvOnlineStatus.apply {
                    background =
                        ContextCompat.getDrawable(context, R.drawable.ic_indicator_active_online)
                    findViewById<TextView>(R.id.tvOnline).setTextColor(Color.WHITE)
                    findViewById<ImageView>(R.id.ivOnline).setColorFilter(Color.WHITE)
                }
                binding.tvOnsiteStatus.apply {
                    background =
                        ContextCompat.getDrawable(context, R.drawable.ic_indicator_inactive_mode)
                    findViewById<TextView>(R.id.tvOnsite).setTextColor(Color.parseColor("#757575"))
                    findViewById<ImageView>(R.id.ivOnsite).setColorFilter(Color.parseColor("#757575"))
                }
            }

            LessonType.OFFLINE -> {
                binding.tvOnlineStatus.apply {
                    background =
                        ContextCompat.getDrawable(context, R.drawable.ic_indicator_inactive_mode)
                    findViewById<TextView>(R.id.tvOnline).setTextColor(Color.parseColor("#757575"))
                    findViewById<ImageView>(R.id.ivOnline).setColorFilter(Color.parseColor("#757575"))
                }
                binding.tvOnsiteStatus.apply {
                    background =
                        ContextCompat.getDrawable(context, R.drawable.ic_indicator_active_onsite)
                    findViewById<TextView>(R.id.tvOnsite).setTextColor(Color.WHITE)
                    findViewById<ImageView>(R.id.ivOnsite).setColorFilter(Color.WHITE)
                }
            }

            LessonType.BOTH -> {
                binding.tvOnlineStatus.apply {
                    background =
                        ContextCompat.getDrawable(context, R.drawable.ic_indicator_active_online)
                    findViewById<TextView>(R.id.tvOnline).setTextColor(Color.WHITE)
                    findViewById<ImageView>(R.id.ivOnline).setColorFilter(Color.WHITE)
                }
                binding.tvOnsiteStatus.apply {
                    background =
                        ContextCompat.getDrawable(context, R.drawable.ic_indicator_active_onsite)
                    findViewById<TextView>(R.id.tvOnsite).setTextColor(Color.WHITE)
                    findViewById<ImageView>(R.id.ivOnsite).setColorFilter(Color.WHITE)
                }
            }
        }
    }

    private fun setupAlsoTeachSection(alsoTeaches: List<AlsoTeachesResponse>) {
        if (alsoTeaches.isEmpty()) {
            binding.alsoTeachCard.visibility = View.GONE
            return
        }

        binding.alsoTeachCard.visibility = View.VISIBLE
        binding.rvAlsoTeach.apply {
            layoutManager = GridLayoutManager(this@DetailTutorActivity, 2)
            adapter = AlsoTeachAdapter(currentTutorId, alsoTeaches)
            addItemDecoration(
                GridSpacingItemDecoration(
                    2,
                    16
                )
            )
        }
    }

    private class GridSpacingItemDecoration(
        private val spanCount: Int,
        private val spacing: Int
    ) : RecyclerView.ItemDecoration() {

        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            val position = parent.getChildAdapterPosition(view)
            val column = position % spanCount

            outRect.left = spacing - column * spacing / spanCount
            outRect.right = (column + 1) * spacing / spanCount

            if (position < spanCount) {
                outRect.top = spacing
            }
            outRect.bottom = spacing
        }
    }


    private fun setupButtons(tutories: DetailedTutoriesResponse) {
        // Chat button setup
        binding.btnChat.setOnClickListener {
            ChatManager.navigateToChat(
                context = this@DetailTutorActivity,
                tutorId = currentTutorId,
                tutorName = currentTutorName
            )
        }

        // Reservation button setup
        binding.btnReservation.setOnClickListener {
            val intent = Intent(this, ReservationActivity::class.java).apply {
                putExtra("TUTOR_ID", currentTutorId)
                putExtra("TUTORIES_ID", currentTutoriesId)
                putExtra("TUTORIES_NAME", tutories.name)
                putExtra("TUTOR_NAME", currentTutorName)
                putExtra("HOURLY_RATE", tutories.hourlyRate)
                putExtra("TYPE_LESSON", tutories.typeLesson)
            }
            startActivity(intent)
        }
    }

    private fun setupTextWithReadMore(
        textView: TextView,
        readMoreButton: TextView,
        maxLines: Int = 4
    ) {
        // Initially hide the read more button
        readMoreButton.visibility = View.GONE

        // Wait for layout to be ready
        textView.post {
            val layout = textView.layout
            if (layout != null) {
                // Check if text spans more than maxLines
                val isTextLong = layout.lineCount > maxLines

                if (isTextLong) {
                    // Text is long enough to need read more button
                    textView.maxLines = maxLines
                    textView.ellipsize = TextUtils.TruncateAt.END
                    readMoreButton.visibility = View.VISIBLE
                    readMoreButton.text = getString(R.string.read_more)

                    readMoreButton.setOnClickListener {
                        if (textView.maxLines == maxLines) {
                            // Expand text
                            textView.maxLines = Integer.MAX_VALUE
                            textView.ellipsize = null
                            readMoreButton.text = getString(R.string.read_less)
                        } else {
                            // Collapse text
                            textView.maxLines = maxLines
                            textView.ellipsize = TextUtils.TruncateAt.END
                            readMoreButton.text = getString(R.string.read_more)
                        }
                    }
                } else {
                    // Text is short, show full text and hide read more button
                    textView.maxLines = Integer.MAX_VALUE
                    textView.ellipsize = null
                    readMoreButton.visibility = View.GONE
                }
            }
        }
    }

    private fun updateAboutText() {
        if (isAboutExpanded) {
            binding.tvAboutText.maxLines = Int.MAX_VALUE
            binding.tvAboutText.ellipsize = null
            binding.tvReadMore1.text = getString(R.string.read_less)
        } else {
            binding.tvAboutText.maxLines = 4
            binding.tvAboutText.ellipsize = TextUtils.TruncateAt.END
            binding.tvReadMore1.text = getString(R.string.read_more)
        }
    }

    private fun updateTeachingMethodologyText() {
        if (isMethodologyExpanded) {
            binding.tvTeachingMethodologyText.maxLines = Int.MAX_VALUE
            binding.tvTeachingMethodologyText.ellipsize = null
            binding.tvReadMore2.text = getString(R.string.read_less)
        } else {
            binding.tvTeachingMethodologyText.maxLines = 4
            binding.tvTeachingMethodologyText.ellipsize = TextUtils.TruncateAt.END
            binding.tvReadMore2.text = getString(R.string.read_more)
        }
    }

    override fun onDestroy() {
        coroutineScope.cancel() // Cancel coroutines when activity is destroyed
        super.onDestroy()
    }

}