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
import com.tutortoise.tutortoise.data.model.CategoryResponse
import com.tutortoise.tutortoise.data.model.CreateTutoriesRequest
import com.tutortoise.tutortoise.data.pref.ApiException
import com.tutortoise.tutortoise.data.repository.CategoryRepository
import com.tutortoise.tutortoise.data.repository.TutorRepository
import com.tutortoise.tutortoise.data.repository.TutoriesRepository
import com.tutortoise.tutortoise.databinding.ActivityCreateTutoriesBinding
import com.tutortoise.tutortoise.utils.parseFormattedNumber
import kotlinx.coroutines.launch

class CreateTutoriesActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCreateTutoriesBinding
    private lateinit var tutoriesRepository: TutoriesRepository
    private lateinit var categoryRepository: CategoryRepository
    private lateinit var tutorRepository: TutorRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateTutoriesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tutoriesRepository = TutoriesRepository(this)
        categoryRepository = CategoryRepository(this)
        tutorRepository = TutorRepository(this)
        setupViews()
        setupListeners()
    }

    private fun setupViews() {
        fetchAvailableCategories()
        setupRateEditText()
        initializeButtons()
    }

    private fun fetchAvailableCategories() {
        lifecycleScope.launch {
            val categoryResponse = categoryRepository.fetchAvailableCategories()
            val categories = categoryResponse?.data
            if (!categories.isNullOrEmpty()) {
                setCategorySpinner(categories)
            }
        }
    }

    private suspend fun getAverageRate(
        categoryId: String,
        city: String,
    ): Float? {
        try {
            val result = tutoriesRepository.fetchTutoriesAverageRate(
                categoryId = categoryId,
                city = city,
            )

            return result?.data
        } catch (e: Exception) {
            Log.e("CreateTutoriesActivity", "Error fetching average rate", e)
            return null
        }
    }

    private fun setCategorySpinner(categories: List<CategoryResponse>) {
        val adapter = ArrayAdapter(
            this,
            R.layout.simple_spinner_item,
            categories
        ).apply {
            setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        }

        binding.spinnerCategory.apply {
            this.adapter = adapter
            onItemSelectedListener = createCategorySelectionListener(categories)
        }
    }

    private fun createCategorySelectionListener(categories: List<CategoryResponse>) =
        object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                updateRateInfo(categories[position])
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

    private fun updateRateInfo(category: CategoryResponse) {
        lifecycleScope.launch {
            // Tutor is forced to fill in their city name before creating tutories, so this should never be null
            val city = tutorRepository.fetchTutorProfile()!!.data!!.city!!
            val rateInfo = RateInfo(
                averageRate = getAverageRate(category.id, city),
                location = city,
                category = category.name
            )

            binding.textRateInfo.text = rateInfo.formatMessage(this@CreateTutoriesActivity)
        }
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

    private fun createTutories() {
        val selectedCategory = binding.spinnerCategory.selectedItem as? CategoryResponse
        val categoryId = selectedCategory!!.id
        val typeLesson = getTypeLesson()

        val hourlyRate = binding.editRate.text.toString().parseFormattedNumber()

        val request = CreateTutoriesRequest(
            name = binding.editTutoriesName.text.toString(),
            categoryId = categoryId,
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
                    "body.categoryId" -> showError(error.message)
                    "body.typeLesson" -> showError("Please select at least one type lesson")
                }
            }
        }
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}