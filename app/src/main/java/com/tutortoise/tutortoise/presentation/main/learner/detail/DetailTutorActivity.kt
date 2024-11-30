package com.tutortoise.tutortoise.presentation.main.learner.detail

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.tutortoise.tutortoise.data.model.LessonType
import com.tutortoise.tutortoise.data.pref.ApiConfig
import com.tutortoise.tutortoise.data.pref.ApiService
import com.tutortoise.tutortoise.databinding.ActivityDetailTutorBinding
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

        val tutorName = intent.getStringExtra("TUTOR_NAME") ?: "Unknown Tutor"
        val subjectName = intent.getStringExtra("SUBJECT_NAME") ?: "Unknown Subject"
        val rating = intent.getFloatExtra("RATING", 0f)
        val hourlyRate = intent.getIntExtra("HOURLY_RATE", 0)
        val city = intent.getStringExtra("CITY") ?: "Unknown City"
        val tutorId = intent.getStringExtra("TUTOR_ID")

        // Use the data to populate your UI
        binding.tvTutorName.text = tutorName
        binding.tvTutorSubject.text = subjectName
        binding.tvRating.text = rating.toString()
        binding.tvHourlyRate.text = "Rp. $hourlyRate / Hour"
        binding.tvCity.text = city

        if (tutorId != null) {
            Glide.with(this)
                .load(Constants.getProfilePictureUrl(tutorId))
                .into(binding.ivTutorImage)

            fetchTutorDetails(tutorId)
        }

        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun fetchTutorDetails(tutorId: String) {
        coroutineScope.launch {
            try {
                // Assuming you have a method to get tutories by ID in your repository
                val response = withContext(Dispatchers.IO) {
                    apiService.getTutoriesById(tutorId)
                }

                if (response.isSuccessful && response.body()?.data != null) {
                    val detailedTutor = response.body()?.data!!

                    // Update UI with additional details from DetailedTutoriesResponse
                    binding.tvAboutText.text = detailedTutor.tutories.aboutYou
                    binding.tvTeachingMethodologyText.text = detailedTutor.tutories.teachingMethodology

                    // Set lesson type
                    binding.tvLessonType.text = when(detailedTutor.tutories.typeLesson) {
                        LessonType.ONLINE -> "Online Lessons"
                        LessonType.OFFLINE -> "Offline Lessons"
                        LessonType.BOTH -> "Online and Offline Lessons"
                        else -> "Lesson Type Not Specified"
                    }

                    // Manage visibility of lesson type views
                    when(detailedTutor.tutories.typeLesson) {
                        LessonType.ONLINE -> {
                            binding.tvOnlineStatus.visibility = View.VISIBLE
                            binding.tvOnsiteStatus.visibility = View.GONE
                        }
                        LessonType.OFFLINE -> {
                            binding.tvOnlineStatus.visibility = View.GONE
                            binding.tvOnsiteStatus.visibility = View.VISIBLE
                        }
                        LessonType.BOTH -> {
                            binding.tvOnlineStatus.visibility = View.VISIBLE
                            binding.tvOnsiteStatus.visibility = View.VISIBLE
                        }
                        else -> {
                            binding.tvOnlineStatus.visibility = View.GONE
                            binding.tvOnsiteStatus.visibility = View.GONE
                        }
                    }

                    // Handle also teaches section
                    if (detailedTutor.alsoTeaches.isNotEmpty()) {
                        val alsoTeachesText = detailedTutor.alsoTeaches.joinToString("\n") {
                            "${it.subjectName} - Rp. ${it.hourlyRate}/Hour (${it.typeLesson})"
                        }
                        binding.tvAlsoTeach.text = alsoTeachesText
                        binding.tvAlsoTeach.visibility = View.VISIBLE

                        // Show or hide additional subject layouts based on the number of subjects
                        when (detailedTutor.alsoTeaches.size) {
                            0 -> {
                                binding.alsoTeach1.visibility = View.GONE
                                binding.alsoTeach2.visibility = View.GONE
                            }
                            1 -> {
                                binding.alsoTeach1.visibility = View.VISIBLE
                                binding.alsoTeach2.visibility = View.GONE

                                // Update the first layout with the single subject
                                val subject = detailedTutor.alsoTeaches[0]
                                (binding.alsoTeach1.getChildAt(0) as TextView).text = subject.subjectName
                            }
                            2 -> {
                                binding.alsoTeach1.visibility = View.VISIBLE
                                binding.alsoTeach2.visibility = View.VISIBLE

                                // Update the first layout
                                val subject1 = detailedTutor.alsoTeaches[0]
                                (binding.alsoTeach1.getChildAt(0) as TextView).text = subject1.subjectName

                                // Update the second layout
                                val subject2 = detailedTutor.alsoTeaches[1]
                                (binding.alsoTeach2.getChildAt(0) as TextView).text = subject2.subjectName
                            }
                            else -> {
                                binding.alsoTeach1.visibility = View.VISIBLE
                                binding.alsoTeach2.visibility = View.VISIBLE

                                // Update the first layout
                                val subject1 = detailedTutor.alsoTeaches[0]
                                (binding.alsoTeach1.getChildAt(0) as TextView).text = subject1.subjectName

                                // Update the second layout
                                val subject2 = detailedTutor.alsoTeaches[1]
                                (binding.alsoTeach2.getChildAt(0) as TextView).text = subject2.subjectName
                            }
                        }
                    } else {
                        binding.tvAlsoTeach.visibility = View.GONE
                        binding.alsoTeach1.visibility = View.GONE
                        binding.alsoTeach2.visibility = View.GONE
                    }

                } else {
                    // Handle error case
                    Toast.makeText(this@DetailTutorActivity, "Failed to load tutor details", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                // Handle network or other errors
                Toast.makeText(this@DetailTutorActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("DetailTutorActivity", "Error fetching tutor details", e)
            }
        }
    }

    override fun onDestroy() {
        coroutineScope.cancel() // Cancel coroutines when activity is destroyed
        super.onDestroy()
    }

}