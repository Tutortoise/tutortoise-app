package com.tutortoise.tutortoise.presentation.main.tutor.tutories

import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.lifecycle.lifecycleScope
import com.tutortoise.tutortoise.R
import com.tutortoise.tutortoise.data.model.CreateTutoriesRequest
import com.tutortoise.tutortoise.data.model.SubjectResponse
import com.tutortoise.tutortoise.data.pref.ApiException
import com.tutortoise.tutortoise.data.repository.SubjectRepository
import com.tutortoise.tutortoise.data.repository.TutoriesRepository
import com.tutortoise.tutortoise.databinding.ActivityCreateTutoriesBinding
import kotlinx.coroutines.launch

class CreateTutoriesActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCreateTutoriesBinding
    private lateinit var tutoriesRepository: TutoriesRepository
    private lateinit var subjectRepository: SubjectRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateTutoriesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tutoriesRepository = TutoriesRepository(this)
        subjectRepository = SubjectRepository(this)
        setupViews()
        setupListeners()
    }

    private fun setupViews() {
        // Fetch available subjects (for spinner)
        fetchAvailableSubjects()

        // Set default state for teaching mode buttons
        binding.btnOnline.isSelected = false
        binding.btnOnsite.isSelected = false
    }

    private fun fetchAvailableSubjects() {
        lifecycleScope.launch {
            val subjectsResponse = subjectRepository.fetchAvailableSubjects()
            val subjects = subjectsResponse?.data
            if (!subjects.isNullOrEmpty()) {
                setSubjectSpinner(subjects)
            }
        }
    }

    private fun setSubjectSpinner(subjects: List<SubjectResponse>) {
        val adapter = ArrayAdapter(
            this@CreateTutoriesActivity,
            android.R.layout.simple_spinner_item,
            subjects,
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerSubject.adapter = adapter
    }

    private fun setupListeners() {
        // Back button
        binding.btnBack.setOnClickListener {
            finish()
        }

        // Teaching mode selection
        binding.btnOnline.setOnClickListener {
            binding.btnOnline.isSelected = !binding.btnOnline.isSelected
            updateButtonAppearance()
        }

        binding.btnOnsite.setOnClickListener {
            binding.btnOnsite.isSelected = !binding.btnOnsite.isSelected
            updateButtonAppearance()
        }

        // Confirm button
        binding.btnConfirm.setOnClickListener {
            createTutories()
        }
    }

    private fun updateButtonAppearance() {
        updateButtonStyle(binding.btnOnline, binding.btnOnline.isSelected)
        updateButtonStyle(binding.btnOnsite, binding.btnOnsite.isSelected)
    }

    private fun updateButtonStyle(button: Button, isSelected: Boolean) {
        if (isSelected) {
            button.background = AppCompatResources.getDrawable(this, R.drawable.bg_dark_green)
            button.setTextColor(getColor(R.color.white))
        } else {
            button.background = AppCompatResources.getDrawable(this, R.drawable.button_outline)
            button.setTextColor(getColor(R.color.darkgreen))
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

    // TODO: before submitting, go to availability page
    private fun createTutories() {
        val selectedSubject = binding.spinnerSubject.selectedItem as? SubjectResponse
        val subjectId = selectedSubject!!.id
        val typeLesson = getTypeLesson()


        val request = CreateTutoriesRequest(
            subjectId = subjectId,
            aboutYou = binding.editAbout.text.toString(),
            teachingMethodology = binding.editMethodology.text.toString(),
            hourlyRate = binding.editRate.text.toString().toIntOrNull()
                ?: return showError("Invalid rate"),
            typeLesson = typeLesson
        )

        Log.d("CreateTutoriesActivity", "Request: $request")

        lifecycleScope.launch {
            try {
                val result = tutoriesRepository.createTutories(request)
                result.fold(
                    onSuccess = {
                        Toast.makeText(
                            this@CreateTutoriesActivity,
                            "Tutories created successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    },
                    onFailure = { throwable ->
                        handleValidationErrors(throwable)
                    }
                )
            } catch (e: Exception) {
                showError("An error occurred: ${e.message}")
            }
        }
    }

    private fun handleValidationErrors(throwable: Throwable) {
        if (throwable is ApiException) {
            // Clear previous errors before showing new ones
            binding.editAbout.error = null
            binding.editMethodology.error = null
            binding.editRate.error = null

            throwable.errorResponse?.errors?.forEach { error ->
                when (error.field) {
                    "body.aboutYou" -> binding.editAbout.error = error.message
                    "body.teachingMethodology" -> binding.editMethodology.error = error.message
                    "body.hourlyRate" -> binding.editRate.error = error.message
                    "body.subjectId" -> showError(error.message)
                    "body.typeLesson" -> showError("Please select at least one type lesson")
                }
            }
        }
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}