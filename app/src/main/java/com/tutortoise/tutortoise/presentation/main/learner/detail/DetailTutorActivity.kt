package com.tutortoise.tutortoise.presentation.main.learner.detail

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.tutortoise.tutortoise.R
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

    // TODO: handle ui of no reviews
    // TODO: fix the layout of the reviews
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

                    binding.tvTutoriesName.text = tutories.name
                    binding.tvCategoryName.text = tutories.categoryName
                    binding.tvRating.text = tutories.avgRating.toString()
                    binding.tvAvgRating.text = tutories.avgRating.toString()
                    binding.tvTotalReviews.text = resources.getQuantityString(
                        R.plurals.total_reviews,
                        tutories.totalReviews,
                        tutories.totalReviews,
                    )
                    binding.tvHourlyRate.text =
                        "Rp. ${tutories.hourlyRate.formatWithThousandsSeparator()} / Hour"
                    binding.tvCity.text = tutories.city

                    Glide.with(this@DetailTutorActivity)
                        .load(Constants.getProfilePictureUrl(tutories.tutorId))
                        .into(binding.ivTutorImage)

                    // Set text and manage "Read More" visibility
                    binding.tvAboutText.text = tutories.aboutYou
                    setupTextWithReadMore(binding.tvAboutText, binding.tvReadMore1)

                    binding.tvTeachingMethodologyText.text = tutories.teachingMethodology
                    setupTextWithReadMore(binding.tvTeachingMethodologyText, binding.tvReadMore2)

                    // Manage visibility of lesson type views
                    binding.tvOnlineStatus.visibility = View.GONE
                    binding.tvOnsiteStatus.visibility = View.GONE
                    when (tutories.typeLesson) {
                        LessonType.ONLINE -> {
                            binding.tvOnlineStatus.visibility = View.VISIBLE
                            binding.tvOnsiteStatus.visibility = View.GONE
                        }

                        LessonType.OFFLINE -> {
                            binding.tvOnsiteStatus.visibility = View.VISIBLE
                            binding.tvOnlineStatus.visibility = View.GONE
                        }

                        LessonType.BOTH -> {
                            binding.tvOnlineStatus.visibility = View.VISIBLE
                            binding.tvOnsiteStatus.visibility = View.VISIBLE
                        }
                    }

                    // Handle also teaches section
                    if (tutories.alsoTeaches.isEmpty()) {
                        binding.tvAlsoTeach.visibility = View.GONE
                    }
                    binding.rvAlsoTeach.layoutManager =
                        GridLayoutManager(this@DetailTutorActivity, 2)
                    binding.rvAlsoTeach.adapter = AlsoTeachAdapter(
                        tutories.alsoTeaches,
                    )

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
                putExtra("TUTOR_NAME", currentTutorName)
                putExtra("HOURLY_RATE", tutories.hourlyRate)
            }
            startActivity(intent)
        }
    }

    private fun setupTextWithReadMore(
        textView: TextView,
        readMoreButton: TextView,
        maxLines: Int = 2
    ) {
        textView.post {
            val layout = textView.layout
            val isTextLong = layout != null && layout.lineCount > maxLines

            if (isTextLong) {
                // Limit the text to maxLines
                textView.maxLines = maxLines
                textView.ellipsize = TextUtils.TruncateAt.END

                // Make read more button visible
                readMoreButton.visibility = View.VISIBLE
                readMoreButton.text = "Read More"

                // Set up toggle functionality
                readMoreButton.setOnClickListener {
                    if (textView.maxLines == maxLines) {
                        // Expand text
                        textView.maxLines = Integer.MAX_VALUE
                        textView.ellipsize = null
                        readMoreButton.text = "Read Less"
                    } else {
                        // Collapse text
                        textView.maxLines = maxLines
                        textView.ellipsize = TextUtils.TruncateAt.END
                        readMoreButton.text = "Read More"
                    }
                }
            } else {
                // If text is not long, hide read more button
                textView.maxLines = Integer.MAX_VALUE
                textView.ellipsize = null
                readMoreButton.visibility = View.GONE
            }
        }
    }

    private fun updateAboutText() {
        if (isAboutExpanded) {
            binding.tvAboutText.maxLines = Int.MAX_VALUE
            binding.tvAboutText.ellipsize = null
            binding.tvReadMore1.text = "Read Less"
        } else {
            binding.tvAboutText.maxLines = 2
            binding.tvAboutText.ellipsize = TextUtils.TruncateAt.END
            binding.tvReadMore1.text = "Read More"
        }
    }

    private fun updateTeachingMethodologyText() {
        if (isMethodologyExpanded) {
            binding.tvTeachingMethodologyText.maxLines = Int.MAX_VALUE
            binding.tvTeachingMethodologyText.ellipsize = null
            binding.tvReadMore2.text = "Read Less"
        } else {
            binding.tvTeachingMethodologyText.maxLines = 2
            binding.tvTeachingMethodologyText.ellipsize = TextUtils.TruncateAt.END
            binding.tvReadMore2.text = "Read More"
        }
    }

    override fun onDestroy() {
        coroutineScope.cancel() // Cancel coroutines when activity is destroyed
        super.onDestroy()
    }

}