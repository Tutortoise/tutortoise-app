package com.tutortoise.tutortoise.presentation.auth.register

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.tutortoise.tutortoise.data.pref.ApiException
import com.tutortoise.tutortoise.data.repository.AuthRepository
import com.tutortoise.tutortoise.databinding.ActivityTutorRegisterBinding
import com.tutortoise.tutortoise.presentation.auth.login.LoginActivity
import kotlinx.coroutines.launch

class TutorRegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTutorRegisterBinding
    private lateinit var authRepository: AuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTutorRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        authRepository = AuthRepository(applicationContext)
        setupListeners()
    }

    private fun setupListeners() {
        binding.btnSignUp.setOnClickListener {
            val name = binding.etName.text.toString()
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()
            val confirmPassword = binding.etConfirmPassword.text.toString()

            if (validateInput(name, email, password, confirmPassword)) {
                registerUser(name, email, password)
            }
        }

        binding.tvSignIn.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }

    private fun registerUser(name: String, email: String, password: String) {
        // Clear any previous errors
        binding.tilEmail.error = null
        binding.tilName.error = null
        binding.tilPassword.error = null
        binding.tilConfirmPassword.error = null

        binding.progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val result = authRepository.register(
                    name = name,
                    email = email,
                    password = password,
                    role = "tutor"
                )

                result.fold(
                    onSuccess = { response ->
                        Log.d("TutorRegisterActivity", "Registration successful")
                        Toast.makeText(
                            this@TutorRegisterActivity,
                            "Registration successful!",
                            Toast.LENGTH_SHORT
                        ).show()
                        val intent = Intent(this@TutorRegisterActivity, LoginActivity::class.java)
                        startActivity(intent)
                        finish()
                    },
                    onFailure = { throwable ->
                        when (throwable) {
                            is ApiException -> {
                                binding.tilName.error = null
                                binding.tilEmail.error = null
                                binding.tilPassword.error = null

                                // Handle validation errors
                                throwable.errorResponse?.errors?.forEach { error ->
                                    when (error.field) {
                                        "body.email" -> binding.tilEmail.error = error.message
                                        "body.name" -> binding.tilName.error = error.message
                                        "body.password" -> binding.tilPassword.error = error.message
                                        else -> Toast.makeText(
                                            this@TutorRegisterActivity,
                                            error.message,
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                } ?: Toast.makeText(
                                    this@TutorRegisterActivity,
                                    throwable.message,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                            else -> {
                                Log.e("TutorRegisterActivity", "Registration error", throwable)
                                Toast.makeText(
                                    this@TutorRegisterActivity,
                                    "Registration failed: ${throwable.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                )
            } catch (e: Exception) {
                Log.e("TutorRegisterActivity", "Error during registration", e)
                Toast.makeText(
                    this@TutorRegisterActivity,
                    "Error: ${e.localizedMessage}",
                    Toast.LENGTH_SHORT
                ).show()
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun validateInput(
        name: String,
        email: String,
        password: String,
        confirmPassword: String
    ): Boolean {
        var isValid = true

        if (name.isBlank()) {
            binding.etName.error = "Name is required"
            isValid = false
        }

        if (email.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.error = "Valid email is required"
            isValid = false
        }

        if (password.isBlank() || password.length < 8) {
            binding.etPassword.error = "Password must be at least 8 characters"
            isValid = false
        }

        if (confirmPassword != password) {
            binding.etConfirmPassword.error = "Passwords do not match"
            isValid = false
        }

        return isValid
    }
}