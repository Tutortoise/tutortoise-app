package com.tutortoise.tutortoise.presentation.main.tutor.tutories.createTutories

import android.R
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
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
        fetchAvailableSubjects()
        setupRateEditText()
        initializeButtons()
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
            this,
            R.layout.simple_spinner_item,
            subjects
        ).apply {
            setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        }

        binding.spinnerSubject.apply {
            this.adapter = adapter
            onItemSelectedListener = createSubjectSelectionListener(subjects)
        }
    }

    private fun createSubjectSelectionListener(subjects: List<SubjectResponse>) =
        object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                updateRateInfo(subjects[position])
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

    private fun updateRateInfo(subject: SubjectResponse) {
        // TODO: Fetch average rate, location from API
        val rateInfo = RateInfo(
            averageRate = 50000,
            location = "Samarinda",
            subject = subject.name
        )
        binding.textRateInfo.text = rateInfo.formatMessage(this)
    }

    private fun setupListeners() {
        with(binding) {
            btnBack.setOnClickListener { finish() }
            btnOnline.setOnClickListener { it.isSelected = !it.isSelected }
            btnOnsite.setOnClickListener { it.isSelected = !it.isSelected }
            btnConfirm.setOnClickListener { createTutories() }
        }
    }


    private fun initializeButtons() {
        binding.apply {
            btnOnline.isSelected = false
            btnOnsite.isSelected = false
        }
    }

    private fun setupRateEditText() {
        binding.editRate.addTextChangedListener(CurrencyTextWatcher(binding.editRate))
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

        val hourlyRate = binding.editRate.text.toString().parseFormattedNumber()

        val request = CreateTutoriesRequest(
            subjectId = subjectId,
            aboutYou = binding.editAbout.text.toString(),
            teachingMethodology = binding.editMethodology.text.toString(),
            hourlyRate = hourlyRate.toInt(),
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