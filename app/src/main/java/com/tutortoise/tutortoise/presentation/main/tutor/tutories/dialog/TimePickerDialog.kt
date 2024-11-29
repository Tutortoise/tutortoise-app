package com.tutortoise.tutortoise.presentation.main.tutor.tutories.dialog

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import com.tutortoise.tutortoise.databinding.FragmentDialogTimePickerBinding

class TimePickerDialog(
    private val context: Context,
    private val isFromTime: Boolean,
    private val currentTime: String? = null,
    private val onTimeSelected: (String) -> Unit,
    private val onError: (String) -> Unit
) {
    private val validHours = (7..21).toList()
    private val validMinutes = listOf(0, 30)

    fun show() {
        val binding = FragmentDialogTimePickerBinding.inflate(LayoutInflater.from(context))

        // Parse default values
        var defaultHour = 7
        var defaultMinute = 0

        currentTime?.let {
            val parts = it.split(":")
            defaultHour = parts[0].toInt()
            defaultMinute = parts[1].toInt()
        }

        // Setup hour picker
        binding.hourPicker.apply {
            minValue = 0
            maxValue = validHours.size - 1
            displayedValues = validHours.map { String.format("%02d", it) }.toTypedArray()
            value = validHours.indexOf(defaultHour)
            wrapSelectorWheel = false
        }

        // Setup minute picker
        binding.minutePicker.apply {
            minValue = 0
            maxValue = validMinutes.size - 1
            displayedValues = validMinutes.map { String.format("%02d", it) }.toTypedArray()
            value = validMinutes.indexOf(defaultMinute.coerceIn(0, 30))
            wrapSelectorWheel = false
        }

        // Disable 30 minutes for 21:00
        binding.hourPicker.setOnValueChangedListener { _, _, newVal ->
            if (validHours[newVal] == 21) {
                binding.minutePicker.value = 0
                binding.minutePicker.isEnabled = false
            } else {
                binding.minutePicker.isEnabled = true
            }
        }

        // Create and show dialog
        AlertDialog.Builder(context)
            .setTitle(if (isFromTime) "Select Start Time" else "Select End Time")
            .setView(binding.root)
            .setPositiveButton("OK") { _, _ ->
                val hour = validHours[binding.hourPicker.value]
                val minute = validMinutes[binding.minutePicker.value]

                if (isValidTimeRange(hour, minute)) {
                    val formattedTime = String.format("%02d:%02d", hour, minute)
                    onTimeSelected(formattedTime)
                } else {
                    onError("Please select time between 07:00 and 21:00")
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun isValidTimeRange(hour: Int, minute: Int): Boolean {
        return when {
            hour < 7 -> false
            hour > 21 -> false
            hour == 21 && minute > 0 -> false
            else -> true
        }
    }
}