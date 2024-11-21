package com.tutortoise.tutortoise.presentation.notification

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.tutortoise.tutortoise.R
import com.tutortoise.tutortoise.databinding.ActivityNotificationBinding
import com.tutortoise.tutortoise.presentation.main.MainActivity

class NotificationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNotificationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

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