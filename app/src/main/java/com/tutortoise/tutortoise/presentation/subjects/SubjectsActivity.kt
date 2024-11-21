package com.tutortoise.tutortoise.presentation.subjects

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.tutortoise.tutortoise.R
import com.tutortoise.tutortoise.databinding.ActivitySubjectsBinding
import com.tutortoise.tutortoise.presentation.main.MainActivity

class SubjectsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySubjectsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_subjects)

        binding.btnBack.setOnClickListener {
            backToHome()
        }
    }

    private fun backToHome() {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("startFragment", "home")
        startActivity(intent)
        finish()

    }
}