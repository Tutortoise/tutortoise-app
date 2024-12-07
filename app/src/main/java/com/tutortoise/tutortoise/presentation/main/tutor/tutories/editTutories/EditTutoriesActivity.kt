package com.tutortoise.tutortoise.presentation.main.tutor.tutories.editTutories

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.tutortoise.tutortoise.R
import com.tutortoise.tutortoise.data.model.DetailedTutoriesResponse
import com.tutortoise.tutortoise.data.model.EditTutoriesRequest
import com.tutortoise.tutortoise.data.pref.ApiException
import com.tutortoise.tutortoise.data.repository.TutoriesRepository
import com.tutortoise.tutortoise.databinding.ActivityEditTutoriesBinding
import com.tutortoise.tutortoise.presentation.main.tutor.tutories.createTutories.CurrencyTextWatcher
import com.tutortoise.tutortoise.presentation.main.tutor.tutories.createTutories.RateInfo
import com.tutortoise.tutortoise.utils.parseFormattedNumber
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

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
            btnTutoriesStatus.setOnClickListener {
                tvTutoriesStatus.setText(if (btnTutoriesStatus.isChecked) R.string.status_enabled else R.string.status_disabled)
            }
            tvDelete.setOnClickListener {
                showDeleteConfirmation()
            }

        }
    }

    private suspend fun getAverageRate(categoryId: String, city: String): Float? {
        try {
            val result = tutoriesRepository.fetchTutoriesAverageRate(
                categoryId = categoryId,
                city = city,
            )

            return result?.data
        } catch (e: Exception) {
            Log.e("EditTutoriesActivity", "Error fetching average rate", e)
            return null
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

    private fun populateData(tutories: DetailedTutoriesResponse) {
        with(binding) {
            editTutoriesName.setText(tutories.name)
            tvCategoryName.text = tutories.categoryName
            editAbout.setText(tutories.aboutYou)
            editMethodology.setText(tutories.teachingMethodology)
            editRate.setText(String.format(Locale.getDefault(), "%d", tutories.hourlyRate))

            // Set type lesson buttons
            when (tutories.typeLesson) {
                "online" -> btnOnline.isSelected = true
                "offline" -> btnOnsite.isSelected = true
                "both" -> {
                    btnOnline.isSelected = true
                    btnOnsite.isSelected = true
                }
            }

            btnTutoriesStatus.isChecked = tutories.isEnabled
            tvTutoriesStatus.setText(if (btnTutoriesStatus.isChecked) R.string.status_enabled else R.string.status_disabled)

            // Update rate info
            lifecycleScope.launch {
                val rateInfo = RateInfo(
                    averageRate = getAverageRate(
                        tutories.categoryId,
                        tutories.city,
                    ),
                    location = tutories.city,
                    category = tutories.categoryName
                )
                textRateInfo.text = rateInfo.formatMessage(this@EditTutoriesActivity)
            }
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
            typeLesson = getTypeLesson(),
            isEnabled = binding.btnTutoriesStatus.isChecked
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

    private fun deleteTutories() {
        lifecycleScope.launch {
            try {
                val result = tutoriesRepository.deleteTutories(tutoriesId!!)
                result.fold(
                    onSuccess = {
                        Toast.makeText(
                            this@EditTutoriesActivity,
                            "Tutories deleted successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    },
                    onFailure = {
                        showError("Failed to delete tutories")
                    }
                )
            } catch (e: Exception) {
                showError("An error occurred: ${e.message}")
            }
        }
    }

    private fun handleValidationErrors(throwable: Throwable) {
        if (throwable is ApiException) {
            binding.editAbout.error = null
            binding.editMethodology.error = null
            binding.editRate.error = null

            if (throwable.message.contains("About you")) {
                binding.editAbout.error = throwable.message
            } else if (throwable.message.contains("Teaching methodology")) {
                binding.editMethodology.error = throwable.message
            }

            throwable.errorResponse?.errors?.forEach { error ->
                when (error.field) {
                    "body.name" -> binding.editTutoriesName.error = error.message
                    "body.aboutYou" -> binding.editAbout.error = error.message
                    "body.teachingMethodology" -> binding.editMethodology.error = error.message
                    "body.hourlyRate" -> binding.editRate.error = error.message
                    "body.typeLesson" -> showError("Please select at least one type lesson")
                }
            }

        }
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun showDeleteConfirmation() {
        val dialogView = LayoutInflater.from(this@EditTutoriesActivity)
            .inflate(R.layout.fragment_dialog_delete_confirmation, null)

        val dialog = MaterialAlertDialogBuilder(this@EditTutoriesActivity)
            .setView(dialogView)
            .create()

        // Make dialog rounded corners
        dialog.window?.setBackgroundDrawableResource(R.drawable.rounded_dialog_background)

        // Set up click listeners for the buttons
        dialogView.findViewById<MaterialButton>(R.id.btnCancel).setOnClickListener {
            dialog.dismiss()
        }

        dialogView.findViewById<MaterialButton>(R.id.btnDelete).setOnClickListener {
            // Show loading state
            val loadingView = dialogView.findViewById<View>(R.id.loadingView)
            val buttonsLayout = dialogView.findViewById<View>(R.id.buttonsLayout)

            loadingView.visibility = View.VISIBLE
            buttonsLayout.visibility = View.GONE

            lifecycleScope.launch {
                try {
                    deleteTutories()
                    dialog.dismiss()
                    finish()
                } catch (e: Exception) {
                    // Handle error
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@EditTutoriesActivity,
                            "Failed to delete tutories. Please try again.",
                            Toast.LENGTH_SHORT
                        ).show()
                        loadingView.visibility = View.GONE
                        buttonsLayout.visibility = View.VISIBLE
                    }
                }
            }
        }

        dialog.show()
    }

    companion object {
        const val EXTRA_TUTORIES_ID = "extra_tutories_id"
    }
}