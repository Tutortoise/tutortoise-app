package com.tutortoise.tutortoise.presentation.commonProfile

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.tutortoise.tutortoise.databinding.ActivityMyActivityBinding
import com.tutortoise.tutortoise.presentation.main.MainActivity

class MyActivityActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMyActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener {
            backToProfile()
        }
    }

    private fun backToProfile() {
        val intent = Intent(this, MainActivity::class.java).apply {
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("startFragment", "profile")
        }
        startActivity(intent)
        finish()
    }
}