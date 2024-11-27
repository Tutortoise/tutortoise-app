package com.tutortoise.tutortoise.presentation.main.tutor.tutories

import android.content.res.ColorStateList
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.tutortoise.tutortoise.R
import com.tutortoise.tutortoise.databinding.ActivityCreateServiceBinding

class CreateServiceActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCreateServiceBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateServiceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViews()
        setupListeners()
    }


    data class ServiceModel(
        val subject: String,
        val about: String,
        val methodology: String,
        val ratePerHour: Int,
        val isOnline: Boolean,
        val isFaceToFace: Boolean
    )

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
                createService()
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

    private fun createService() {
        // Create service object
        val service = ServiceModel(
            subject = binding.spinnerSubject.selectedItem.toString(),
            about = binding.editAbout.text.toString(),
            methodology = binding.editMethodology.text.toString(),
            ratePerHour = binding.editRate.text.toString().toInt(),
            isOnline = binding.btnOnline.isSelected,
            isFaceToFace = binding.btnFaceToFace.isSelected
        )

        // TODO: Save service to backend/database

        // Show success message and finish activity
        Toast.makeText(this, "Service created successfully", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}