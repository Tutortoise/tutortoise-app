package com.tutortoise.tutortoise.presentation.main.learner.detail

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.tutortoise.tutortoise.data.model.LessonType
import com.tutortoise.tutortoise.data.pref.ApiConfig
import com.tutortoise.tutortoise.data.pref.ApiService
import com.tutortoise.tutortoise.databinding.ActivityDetailTutorBinding
import com.tutortoise.tutortoise.presentation.main.learner.detail.adapter.AlsoTeachAdapter
import com.tutortoise.tutortoise.presentation.main.learner.reservation.ReservationActivity
import com.tutortoise.tutortoise.utils.ChatUtils
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

    private var currentTutorId: String = ""
    private var currentTutorName: String = ""
    private var currentCity: String = ""
    private var currentRating: Float = 0f
    private var currentHourlyRate: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailTutorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentTutorId = intent.getStringExtra("TUTOR_ID") ?: ""
        currentTutorName = intent.getStringExtra("TUTOR_NAME") ?: ""
        currentCity = intent.getStringExtra("CITY") ?: ""
        currentRating = intent.getFloatExtra("RATING", 0f)
        currentHourlyRate = intent.getIntExtra("HOURLY_RATE", 0)

        val tutoriesId = intent.getStringExtra("TUTORIES_ID") ?: ""
        val subjectName = intent.getStringExtra("SUBJECT_NAME") ?: ""

        // Use the data to populate your UI
        binding.tvTutorName.text = currentTutorName
        binding.tvTutorSubject.text = subjectName
        binding.tvRating.text = currentRating.toString()
        binding.tvHourlyRate.text = "Rp. ${currentHourlyRate.formatWithThousandsSeparator()} / Hour"
        binding.tvCity.text = currentCity

        Glide.with(this)
            .load(Constants.getProfilePictureUrl(currentTutorId))
            .into(binding.ivTutorImage)

        fetchTutoriesDetails(tutoriesId)

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

    private fun fetchTutoriesDetails(tutoriesId: String) {
        coroutineScope.launch {
            try {
                // Assuming you have a method to get tutories by ID in your repository
                val response = withContext(Dispatchers.IO) {
                    apiService.getTutoriesById(tutoriesId)
                }

                if (response.isSuccessful && response.body()?.data != null) {
                    val detailedTutor = response.body()?.data!!

                    // Update UI with additional details from DetailedTutoriesResponse
                    binding.tvAboutText.text = detailedTutor.tutories.aboutYou
                    binding.tvTeachingMethodologyText.text =
                        detailedTutor.tutories.teachingMethodology

                    // Manage visibility of lesson type views
                    binding.tvOnlineStatus.visibility = View.GONE
                    binding.tvOnsiteStatus.visibility = View.GONE
                    when (detailedTutor.tutories.typeLesson) {
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
                    if (detailedTutor.alsoTeaches.isEmpty()) {
                        binding.tvAlsoTeach.visibility = View.GONE
                    }
                    binding.rvAlsoTeach.layoutManager =
                        GridLayoutManager(this@DetailTutorActivity, 2)
                    binding.rvAlsoTeach.adapter = AlsoTeachAdapter(
                        detailedTutor.alsoTeaches,
                        currentTutorId,
                        currentTutorName,
                        currentCity,
                        currentRating
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

        // Chat button click listener
        binding.btnChat.setOnClickListener {
            ChatUtils.navigateToChat(this, currentTutorId)
        }

        // Reservation button click listener
        binding.btnReservation.setOnClickListener {
            val intent = Intent(this, ReservationActivity::class.java).apply {
                putExtra("TUTOR_ID", currentTutorId)
                putExtra("TUTOR_NAME", currentTutorName)
                putExtra("HOURLY_RATE", currentHourlyRate)
            }
            startActivity(intent)
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