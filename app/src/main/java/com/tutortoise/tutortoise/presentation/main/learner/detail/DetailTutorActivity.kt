package com.tutortoise.tutortoise.presentation.main.learner.detail

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.tutortoise.tutortoise.databinding.ActivityDetailTutorBinding
import com.tutortoise.tutortoise.utils.Constants

class DetailTutorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailTutorBinding

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
        }

        binding.btnBack.setOnClickListener {
            finish()
        }
    }

}