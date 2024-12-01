package com.tutortoise.tutortoise.presentation.main.learner.detail

import android.os.Bundle
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
import com.tutortoise.tutortoise.utils.Constants
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailTutorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val tutoriesId = intent.getStringExtra("TUTORIES_ID")!!
        val tutorName = intent.getStringExtra("TUTOR_NAME")!!
        val subjectName = intent.getStringExtra("SUBJECT_NAME")!!
        val rating = intent.getFloatExtra("RATING", 0f)
        val hourlyRate = intent.getIntExtra("HOURLY_RATE", 0)
        val city = intent.getStringExtra("CITY")!!
        val tutorId = intent.getStringExtra("TUTOR_ID")!!

        // Use the data to populate your UI
        binding.tvTutorName.text = tutorName
        binding.tvTutorSubject.text = subjectName
        binding.tvRating.text = rating.toString()
        binding.tvHourlyRate.text = "Rp. $hourlyRate / Hour"
        binding.tvCity.text = city

        Glide.with(this)
            .load(Constants.getProfilePictureUrl(tutorId))
            .into(binding.ivTutorImage)

        fetchTutoriesDetails(tutoriesId)

        binding.btnBack.setOnClickListener {
            finish()
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
                        }

                        LessonType.OFFLINE -> {
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
                    binding.rvAlsoTeach.adapter = AlsoTeachAdapter(detailedTutor.alsoTeaches)

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

    override fun onDestroy() {
        coroutineScope.cancel() // Cancel coroutines when activity is destroyed
        super.onDestroy()
    }

}