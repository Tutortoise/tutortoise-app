package com.tutortoise.tutortoise.presentation.main.tutor.tutories.dialog

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.tutortoise.tutortoise.R

class TimePickerDialog(
    private val context: Context,
    private val isFromTime: Boolean,
    private val currentTime: String? = null,
    private val onTimeSelected: (String) -> Unit,
    private val onError: (String) -> Unit
) {
    fun show() {
        var defaultHour = 7
        var defaultMinute = 0

        currentTime?.let {
            val parts = it.split(":")
            defaultHour = parts[0].toInt()
            defaultMinute = parts[1].toInt()
        }

        val picker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setHour(defaultHour)
            .setMinute(defaultMinute)
            .setInputMode(MaterialTimePicker.INPUT_MODE_CLOCK)
            .setTitleText(if (isFromTime) "Select Start Time" else "Select End Time")
            .setPositiveButtonText("Confirm")
            .setNegativeButtonText("Cancel")
            .setTheme(R.style.MaterialTimePickerTheme)
            .build()

        picker.addOnPositiveButtonClickListener {
            val hour = picker.hour
            val minute = picker.minute

            if (!isValidTimeRange(hour, minute)) {
                onError("Please select time between 07:00 and 21:00")
                return@addOnPositiveButtonClickListener
            }

            val roundedMinute = if (minute < 30) 0 else 30
            val formattedTime = String.format("%02d:%02d", hour, roundedMinute)
            onTimeSelected(formattedTime)
        }

        picker.show((context as AppCompatActivity).supportFragmentManager, "time_picker")
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