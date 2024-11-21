package com.tutortoise.tutortoise.presentation.menuUser.profile.general

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.tutortoise.tutortoise.R
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
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("startFragment", "profile")
        startActivity(intent)
        finish()
    }
}