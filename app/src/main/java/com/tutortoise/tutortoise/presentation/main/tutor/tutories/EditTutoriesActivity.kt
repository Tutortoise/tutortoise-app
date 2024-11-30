package com.tutortoise.tutortoise.presentation.main.tutor.tutories.editTutories

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.tutortoise.tutortoise.data.model.DetailedTutoriesResponse
import com.tutortoise.tutortoise.data.model.EditTutoriesRequest
import com.tutortoise.tutortoise.data.pref.ApiException
import com.tutortoise.tutortoise.data.repository.TutoriesRepository
import com.tutortoise.tutortoise.databinding.ActivityEditTutoriesBinding
import com.tutortoise.tutortoise.presentation.main.tutor.tutories.createTutories.CurrencyTextWatcher
import com.tutortoise.tutortoise.presentation.main.tutor.tutories.createTutories.RateInfo
import com.tutortoise.tutortoise.presentation.main.tutor.tutories.createTutories.parseFormattedNumber
import kotlinx.coroutines.launch

class EditTutoriesActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditTutoriesBinding
    private lateinit var tutoriesRepository: TutoriesRepository
    private var tutoriesId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            binding = ActivityEditTutoriesBinding.inflate(layoutInflater)
            setContentView(binding.root)

            tutoriesId = intent.getStringExtra(EXTRA_TUTORIES_ID)
            Log.d("EditTutoriesActivity", "Received tutoriesId: $tutoriesId")

            if (tutoriesId == null) {
                Log.e("EditTutoriesActivity", "No tutoriesId provided")
                Toast.makeText(this, "Error: No tutories ID provided", Toast.LENGTH_SHORT).show()
                finish()
                return
            }

            tutoriesRepository = TutoriesRepository(this)
            setupViews()
            setupListeners()
            fetchTutoriesData()

        } catch (e: Exception) {
            Log.e("EditTutoriesActivity", "Error in onCreate", e)
            Toast.makeText(this, "Error initializing: ${e.message}", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun setupViews() {
        setupRateEditText()
        initializeButtons()
    }

    private fun setupRateEditText() {
        binding.editRate.addTextChangedListener(CurrencyTextWatcher(binding.editRate))
    }

    private fun initializeButtons() {
        binding.apply {
            btnOnline.isSelected = false
            btnOnsite.isSelected = false
        }
    }

    private fun setupListeners() {
        with(binding) {
            btnBack.setOnClickListener { finish() }
            btnOnline.setOnClickListener { it.isSelected = !it.isSelected }
            btnOnsite.setOnClickListener { it.isSelected = !it.isSelected }
            btnConfirm.setOnClickListener { updateTutories() }
        }
    }

    private fun fetchTutoriesData() {
        lifecycleScope.launch {
            try {
                Log.d("EditTutoriesActivity", "Fetching tutories data for ID: $tutoriesId")
                val result = tutoriesRepository.getTutoriesById(tutoriesId!!)
                result.fold(
                    onSuccess = { tutories ->
                        Log.d("EditTutoriesActivity", "Successfully fetched tutories data")
                        populateData(tutories)
                    },
                    onFailure = { throwable ->
                        Log.e("EditTutoriesActivity", "Failed to load tutories data", throwable)
                        showError("Failed to load tutories data: ${throwable.message}")
                        finish()
                    }
                )
            } catch (e: Exception) {
                Log.e("EditTutoriesActivity", "Error fetching tutories data", e)
                showError("An error occurred: ${e.message}")
                finish()
            }
        }
    }

    private fun populateData(detailedTutories: DetailedTutoriesResponse) {
        with(binding) {
            val tutories = detailedTutories.tutories
            val subjects = detailedTutories.subjects

            textSubject.text = subjects.name
            editAbout.setText(tutories.aboutYou)
            editMethodology.setText(tutories.teachingMethodology)
            editRate.setText(tutories.hourlyRate.toString())

            // Set type lesson buttons
            when (tutories.typeLesson) {
                "online" -> btnOnline.isSelected = true
                "offline" -> btnOnsite.isSelected = true
                "both" -> {
                    btnOnline.isSelected = true
                    btnOnsite.isSelected = true
                }
            }

            // Update rate info
            val rateInfo = RateInfo(
                averageRate = 50000, // TODO: Fetch from API
                location = detailedTutories.tutors.city ?: "Samarinda",
                subject = subjects.name
            )
            textRateInfo.text = rateInfo.formatMessage(this@EditTutoriesActivity)
        }
    }

    private fun getTypeLesson(): String {
        binding.apply {
            return if (btnOnline.isSelected && btnOnsite.isSelected) {
                "both"
            } else if (btnOnline.isSelected) {
                "online"
            } else if (btnOnsite.isSelected) {
                "offline"
            } else {
                ""
            }
        }
    }

    private fun updateTutories() {
        val hourlyRate = binding.editRate.text.toString().parseFormattedNumber()

        val request = EditTutoriesRequest(
            aboutYou = binding.editAbout.text.toString(),
            teachingMethodology = binding.editMethodology.text.toString(),
            hourlyRate = hourlyRate.toInt(),
            typeLesson = getTypeLesson()
        )

        lifecycleScope.launch {
            try {
                val result = tutoriesRepository.updateTutories(tutoriesId!!, request)
                result.fold(
                    onSuccess = {
                        Toast.makeText(
                            this@EditTutoriesActivity,
                            "Tutories updated successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    },
                    onFailure = { throwable ->
                        if (throwable is ApiException) {
                            handleValidationErrors(throwable)
                        } else {
                            showError("Failed to update tutories: ${throwable.message}")
                        }
                    }
                )
            } catch (e: Exception) {
                showError("An error occurred: ${e.message}")
            }
        }
    }

    private fun handleValidationErrors(throwable: ApiException) {
        binding.editAbout.error = null
        binding.editMethodology.error = null
        binding.editRate.error = null

        throwable.errorResponse?.errors?.forEach { error ->
            when (error.field) {
                "body.aboutYou" -> binding.editAbout.error = error.message
                "body.teachingMethodology" -> binding.editMethodology.error = error.message
                "body.hourlyRate" -> binding.editRate.error = error.message
                "body.typeLesson" -> showError("Please select at least one type lesson")
            }
        }
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        const val EXTRA_TUTORIES_ID = "extra_tutories_id"
    }
}