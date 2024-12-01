package com.tutortoise.tutortoise.presentation.chat

import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tutortoise.tutortoise.databinding.ActivityChatRoomBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

class ChatRoomActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatRoomBinding
    private lateinit var adapter: ChatMessageAdapter
    private lateinit var viewModel: ChatViewModel
    private lateinit var roomId: String

    private var shouldScrollToBottom = true

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    companion object {
        private const val TAG = "ChatRoomActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatRoomBinding.inflate(layoutInflater)
        setContentView(binding.root)

        roomId = intent.getStringExtra("ROOM_ID") ?: run {
            Log.e(TAG, "No room ID provided")
            Toast.makeText(this, "Invalid room ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        Log.d(TAG, "Starting chat room with ID: $roomId")
        viewModel = ChatViewModel(this)
        setupUI()
        observeViewModel()
        viewModel.loadMessages(roomId)
    }

    private fun setupUI() {
        adapter = ChatMessageAdapter()
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@ChatRoomActivity).apply {
                stackFromEnd = true
                reverseLayout = true
            }
            adapter = this@ChatRoomActivity.adapter

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)

                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val lastVisibleItem = layoutManager.findLastVisibleItemPosition()
                    val totalItemCount = layoutManager.itemCount

                    // Load more when user scrolls near the top (since layout is reversed)
                    if (!viewModel.isLoading.value!! && !viewModel.isLoadingMore.value!! &&
                        lastVisibleItem >= totalItemCount - 5
                    ) {
                        viewModel.messages.value?.let { messages ->
                            if (messages.isNotEmpty()) {
                                shouldScrollToBottom = false // Don't scroll when loading more
                                viewModel.loadMoreMessages(roomId, messages.last().sentAt)
                            }
                        }
                    }
                }
            })
        }

        binding.btnSend.setOnClickListener {
            val message = binding.editMessage.text.toString().trim()
            if (message.isNotEmpty()) {
                viewModel.sendMessage(roomId, message)
                binding.editMessage.text.clear()
            }
        }

        binding.btnAttach.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }
    }

    private fun observeViewModel() {
        viewModel.messages.observe(this) { messages ->
            val recyclerView = binding.recyclerView
            val layoutManager = recyclerView.layoutManager as LinearLayoutManager

            // Store the current last visible position
            val lastVisiblePosition = layoutManager.findLastVisibleItemPosition()

            adapter.submitList(messages.toList()) {
                if (shouldScrollToBottom) {
                    // Scroll to bottom only for new messages
                    recyclerView.scrollToPosition(0)
                } else if (!viewModel.isLoading.value!! && lastVisiblePosition > 0) {
                    // Maintain scroll position when loading more messages
                    recyclerView.scrollToPosition(lastVisiblePosition + (messages.size - adapter.currentList.size))
                }
            }
        }

        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.isLoadingMore.observe(this) { isLoadingMore ->
            // You could show a different loading indicator for loading more
            binding.loadingMoreProgress.visibility = if (isLoadingMore) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(this) { error ->
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
        }

        binding.btnSend.setOnClickListener {
            val message = binding.editMessage.text.toString().trim()
            if (message.isNotEmpty()) {
                shouldScrollToBottom = true // Scroll to bottom for new sent message
                viewModel.sendMessage(roomId, message)
                binding.editMessage.text.clear()
            }
        }
    }

    private val imagePickerLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                // Convert Uri to base64 string
                val inputStream = contentResolver.openInputStream(uri)
                val bytes = inputStream?.readBytes()
                val base64String = Base64.encodeToString(bytes, Base64.DEFAULT)

                viewModel.sendMessage(roomId, base64String, isImage = true)
            }
        }

    override fun onDestroy() {
        scope.cancel() // Cancel all coroutines when the activity is destroyed
        super.onDestroy()
    }
}