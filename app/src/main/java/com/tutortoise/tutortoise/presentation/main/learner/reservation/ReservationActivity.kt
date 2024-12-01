package com.tutortoise.tutortoise.presentation.main.learner.reservation

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.tutortoise.tutortoise.databinding.ActivityReservationBinding

class ReservationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityReservationBinding

    private var tutorId: String = ""
    private var tutorName: String = ""
    private var hourlyRate: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReservationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get data from intent
        tutorId = intent.getStringExtra("TUTOR_ID") ?: ""
        tutorName = intent.getStringExtra("TUTOR_NAME") ?: ""
        hourlyRate = intent.getIntExtra("HOURLY_RATE", 0)

        setupUI()
    }

    private fun setupUI() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        // Handle online/onsite button selection
        binding.btnOnline.setOnClickListener {
            binding.btnOnline.isSelected = true
            binding.btnOnsite.isSelected = false
        }

        binding.btnOnsite.setOnClickListener {
            binding.btnOnline.isSelected = false
            binding.btnOnsite.isSelected = true
        }

        // Handle date selection
        val dateButtons = listOf(
            binding.btnDate1,
            binding.btnDate2,
            binding.btnDate3,
            binding.btnDate4
        )

        dateButtons.forEach { button ->
            button.setOnClickListener {
                dateButtons.forEach { it.isSelected = false }
                button.isSelected = true
            }
        }

        // Handle tutoring time selection
        val timeButtons = listOf(
            binding.btnTutoringTime1,
            binding.btnTutoringTime2,
            binding.btnTutoringTime3,
            binding.btnTutoringTime4,
            binding.btnTutoringTime5
        )

        timeButtons.forEach { button ->
            button.setOnClickListener {
                timeButtons.forEach { it.isSelected = false }
                button.isSelected = true
            }
        }

        // Handle save button
        binding.btnSave.setOnClickListener {
            // TODO: Handle reservation submission
        }
    }
}