package com.tutortoise.tutortoise.presentation.menuLearner.profile.general

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.tutortoise.tutortoise.databinding.ActivityChangePasswordBinding
import com.tutortoise.tutortoise.presentation.main.MainActivity

class ChangePasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChangePasswordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangePasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener {
            backToProfile()
        }
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