package com.tutortoise.tutortoise.presentation.main.tutor.tutories

import android.content.res.ColorStateList
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.tutortoise.tutortoise.R
import com.tutortoise.tutortoise.data.pref.ApiConfig
import com.tutortoise.tutortoise.data.pref.TutoriesServiceModel
import com.tutortoise.tutortoise.databinding.ActivityCreateTutoriesBinding
import kotlinx.coroutines.launch

class CreateTutoriesActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCreateTutoriesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateTutoriesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViews()
        setupListeners()
    }

    private fun setupViews() {
        // TODO: Set up spinner for subject selection
        val subjects = arrayOf("Mathematics", "Physics", "Chemistry", "Biology", "English")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, subjects)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerSubject.adapter = adapter

        // Set default state for teaching mode buttons
        binding.btnOnline.isSelected = false
        binding.btnFaceToFace.isSelected = false
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
        // Create tutories object
        val tutories = TutoriesServiceModel(
            id = "",
            subject = binding.spinnerSubject.selectedItem.toString(),
            about = binding.editAbout.text.toString(),
            methodology = binding.editMethodology.text.toString(),
            ratePerHour = binding.editRate.text.toString().toInt(),
            isOnline = binding.btnOnline.isSelected,
            isFaceToFace = binding.btnFaceToFace.isSelected
        )

        lifecycleScope.launch {
            try {
                // Get the ApiService instance
                val apiService = ApiConfig.getApiService(this@CreateTutoriesActivity)

                // Call the API to create the tutor class
                val response = apiService.createTutories(tutories)

                if (response.isSuccessful) {
                    // Handle the successful response
                    Toast.makeText(this@CreateTutoriesActivity, "Tutories created successfully", Toast.LENGTH_SHORT).show()
                    finish() // Close the activity or navigate as needed
                } else {
                    // Handle API error
                    val errorMessage = response.body()?.message ?: "Unknown error"
                    showError(errorMessage)
                }
            } catch (e: Exception) {
                // Handle network errors or exceptions
                showError("An error occurred: ${e.message}")
            }
        }
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}