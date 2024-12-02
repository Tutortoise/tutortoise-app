package com.tutortoise.tutortoise.presentation.chat

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.tutortoise.tutortoise.databinding.ActivityChatListBinding
import com.tutortoise.tutortoise.presentation.chat.adapter.ChatRoomAdapter

class ChatListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatListBinding
    private lateinit var viewModel: ChatViewModel
    private lateinit var adapter: ChatRoomAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ChatViewModel(this)

        setupUI()
        observeViewModel()
        viewModel.loadRooms()
    }

    private fun setupUI() {
        adapter = ChatRoomAdapter { room ->
            val intent = Intent(this, ChatRoomActivity::class.java).apply {
                putExtra("ROOM_ID", room.id)
                putExtra("LEARNER_ID", room.learnerId)
                putExtra("TUTOR_ID", room.tutorId)
                putExtra("LEARNER_NAME", room.learnerName)
                putExtra("TUTOR_NAME", room.tutorName)
            }
            startActivity(intent)
        }

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@ChatListActivity)
            adapter = this@ChatListActivity.adapter
        }

        binding.btnBack.setOnClickListener { finish() }
    }

    private fun observeViewModel() {
        viewModel.rooms.observe(this) { rooms ->
            adapter.submitList(rooms)
        }

        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(this) { error ->
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
        }
    }
}