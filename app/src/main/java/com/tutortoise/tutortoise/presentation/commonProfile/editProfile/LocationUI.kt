package com.tutortoise.tutortoise.presentation.commonProfile.editProfile

import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.tutortoise.tutortoise.R
import com.tutortoise.tutortoise.databinding.ActivityEditProfileBinding

class LocationUI(
    private val activity: EditProfileActivity,
    private val binding: ActivityEditProfileBinding
) {
    fun showLoading() {
        binding.btnLocation.apply {
            isEnabled = false
            icon = createLoadingIndicator()
            text = "Getting location..."
        }
    }

    fun hideLoading() {
        binding.btnLocation.apply {
            isEnabled = true
            icon = ContextCompat.getDrawable(activity, R.drawable.ic_location)
        }
    }

    fun showError(message: String) {
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
    }

    fun updateLocationText(text: String) {
        binding.btnLocation.text = text
    }

    private fun createLoadingIndicator() = CircularProgressDrawable(activity).apply {
        setStyle(CircularProgressDrawable.DEFAULT)
        setColorSchemeColors(ContextCompat.getColor(activity, R.color.darkgreen))
        start()
    }
}