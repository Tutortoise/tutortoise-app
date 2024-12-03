package com.tutortoise.tutortoise.presentation.questioner

import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.tutortoise.tutortoise.R
import com.tutortoise.tutortoise.data.repository.AuthRepository
import com.tutortoise.tutortoise.databinding.ActivityQuestionnaireBinding
import com.tutortoise.tutortoise.presentation.auth.login.LoginActivity
import com.tutortoise.tutortoise.presentation.main.MainActivity

class QuestionnaireActivity : AppCompatActivity() {
    private lateinit var binding: ActivityQuestionnaireBinding
    private var currentFragment = 0
    private lateinit var authRepository: AuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuestionnaireBinding.inflate(layoutInflater)
        setContentView(binding.root)

        authRepository = AuthRepository(this)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                showExitConfirmationDialog()
            }
        })

        // Start with first questionnaire fragment
        supportFragmentManager.beginTransaction()
            .replace(R.id.questionnaireContainer, QuestionerStyleFragment())
            .commit()
    }

    fun showExitConfirmationDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Exit Confirmation")
            .setMessage("You need to complete your profile setup before proceeding. Are you sure you want to exit?")
            .setPositiveButton("Stay") { dialog, _ ->
                dialog.dismiss()
            }
            .setNegativeButton("Exit") { _, _ ->
                authRepository.clearToken()

                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            .setCancelable(false)
            .show()
    }

    fun navigateToNext() {
        currentFragment++
        when (currentFragment) {
            1 -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.questionnaireContainer, QuestionerInterestFragment())
                    .commit()
            }

            2 -> {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }
    }
}