package com.tutortoise.tutortoise.presentation.commonProfile

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.tutortoise.tutortoise.R
import com.tutortoise.tutortoise.data.pref.ApiConfig
import com.tutortoise.tutortoise.data.pref.ApiService
import com.tutortoise.tutortoise.data.pref.ChangePasswordRequest
import com.tutortoise.tutortoise.data.repository.AuthRepository
import com.tutortoise.tutortoise.databinding.ActivityChangePasswordBinding
import com.tutortoise.tutortoise.presentation.main.MainActivity
import kotlinx.coroutines.launch

class ChangePasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChangePasswordBinding
    private lateinit var apiService: ApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangePasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        apiService = ApiConfig.getApiService(this)

        binding.btnBack.setOnClickListener {
            backToProfile()
        }

        binding.btnChangePassword.setOnClickListener {
            if (validateInputs()) {
                changePassword()
            }
        }

        // Clear errors on text change
        binding.etCurrentPassword.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                binding.tilCurrentPassword.error = null
            }
        }

        binding.etNewPassword.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                binding.tilNewPassword.error = null
            }
        }

        binding.etConfirmPassword.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                binding.tilConfirmPassword.error = null
            }
        }
    }

    private fun validateInputs(): Boolean {
        var isValid = true

        // Reset errors
        binding.tilCurrentPassword.error = null
        binding.tilNewPassword.error = null
        binding.tilConfirmPassword.error = null

        val currentPassword = binding.etCurrentPassword.text.toString()
        val newPassword = binding.etNewPassword.text.toString()
        val confirmPassword = binding.etConfirmPassword.text.toString()

        // Validate current password
        if (currentPassword.isEmpty()) {
            binding.tilCurrentPassword.error = getString(R.string.error_current_password_required)
            isValid = false
        }

        // Validate new password
        if (newPassword.isEmpty()) {
            binding.tilNewPassword.error = getString(R.string.error_new_password_required)
            isValid = false
        } else if (newPassword.length < 8) {
            binding.tilNewPassword.error = getString(R.string.error_password_too_short)
            isValid = false
        }

        // Validate confirm password
        if (confirmPassword.isEmpty()) {
            binding.tilConfirmPassword.error = getString(R.string.error_confirm_password_required)
            isValid = false
        } else if (newPassword != confirmPassword) {
            binding.tilConfirmPassword.error = getString(R.string.error_passwords_dont_match)
            isValid = false
        }

        return isValid
    }

    private fun changePassword() {
        // Show loading state
        setLoadingState(true)

        lifecycleScope.launch {
            try {
                val request = ChangePasswordRequest(
                    currentPassword = binding.etCurrentPassword.text.toString(),
                    newPassword = binding.etNewPassword.text.toString(),
                    confirmPassword = binding.etConfirmPassword.text.toString()
                )

                val response =
                    if (AuthRepository(this@ChangePasswordActivity).getUserRole() == "learner") {
                        apiService.changeLearnerPassword(request)
                    } else {
                        apiService.changeTutorPassword(request)
                    }

                if (response.isSuccessful) {
                    Snackbar.make(
                        binding.root,
                        getString(R.string.success_password_changed),
                        Snackbar.LENGTH_SHORT
                    ).show()

                    // Delay a bit to show the success message
                    kotlinx.coroutines.delay(1000)
                    backToProfile()
                } else {
                    val error = ApiConfig.parseError(response)
                    when {
                        error?.message?.contains("current password", ignoreCase = true) == true -> {
                            binding.tilCurrentPassword.error =
                                getString(R.string.error_incorrect_current_password)
                        }

                        else -> {
                            showErrorSnackbar(
                                error?.message ?: getString(R.string.error_change_password_failed)
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                showErrorSnackbar(getString(R.string.error_change_password_failed))
            }

            setLoadingState(false)
        }
    }

    private fun setLoadingState(isLoading: Boolean) {
        binding.apply {
            btnChangePassword.isEnabled = !isLoading

            // Disable input fields during loading
            etCurrentPassword.isEnabled = !isLoading
            etNewPassword.isEnabled = !isLoading
            etConfirmPassword.isEnabled = !isLoading
        }
    }

    private fun showErrorSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setAction("OK") { }
            .show()
    }

    private fun backToProfile() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("startFragment", "profile")
        }
        startActivity(intent)
        finish()
    }
}