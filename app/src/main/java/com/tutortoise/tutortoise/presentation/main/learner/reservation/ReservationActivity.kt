package com.tutortoise.tutortoise.presentation.main.learner.reservation

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.flexbox.FlexboxLayoutManager
import com.tutortoise.tutortoise.data.repository.TutoriesRepository
import com.tutortoise.tutortoise.databinding.ActivityReservationBinding
import com.tutortoise.tutortoise.presentation.main.learner.reservation.adapter.DateAdapter
import com.tutortoise.tutortoise.presentation.main.learner.reservation.adapter.TimeAdapter
import com.tutortoise.tutortoise.utils.groupAvailabilityByDate
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class ReservationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityReservationBinding
    private val viewModel: ReservationViewModel by viewModels {
        ReservationViewModel.provideFactory(
            TutoriesRepository(this)
        )
    }

    private var tutoriesId: String = ""
    private var tutorId: String = ""
    private var tutorName: String = ""
    private var hourlyRate: Int = 0

    private var selectedDatetime = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReservationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tutoriesId = intent.getStringExtra("TUTORIES_ID") ?: ""
        tutorId = intent.getStringExtra("TUTOR_ID") ?: ""
        tutorName = intent.getStringExtra("TUTOR_NAME") ?: ""
        hourlyRate = intent.getIntExtra("HOURLY_RATE", 0)

        setupUI()

        observeViewModel()
        viewModel.fetchAvailability(tutoriesId)
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.availability.collect { availability ->
                if (availability != null) {
                    updateAvailabilityUI(availability)
                }
            }
        }
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

    private fun updateAvailabilityUI(availability: List<String>) {
        val groupedAvailability = groupAvailabilityByDate(availability)
        val dateList = groupedAvailability.keys.toList()

        // Only render four dates
        val firstFourDates = dateList.take(4)
        val dateAdapter = DateAdapter(firstFourDates) {
            updateTimesForSelectedDate(it, groupedAvailability)
        }

        binding.rvSelectDate.apply {
            layoutManager =
                LinearLayoutManager(this@ReservationActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = dateAdapter
        }
        
        if (firstFourDates.isNotEmpty()) {
            val defaultPosition = 0
            dateAdapter.setDefaultSelectedDate(defaultPosition)
            updateTimesForSelectedDate(firstFourDates[defaultPosition], groupedAvailability)
        }
    }

    @SuppressLint("NewApi")
    private fun updateTimesForSelectedDate(
        date: String,
        groupedAvailability: Map<String, List<String>>
    ) {
        val times = groupedAvailability[date] ?: emptyList()
        val timeAdapter = TimeAdapter(times, onTimeSelected = { time ->
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

            val localDateTime = LocalDateTime.parse("$date $time", formatter)
            val localZone = ZoneId.systemDefault()
            val localZonedDateTime = ZonedDateTime.of(localDateTime, localZone)

            val utcZonedDateTime = localZonedDateTime.withZoneSameInstant(ZoneId.of("UTC"))

            val isoFormat = DateTimeFormatter.ISO_INSTANT
            val isoString = isoFormat.format(utcZonedDateTime.toInstant())
            Log.d("ReservationActivity", "Selected $isoString")
            selectedDatetime = isoString
        })
        binding.rvSelectTime.apply {
            layoutManager = FlexboxLayoutManager(this@ReservationActivity)
            adapter = timeAdapter
        }
    }

}