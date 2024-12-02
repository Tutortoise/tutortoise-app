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
import com.tutortoise.tutortoise.domain.ChatManager
import com.tutortoise.tutortoise.presentation.main.learner.detail.adapter.AlsoTeachAdapter
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailTutorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentTutoriesId = intent.getStringExtra("TUTORIES_ID") ?: ""
        currentTutorId = intent.getStringExtra("TUTOR_ID") ?: ""

        fetchTutoriesDetails()

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

    private fun fetchTutoriesDetails() {
        coroutineScope.launch {
            try {
                // Assuming you have a method to get tutories by ID in your repository
                val response = withContext(Dispatchers.IO) {
                    apiService.getTutoriesById(currentTutoriesId)
                }

                if (response.isSuccessful && response.body()?.data != null) {
                    val tutories = response.body()?.data!!

                    binding.tvTutorName.text = tutories.tutorName
                    binding.tvCategoryName.text = tutories.categoryName
                    // TODO: avg rating
//                    binding.tvRating.text = tutories.avgRating.toString()
                    binding.tvHourlyRate.text =
                        "Rp. ${tutories.hourlyRate.formatWithThousandsSeparator()} / Hour"
                    binding.tvCity.text = tutories.city

                    Glide.with(this@DetailTutorActivity)
                        .load(Constants.getProfilePictureUrl(tutories.tutorId))
                        .into(binding.ivTutorImage)

                    binding.tvAboutText.text = tutories.aboutYou
                    binding.tvTeachingMethodologyText.text =
                        tutories.teachingMethodology

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

        // Chat button click listener
        binding.btnChat.setOnClickListener {
            ChatManager.navigateToChat(this, currentTutorId)
        }

        // Reservation button click listener
        binding.btnReservation.setOnClickListener {
            val intent = Intent(this, ReservationActivity::class.java).apply {
                putExtra("TUTOR_ID", currentTutorId)
                putExtra("TUTORIES_ID", currentTutoriesId)
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