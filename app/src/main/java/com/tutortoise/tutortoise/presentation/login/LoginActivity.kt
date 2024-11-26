package com.tutortoise.tutortoise.presentation.login

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.google.rpc.context.AttributeContext.Auth
import com.tutortoise.tutortoise.data.repository.AuthRepository
import com.tutortoise.tutortoise.databinding.ActivityLoginBinding
import com.tutortoise.tutortoise.presentation.main.MainActivity
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var AuthRepository: AuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        AuthRepository = AuthRepository(applicationContext)

        if (AuthRepository.getToken() != null) {
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
            } else {
                Toast.makeText(this, "Please enter both email and password.", Toast.LENGTH_SHORT)
                    .show()
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
    }

    private fun loginUser(email: String, password: String) {
        lifecycleScope.launch {
            val success = AuthRepository.login(email, password)
            if (success) {
                Toast.makeText(this@LoginActivity, "Login successful!", Toast.LENGTH_SHORT).show()
                navigateToMainActivity()
            } else {
                Toast.makeText(this@LoginActivity, "Login failed!", Toast.LENGTH_SHORT).show()
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
        return email.isNotEmpty() && password.isNotEmpty()
    }


//    private lateinit var binding: ActivityLoginBinding
//    private lateinit var auth: FirebaseAuth
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        binding = ActivityLoginBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//        auth = FirebaseAuth.getInstance()
//
//        if (auth.currentUser != null) {
//            // If user is logged in, redirect to the MainActivity
//            navigateToMainActivity()
//        }
//
//        setupListeners()
//    }
//
//    private fun setupListeners() {
//        binding.btnSignIn.setOnClickListener {
//            val email = binding.tilEmail.editText?.text.toString().trim()
//            val password = binding.tilPassword.editText?.text.toString().trim()
//
//            if (validateInput(email, password)) {
//                signIn(email, password)
//            } else {
//                Toast.makeText(this, "Please enter both email and password.", Toast.LENGTH_SHORT)
//                    .show()
//            }
//        }
//
//        binding.btnSignUp.setOnClickListener {
//            val intent = Intent(Intent.ACTION_VIEW).apply {
//                data = Uri.parse("tutortoise://onboarding/register")
//                setClassName(
//                    this@LoginActivity,
//                    "com.tutortoise.tutortoise.presentation.onboarding.OnboardingActivity"
//                )
//            }
//            startActivity(intent)
//        }
//    }
//
//    private fun signIn(email: String, password: String) {
//        auth.signOut()
//        Log.d("LoginActivity", "Attempting to sign in with email: $email")
//        auth.signInWithEmailAndPassword(email, password)
//            .addOnCompleteListener(this) { task ->
//                if (task.isSuccessful) {
//                    val user = auth.currentUser
//                    Log.d("LoginActivity", "Login successful for user: ${user?.email}")
//                    Toast.makeText(this, "Welcome back, ${user?.displayName ?: "User"}!", Toast.LENGTH_SHORT).show()
//                    navigateToMainActivity()
//                } else {
//                    Log.e("LoginActivity", "Authentication failed: ${task.exception?.message}")
//                    Toast.makeText(
//                        this,
//                        "Authentication failed: ${task.exception?.message}",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                }
//            }
//    }
//
//    private fun navigateToMainActivity() {
//        val intent = Intent(this, MainActivity::class.java)
//        intent.putExtra("startFragment", "home")
//        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//        startActivity(intent)
//        finish()
//    }
//
//    private fun validateInput(email: String, password: String): Boolean {
//        return email.isNotEmpty() && password.isNotEmpty()
//    }
}