package com.tutortoise.tutortoise.presentation.login

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.tutortoise.tutortoise.R
import com.tutortoise.tutortoise.databinding.ActivityLearnerRegisterBinding
import com.tutortoise.tutortoise.databinding.ActivityLoginBinding
import com.tutortoise.tutortoise.presentation.main.MainActivity
import com.tutortoise.tutortoise.presentation.onboarding.fragment.RegisterAsFragment

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        if (auth.currentUser != null) {
            // If user is logged in, redirect to the MainActivity
            navigateToMainActivity()
        }

        setupListeners()
    }

    private fun setupListeners() {
        binding.btnSignIn.setOnClickListener {
            val email = binding.tilEmail.editText?.text.toString().trim()
            val password = binding.tilPassword.editText?.text.toString().trim()

            if (validateInput(email, password)) {
                signIn(email, password)
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


    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            reload()
        }
    }

    private fun signIn(email: String, password: String) {
        auth.signOut()
        Log.d("LoginActivity", "Attempting to sign in with email: $email")
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    Log.d("LoginActivity", "Login successful for user: ${user?.email}")
                    Toast.makeText(this, "Welcome back, ${user?.displayName ?: "User"}!", Toast.LENGTH_SHORT).show()
                    navigateToMainActivity()
                } else {
                    Log.e("LoginActivity", "Authentication failed: ${task.exception?.message}")
                    Toast.makeText(
                        this,
                        "Authentication failed: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("startFragment", "home")
        startActivity(intent)
        finish()
    }

    private fun validateInput(email: String, password: String): Boolean {
        return email.isNotEmpty() && password.isNotEmpty()
    }

    private fun reload() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}