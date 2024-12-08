package com.tutortoise.tutortoise.presentation.main.tutor.tutories

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.tutortoise.tutortoise.R
import com.tutortoise.tutortoise.data.model.UpdateTutorProfileRequest
import com.tutortoise.tutortoise.data.repository.TutorRepository
import com.tutortoise.tutortoise.databinding.ActivitySetScheduleBinding
import com.tutortoise.tutortoise.presentation.main.tutor.tutories.dialog.TimePickerDialog
import com.tutortoise.tutortoise.utils.generateTimeSlots
import com.tutortoise.tutortoise.utils.utcAvailabilityToLocal
import kotlinx.coroutines.launch

class SetScheduleActivity : AppCompatActivity() {
    private var selectedFromTime: String? = null
    private var selectedToTime: String? = null
    private val selectedDays = mutableSetOf<String>()
    private val dayButtons = mutableMapOf<MaterialButton, String>()

    private lateinit var binding: ActivitySetScheduleBinding
    private lateinit var tutorRepository: TutorRepository

    private val daysOfWeek = mapOf(
        "Sun" to 0,
        "Mon" to 1,
        "Tue" to 2,
        "Wed" to 3,
        "Thu" to 4,
        "Fri" to 5,
        "Sat" to 6
    )
    private val daysOfWeekReverse = daysOfWeek.entries.associateBy({ it.value }) { it.key }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySetScheduleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tutorRepository = TutorRepository(this)

        populateSchedule()
        initializeDayButtons()
        setupTimeButtons()
        setupConfirmButton()
        setupBackButton()
    }

    private fun populateSchedule() {
        lifecycleScope.launch {
            val profile = tutorRepository.fetchTutorProfile()
            profile?.data?.availability?.let { availability ->
                val schedule = utcAvailabilityToLocal(availability)

                schedule.forEach { (dayIdx, timeRange) ->
                    val day = daysOfWeekReverse[dayIdx]!!
                    selectedDays.add(day)
                    dayButtons.forEach { (button, dayName) ->
                        if (dayName == day) {
                            button.isSelected = true
                        }
                    }
                    selectedFromTime = timeRange.first()
                    selectedToTime = timeRange.last()
                    binding.btnSelectFromTime.text = getString(R.string.from_time, selectedFromTime)
                    binding.btnSelectToTime.text = getString(R.string.to_time, selectedToTime)
                }


            }
        }
    }

    private fun setupTimeButtons() {
        binding.btnSelectFromTime.setOnClickListener {
            binding.fromTimeLayout.error = null // Clear previous error
            TimePickerDialog(
                this,
                true,
                selectedFromTime,
                onTimeSelected = { time ->
                    if (selectedToTime != null && !isValidTimeRange(time, selectedToTime!!)) {
                        binding.fromTimeLayout.error = "Start time must be before end time"
                        return@TimePickerDialog
                    }
                    selectedFromTime = time
                    binding.btnSelectFromTime.text = getString(R.string.from_time, time)
                    updateConfirmButtonState()
                },
                onError = { error ->
                    binding.fromTimeLayout.error = error
                }
            ).show()
        }

        binding.btnSelectToTime.setOnClickListener {
            binding.toTimeLayout.error = null // Clear previous error
            TimePickerDialog(
                this,
                false,
                selectedToTime,
                onTimeSelected = { time ->
                    if (selectedFromTime != null && !isValidTimeRange(selectedFromTime!!, time)) {
                        binding.toTimeLayout.error = "End time must be after start time"
                        return@TimePickerDialog
                    }
                    selectedToTime = time
                    binding.btnSelectToTime.text = getString(R.string.to_time, time)
                    updateConfirmButtonState()
                },
                onError = { error ->
                    binding.toTimeLayout.error = error
                }
            ).show()
        }
    }

    private fun setupBackButton() {
        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun updateConfirmButtonState() {
        binding.btnConfirm.apply {
            isEnabled = selectedDays.isNotEmpty() &&
                    selectedFromTime != null &&
                    selectedToTime != null

            alpha = if (isEnabled) 1.0f else 0.5f
        }
    }

    private fun setupConfirmButton() {
        binding.btnConfirm.apply {
            isEnabled = false // Initially disabled
            alpha = 0.5f // Initially faded
            setOnClickListener {
                if (validateSchedule()) {
                    val scheduleInfo = buildScheduleInfo()
                    lifecycleScope.launch {
                        tutorRepository.updateTutorProfile(
                            UpdateTutorProfileRequest(
                                availability = scheduleInfo
                            )
                        )
                    }
                    finish()
                }
            }
        }
    }

    private fun validateSchedule(): Boolean {
        var isValid = true

        if (selectedDays.isEmpty()) {
            Toast.makeText(this, "Please select at least one day", Toast.LENGTH_SHORT).show()
            isValid = false
        }

        if (selectedFromTime == null) {
            binding.fromTimeLayout.error = "Please select start time"
            isValid = false
        }

        if (selectedToTime == null) {
            binding.toTimeLayout.error = "Please select end time"
            isValid = false
        }

        return isValid
    }

    @SuppressLint("NewApi")
    private fun buildScheduleInfo(): Map<Int, List<String>> {

        val availability = generateTimeSlots(
            selectedDays.map { daysOfWeek[it]!! },
            selectedFromTime!!,
            selectedToTime!!,
        )

        Log.d("SetScheduleActivity", "Availability: $availability")

        return availability
    }


    private fun isValidTimeRange(startTime: String, endTime: String): Boolean {
        val (startHour, startMinute) = startTime.split(":").map { it.toInt() }
        val (endHour, endMinute) = endTime.split(":").map { it.toInt() }

        val startInMinutes = startHour * 60 + startMinute
        val endInMinutes = endHour * 60 + endMinute

        return startInMinutes < endInMinutes
    }

    private fun initializeDayButtons() {
        val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
        val buttonIds = listOf(
            R.id.btnMonday,
            R.id.btnTuesday,
            R.id.btnWednesday,
            R.id.btnThursday,
            R.id.btnFriday,
            R.id.btnSaturday,
        )
        buttonIds.forEachIndexed { index, id ->
            val button = findViewById<MaterialButton>(id)
            dayButtons[button] = days[index]
            button.setOnClickListener {
                toggleDaySelection(button, days[index])
            }
        }
    }

    private fun toggleDaySelection(
        button: MaterialButton,
        day: String
    ) {
        if (selectedDays.contains(day)) {
            selectedDays.remove(day)
            button.isSelected = false
        } else {
            selectedDays.add(day)
            button.isSelected = true
        }

        updateConfirmButtonState()
    }
}