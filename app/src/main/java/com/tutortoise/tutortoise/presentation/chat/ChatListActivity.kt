package com.tutortoise.tutortoise.presentation.chat

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.tutortoise.tutortoise.R
import com.tutortoise.tutortoise.databinding.ActivityChatListBinding
import com.tutortoise.tutortoise.presentation.main.MainActivity

class ChatListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_list)

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