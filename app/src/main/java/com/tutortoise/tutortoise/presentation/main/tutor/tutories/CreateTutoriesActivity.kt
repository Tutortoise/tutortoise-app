package com.tutortoise.tutortoise.presentation.main.tutor.tutories

import android.content.res.ColorStateList
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.tutortoise.tutortoise.R
import com.tutortoise.tutortoise.data.model.CreateTutoriesRequest
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
        binding.btnFaceToFace.isSelected = false
    }

    private fun fetchAvailableSubjects() {
        lifecycleScope.launch {
            val subjectsResponse = subjectRepository.fetchAvailableSubjects()
            val subjects = subjectsResponse?.data?.map { it.name }
            if (!subjects.isNullOrEmpty()) {
                setSubjectSpinner(subjects)
            }
        }
    }

    private fun setSubjectSpinner(subjects: List<String>) {
        val adapter = ArrayAdapter(
            this@CreateTutoriesActivity,
            android.R.layout.simple_spinner_item,
            subjects
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

        binding.btnFaceToFace.setOnClickListener {
            binding.btnFaceToFace.isSelected = !binding.btnFaceToFace.isSelected
            updateButtonAppearance()
        }

        // Confirm button
        binding.btnConfirm.setOnClickListener {
            if (validateInputs()) {
                createTutories()
            }
        }
    }

    private fun updateButtonAppearance() {
        binding.btnOnline.backgroundTintList = ColorStateList.valueOf(
            if (binding.btnOnline.isSelected) getColor(R.color.darkgreen) else getColor(R.color.white)
        )
        binding.btnOnline.setTextColor(
            if (binding.btnOnline.isSelected) getColor(R.color.white) else getColor(R.color.darkgreen)
        )

        binding.btnFaceToFace.backgroundTintList = ColorStateList.valueOf(
            if (binding.btnFaceToFace.isSelected) getColor(R.color.darkgreen) else getColor(R.color.white)
        )
        binding.btnFaceToFace.setTextColor(
            if (binding.btnFaceToFace.isSelected) getColor(R.color.white) else getColor(R.color.darkgreen)
        )
    }

    private fun validateInputs(): Boolean {
        val subject = binding.spinnerSubject.selectedItem?.toString()
        val about = binding.editAbout.text.toString()
        val methodology = binding.editMethodology.text.toString()
        val rate = binding.editRate.text.toString()

        if (subject.isNullOrEmpty()) {
            showError("Please select a subject")
            return false
        }
        if (about.isEmpty()) {
            showError("Please provide information about yourself")
            return false
        }
        if (methodology.isEmpty()) {
            showError("Please describe your teaching methodology")
            return false
        }
        if (rate.isEmpty()) {
            showError("Please set your rate per hour")
            return false
        }
        if (!binding.btnOnline.isSelected && !binding.btnFaceToFace.isSelected) {
            showError("Please select at least one teaching mode")
            return false
        }

        return true
    }

    //    TODO: Fix Post Service
    private fun createTutories() {
        val request = CreateTutoriesRequest(
            subject = binding.spinnerSubject.selectedItem.toString(),
            about = binding.editAbout.text.toString(),
            methodology = binding.editMethodology.text.toString(),
            ratePerHour = binding.editRate.text.toString().toInt(),
            isOnline = binding.btnOnline.isSelected,
            isFaceToFace = binding.btnFaceToFace.isSelected
        )

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
                    onFailure = { error ->
                        showError(error.message ?: "Failed to create tutories")
                    }
                )
            } catch (e: Exception) {
                showError("An error occurred: ${e.message}")
            }
        }
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}