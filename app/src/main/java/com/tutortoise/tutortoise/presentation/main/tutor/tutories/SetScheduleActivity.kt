package com.tutortoise.tutortoise.presentation.main.tutor.tutories

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.tutortoise.tutortoise.R
import com.tutortoise.tutortoise.databinding.ActivitySetScheduleBinding
import com.tutortoise.tutortoise.presentation.main.tutor.tutories.dialog.TimePickerDialog

class SetScheduleActivity : AppCompatActivity() {
    private var selectedFromTime: String? = null
    private var selectedToTime: String? = null
    private val selectedDays = mutableSetOf<String>()
    private val dayButtons = mutableMapOf<MaterialButton, String>()

    private lateinit var binding: ActivitySetScheduleBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySetScheduleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initializeDayButtons()
        setupTimeButtons()
        setupConfirmButton()
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
                    binding.btnSelectFromTime.text = "From: $time"
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
                    binding.btnSelectToTime.text = "To: $time"
                    updateConfirmButtonState()
                },
                onError = { error ->
                    binding.toTimeLayout.error = error
                }
            ).show()
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
                    // TODO: Save schedule
                    Toast.makeText(this@SetScheduleActivity, scheduleInfo, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun validateSchedule(): Boolean {
        var isValid = true

        if (selectedDays.isEmpty()) {
            showError("Please select at least one day")
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

    private fun buildScheduleInfo(): String {
        val days = selectedDays.joinToString(", ")
        return "Schedule set for $days\nFrom: $selectedFromTime To: $selectedToTime"
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
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