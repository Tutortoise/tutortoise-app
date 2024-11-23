package com.tutortoise.tutortoise.presentation.menuLearner.detail

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.tutortoise.tutortoise.R
import com.tutortoise.tutortoise.databinding.ActivityDetailTutorBinding
import com.tutortoise.tutortoise.presentation.main.MainActivity

class DetailTutorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailTutorBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailTutorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener {
            finish()
        }
    }

}