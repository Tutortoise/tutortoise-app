package com.tutortoise.tutortoise.presentation.chat

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.tutortoise.tutortoise.R
import com.tutortoise.tutortoise.data.model.ChatRoom
import com.tutortoise.tutortoise.databinding.ActivityChatRoomBinding
import com.tutortoise.tutortoise.domain.AuthManager
import com.tutortoise.tutortoise.presentation.chat.adapter.ChatMessageAdapter
import com.tutortoise.tutortoise.utils.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import java.io.ByteArrayOutputStream

class ChatRoomActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatRoomBinding
    private lateinit var adapter: ChatMessageAdapter
    private lateinit var viewModel: ChatViewModel

    private var roomId: String? = null
    private val learnerId: String by lazy { intent.getStringExtra("LEARNER_ID") ?: "" }
    private val tutorId: String by lazy { intent.getStringExtra("TUTOR_ID") ?: "" }
    private val tutorName: String by lazy { intent.getStringExtra("TUTOR_NAME") ?: "" }

    private var shouldScrollToBottom = true

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    companion object {
        private const val TAG = "ChatRoomActivity"
    }

    private val isLearner by lazy { AuthManager.getInstance()?.getUserRole() == "learner" }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatRoomBinding.inflate(layoutInflater)
        setContentView(binding.root)

        roomId = intent.getStringExtra("ROOM_ID")
        Log.d("ChatRoomActivity", "Received Room ID: $roomId")
        viewModel = ChatViewModel(this)

        setupUI()
        setupToolbar()
        observeViewModel()

        if (roomId != null) {
            // Existing room
            val room = ChatRoom(
                id = roomId!!,
                learnerId = learnerId,
                tutorId = tutorId,
                lastMessageAt = "",
                createdAt = "",
                learnerName = intent.getStringExtra("LEARNER_NAME") ?: "",
                tutorName = intent.getStringExtra("TUTOR_NAME") ?: "",
                lastMessage = null
            )
            viewModel.setChatPartner(isLearner, room)
            viewModel.listenForNewMessages(roomId!!)
            viewModel.loadMessages(roomId!!)
        } else {
            // New room - set basic info
            val room = ChatRoom(
                id = "",  // Empty for new room
                learnerId = learnerId,
                tutorId = tutorId,
                lastMessageAt = "",
                createdAt = "",
                learnerName = "",  // We might not have this yet
                tutorName = tutorName,  // We should have this from navigation
                lastMessage = null
            )
            viewModel.setChatPartner(isLearner, room)
        }
    }

    private fun setupToolbar() {
        binding.btnBack.setOnClickListener { finish() }
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

                    if (!viewModel.isLoading.value!! && !viewModel.isLoadingMore.value!! &&
                        lastVisibleItem >= totalItemCount - 5
                    ) {
                        viewModel.messages.value?.let { messages ->
                            if (messages.isNotEmpty() && roomId != null) {
                                shouldScrollToBottom = false
                                viewModel.loadMoreMessages(roomId!!, messages.last().sentAt)
                            }
                        }
                    }
                }
            })
        }

        binding.btnSend.setOnClickListener {
            val message = binding.editMessage.text.toString().trim()
            if (message.isNotEmpty()) {
                shouldScrollToBottom = true
                viewModel.sendMessage(roomId, message)
                binding.editMessage.text.clear()
            }
        }

        binding.btnAttach.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }
    }

    private fun observeViewModel() {
        viewModel.roomCreated.observe(this) { newRoomId ->
            roomId = newRoomId
        }

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
                shouldScrollToBottom = true
                if (roomId == null) {
                    viewModel.createRoomAndSendMessage(learnerId, tutorId, message)
                } else {
                    viewModel.sendMessage(roomId!!, message)
                }
                binding.editMessage.text.clear()
            }
        }

        binding.btnAttach.setOnClickListener {
            if (roomId == null) {
                Toast.makeText(this, "Please send a message first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            imagePickerLauncher.launch("image/*")
        }

        viewModel.chatPartner.observe(this) { (name, id) ->
            binding.tvName.text = name

            // Load profile picture
            Glide.with(this)
                .load(Constants.getProfilePictureUrl(id))
                .placeholder(R.drawable.default_profile_picture)
                .error(R.drawable.default_profile_picture)
                .circleCrop()
                .into(binding.profileImage)
        }
    }

    private val imagePickerLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                try {
                    val inputStream = contentResolver.openInputStream(uri)
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    val outputStream = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
                    val bytes = outputStream.toByteArray()
                    val base64String = Base64.encodeToString(bytes, Base64.DEFAULT)

                    roomId?.let { id ->
                        viewModel.sendMessage(id, base64String, isImage = true)
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, "Failed to process image", Toast.LENGTH_SHORT).show()
                }
            }
        }

    override fun onDestroy() {
        viewModel.removeMessageListener()
        scope.cancel() // Cancel all coroutines when the activity is destroyed
        super.onDestroy()
    }
}