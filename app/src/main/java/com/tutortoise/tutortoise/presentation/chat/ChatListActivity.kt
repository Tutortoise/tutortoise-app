package com.tutortoise.tutortoise.presentation.chat

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.tutortoise.tutortoise.databinding.ActivityChatListBinding
import com.tutortoise.tutortoise.domain.AuthManager
import com.tutortoise.tutortoise.presentation.chat.adapter.ChatRoomAdapter

class ChatListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatListBinding
    private lateinit var viewModel: ChatViewModel
    private lateinit var adapter: ChatRoomAdapter

    private lateinit var database: DatabaseReference
    private var roomsListener: ValueEventListener? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ChatViewModel(this)

        database = Firebase.database.reference

        setupUI()
        setupRealtimeUpdates()
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


    private fun setupRealtimeUpdates() {
        val userId = AuthManager.getCurrentUserId() ?: return

        roomsListener = database.child("rooms")
            .orderByChild("lastMessageAt")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    viewModel.loadRooms()

                    database.child("presence").child(userId)
                        .setValue(
                            mapOf(
                                "isOnline" to true,
                                "lastSeen" to System.currentTimeMillis()
                            )
                        )
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ChatList", "Failed to listen for updates", error.toException())
                }
            })

        database.child("presence").child(userId)
            .onDisconnect()
            .setValue(
                mapOf(
                    "isOnline" to false,
                    "lastSeen" to System.currentTimeMillis()
                )
            )
    }

    private fun observeViewModel() {
        viewModel.rooms.observe(this) { rooms ->
            adapter.submitList(rooms)

            if (rooms.isNullOrEmpty()) {
                binding.recyclerView.visibility = View.GONE
                binding.emptyStateLayout.visibility = View.VISIBLE
            } else {
                binding.recyclerView.visibility = View.VISIBLE
                binding.emptyStateLayout.visibility = View.GONE
            }
        }

        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(this) { error ->
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadRooms()
    }

    override fun onDestroy() {
        super.onDestroy()
        roomsListener?.let { database.removeEventListener(it) }
        val userId = AuthManager.getCurrentUserId()
        if (userId != null) {
            database.child("presence").child(userId)
                .setValue(
                    mapOf(
                        "isOnline" to false,
                        "lastSeen" to System.currentTimeMillis()
                    )
                )
        }
    }
}