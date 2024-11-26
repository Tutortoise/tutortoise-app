package com.tutortoise.tutortoise.presentation.register

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.rpc.context.AttributeContext.Auth
import com.tutortoise.tutortoise.R
import com.tutortoise.tutortoise.data.repository.ApiException
import com.tutortoise.tutortoise.data.repository.AuthRepository
import com.tutortoise.tutortoise.data.repository.FirebaseRepository
import com.tutortoise.tutortoise.databinding.ActivityLearnerRegisterBinding
import com.tutortoise.tutortoise.presentation.login.LoginActivity
import kotlinx.coroutines.launch

class LearnerRegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLearnerRegisterBinding
    private lateinit var authRepository: AuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLearnerRegisterBinding.inflate(layoutInflater)
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
        binding.progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val result = authRepository.register(
                    name = name,
                    email = email,
                    password = password,
                    role = "learner"
                )

                result.fold(
                    onSuccess = { response ->
                        Toast.makeText(this@LearnerRegisterActivity, "Registration successful!", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@LearnerRegisterActivity, LoginActivity::class.java)
                        startActivity(intent)
                        finish()
                    },
                    onFailure = { throwable ->
                        when (throwable) {
                            is ApiException -> {
                                // Handle validation errors
                                throwable.errorResponse?.errors?.forEach { error ->
                                    when (error.field) {
                                        "body.email" -> binding.tilEmail.error = error.message
                                        "body.name" -> binding.tilName.error = error.message
                                        "body.password" -> binding.tilPassword.error = error.message
                                        else -> Toast.makeText(
                                            this@LearnerRegisterActivity,
                                            error.message,
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                } ?: Toast.makeText(
                                    this@LearnerRegisterActivity,
                                    throwable.message,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            else -> Toast.makeText(
                                this@LearnerRegisterActivity,
                                "Registration failed: ${throwable.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                )
            } catch (e: Exception) {
                Toast.makeText(
                    this@LearnerRegisterActivity,
                    "Error: ${e.localizedMessage}",
                    Toast.LENGTH_SHORT
                ).show()
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun validateInput(name: String, email: String, password: String, confirmPassword: String): Boolean {
        var isValid = true

        if (name.isBlank()) {
            binding.etName.error = "Name is required"
            isValid = false
        }

        if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
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