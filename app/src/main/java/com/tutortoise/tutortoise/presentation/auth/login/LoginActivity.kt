package com.tutortoise.tutortoise.presentation.auth.login

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.tasks.Task
import com.tutortoise.tutortoise.data.pref.ApiException
import com.tutortoise.tutortoise.data.repository.AuthRepository
import com.tutortoise.tutortoise.data.repository.CustomOAuthException
import com.tutortoise.tutortoise.data.repository.LearnerRepository
import com.tutortoise.tutortoise.databinding.ActivityLoginBinding
import com.tutortoise.tutortoise.presentation.main.MainActivity
import com.tutortoise.tutortoise.presentation.questioner.QuestionnaireActivity
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var authRepository: AuthRepository
    private lateinit var learnerRepository: LearnerRepository

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            handleGoogleSignInResult(task)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        authRepository = AuthRepository(this)
        learnerRepository = LearnerRepository(this)

        if (authRepository.getToken() != null) {
            navigateToMainActivity()
        }

        setupListeners()
    }

    private fun setupListeners() {
        binding.btnSignIn.setOnClickListener {
            val email = binding.tilEmail.editText?.text.toString().trim()
            val password = binding.tilPassword.editText?.text.toString().trim()

            if (validateInput(email, password)) {
                loginUser(email, password)
            }
        }

        binding.btnSignUp.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("tutortoise://onboarding/register")
                setClassName(
                    this@LoginActivity,
                    "com.tutortoise.tutortoise.presentation.onboarding.OnboardingActivity"
                )
            }
            startActivity(intent)
        }

        binding.btnGoogleSignIn.setOnClickListener {
            lifecycleScope.launch {
                try {
                    val result = authRepository.authenticateWithGoogle(null)
                    result.fold(
                        onSuccess = {
                            navigateToMainActivity()
                        },
                        onFailure = { throwable ->
                            when (throwable) {
                                is CustomOAuthException -> {
                                    if (throwable.message == "LAUNCH_GOOGLE_SIGNIN") {
                                        // Launch Google Sign In
                                        val signInIntent = GoogleSignIn.getClient(
                                            this@LoginActivity,
                                            authRepository.getGoogleSignInOptions()
                                        ).signInIntent
                                        googleSignInLauncher.launch(signInIntent)
                                    } else {
                                        showError(throwable.message.toString())
                                    }
                                }

                                is ApiException -> showError("Your Google account is not registered with us")
                                is GetCredentialCancellationException -> {
                                    // Do nothing
                                }

                                else -> showError(throwable.toString())
                            }
                        }
                    )
                } catch (e: Exception) {
                    showError("Authentication failed: ${e.message}")
                }
            }
        }

        setupTextChangeListeners()
    }

    private fun handleGoogleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            account.idToken?.let { token ->
                lifecycleScope.launch {
                    val result = authRepository.handleGoogleAuthentication(token, null)
                    result.fold(
                        onSuccess = { navigateToMainActivity() },
                        onFailure = { throwable ->
                            when (throwable) {
                                is CustomOAuthException -> showError(throwable.message.toString())
                                else -> showError("Google sign-in failed")
                            }
                        }
                    )
                }
            } ?: showError("Failed to get ID token")
        } catch (e: ApiException) {
            showError("Google sign-in failed: ${e.message}")
        }
    }

    private fun setupTextChangeListeners() {
        binding.tilEmail.editText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.tilEmail.error = null
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.tilPassword.editText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.tilPassword.error = null
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private suspend fun handleSuccessfulLogin() {
        val userRole = authRepository.getUserRole()

        if (userRole == "learner") {
            val learnerProfile = learnerRepository.fetchLearnerProfile()

            if (learnerProfile?.data?.learningStyle == null ||
                learnerProfile.data.interests.isNullOrEmpty()
            ) {
                startActivity(Intent(this, QuestionnaireActivity::class.java))
            } else {
                startActivity(Intent(this, MainActivity::class.java))
            }
        } else {
            // For tutors, directly go to main activity
            startActivity(Intent(this, MainActivity::class.java))
        }
        finish()
    }

    private fun loginUser(email: String, password: String) {
        binding.tilEmail.error = null
        binding.tilPassword.error = null

        lifecycleScope.launch {
            try {
                val success = authRepository.login(email, password)
                if (success) {
                    showError("Login successful!")
                    handleSuccessfulLogin()
                } else {
                    binding.tilPassword.error = "Invalid email or password"
                }
            } catch (e: Exception) {
                showError("Login failed: ${e.message}")
            }
        }
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun validateInput(email: String, password: String): Boolean {
        var isValid = true

        if (email.isBlank()) {
            binding.tilEmail.error = "Email is required"
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = "Invalid email format"
            isValid = false
        }

        if (password.isBlank()) {
            binding.tilPassword.error = "Password is required"
            isValid = false
        }

        return isValid
    }
}