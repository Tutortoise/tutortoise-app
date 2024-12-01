package com.tutortoise.tutortoise.presentation.chat

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.tutortoise.tutortoise.databinding.ActivityChatListBinding

class ChatListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener {
            finish()
        }

    }
}