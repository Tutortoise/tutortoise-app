package com.tutortoise.tutortoise.presentation.chat

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.tutortoise.tutortoise.data.model.ChatMessage
import com.tutortoise.tutortoise.data.model.ChatRoom
import com.tutortoise.tutortoise.data.model.FirebaseMessage
import com.tutortoise.tutortoise.data.repository.ChatRepository
import com.tutortoise.tutortoise.domain.ChatManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChatViewModel(private val context: Context) : ViewModel() {
    private val TAG = "ChatViewModel"
    private val chatRepository = ChatRepository(context)
    private val _messages = MutableLiveData<List<ChatMessage>>()
    val messages: LiveData<List<ChatMessage>> = _messages

    private val _rooms = MutableLiveData<List<ChatRoom>>()
    val rooms: LiveData<List<ChatRoom>> = _rooms

    private val _chatPartner = MutableLiveData<Pair<String, String>>() // (name, id)
    val chatPartner: LiveData<Pair<String, String>> = _chatPartner

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private var _isLoadingMore = MutableLiveData<Boolean>(false)
    val isLoadingMore: LiveData<Boolean> = _isLoadingMore

    private var _hasMoreMessages = MutableLiveData<Boolean>(true)

    private val _roomCreated = MutableLiveData<String>()
    val roomCreated: LiveData<String> = _roomCreated


    private val database = Firebase.database.reference
    private var messageListener: ValueEventListener? = null

    init {
        _isLoading.value = false
    }

    fun setChatPartner(isLearner: Boolean, room: ChatRoom) {
        _chatPartner.value = if (isLearner) {
            Pair(room.tutorName, room.tutorId)
        } else {
            Pair(room.learnerName, room.learnerId)
        }
    }

    fun loadRooms() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val result = withContext(Dispatchers.IO) {
                    chatRepository.getRooms()
                }
                result.fold(
                    onSuccess = { _rooms.value = it },
                    onFailure = { _error.value = it.message }
                )
            } catch (e: Exception) {
                _error.value = "Failed to load chat rooms"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadMoreMessages(roomId: String, before: String) {
        if (_isLoadingMore.value == true || _hasMoreMessages.value == false) return

        viewModelScope.launch {
            try {
                _isLoadingMore.value = true
                val result = chatRepository.getRoomMessages(roomId, before)
                result.fold(
                    onSuccess = { newMessages ->
                        _hasMoreMessages.value = newMessages.size >= PAGE_SIZE
                        val currentMessages = _messages.value?.toMutableList() ?: mutableListOf()
                        // Add new messages to the end since we're using reverseLayout
                        currentMessages.addAll(newMessages)
                        _messages.value = currentMessages
                    },
                    onFailure = {
                        _error.value = it.message
                    }
                )
            } catch (e: Exception) {
                _error.value = "Failed to load more messages"
            } finally {
                _isLoadingMore.value = false
            }
        }
    }

    fun loadMessages(roomId: String) {
        if (_isLoading.value == true) return

        viewModelScope.launch {
            try {
                _isLoading.value = true
                Log.d(TAG, "Loading initial messages for room: $roomId")
                val result = chatRepository.getRoomMessages(roomId)
                result.fold(
                    onSuccess = { messages ->
                        Log.d(TAG, "Successfully loaded ${messages.size} messages")
                        _hasMoreMessages.value = messages.size >= PAGE_SIZE
                        _messages.value = messages
                    },
                    onFailure = {
                        Log.e(TAG, "Failed to load messages", it)
                        _error.value = it.message
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Exception while loading messages", e)
                _error.value = "Failed to load messages"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun sendMessage(roomId: String?, content: String, isImage: Boolean = false) {
        if (roomId == null) {
            _error.value = "Invalid room ID"
            return
        }

        viewModelScope.launch {
            try {
                val result = chatRepository.sendMessage(
                    roomId,
                    content,
                    if (isImage) "image" else "text"
                )

                result.onFailure { _error.value = it.message }
            } catch (e: Exception) {
                _error.value = "Failed to send message"
            }
        }
    }


    suspend fun createRoom(learnerId: String, tutorId: String): String? {
        return try {
            _isLoading.value = true
            val result = withContext(Dispatchers.IO) {
                ChatManager.findOrCreateChatRoom(context, tutorId)
            }

            result.fold(
                onSuccess = { room ->
                    _roomCreated.value = room.id
                    listenForNewMessages(room.id)
                    room.id // Return room ID
                },
                onFailure = {
                    _error.value = it.message
                    null // Return null if creation fails
                }
            )
        } catch (e: Exception) {
            _error.value = "Failed to create chat room"
            null
        } finally {
            _isLoading.value = false
        }
    }

    fun sendMessageToRoom(roomId: String, message: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                sendMessage(roomId, message)
            } catch (e: Exception) {
                _error.value = "Failed to send message"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun listenForNewMessages(roomId: String) {
        viewModelScope.launch {
            try {
                val result = chatRepository.getRoomMessages(roomId)
                result.fold(
                    onSuccess = { apiMessages ->
                        _messages.value = apiMessages

                        setupFirebaseMessageListener(roomId)
                    },
                    onFailure = { _error.value = it.message }
                )
            } catch (e: Exception) {
                _error.value = "Failed to load messages"
            }
        }
    }

    private fun setupFirebaseMessageListener(roomId: String) {
        messageListener?.let { database.child("messages").child(roomId).removeEventListener(it) }

        messageListener = database.child("messages")
            .child(roomId)
            .orderByChild("sentAt")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    try {
                        val firebaseMessages = mutableListOf<ChatMessage>()
                        snapshot.children.forEach { messageSnap ->
                            messageSnap.getValue(FirebaseMessage::class.java)?.let { fbMessage ->
                                firebaseMessages.add(fbMessage.toChatMessage())
                            }
                        }

                        val currentMessages = _messages.value?.toMutableList() ?: mutableListOf()

                        firebaseMessages.forEach { fbMessage ->
                            if (!currentMessages.any { it.id == fbMessage.id }) {
                                currentMessages.add(fbMessage)
                            }
                        }

                        _messages.postValue(currentMessages.sortedByDescending { it.sentAt })
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing messages", e)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "Failed to listen for messages", error.toException())
                    _error.postValue("Failed to listen for new messages: ${error.message}")
                }
            })
    }

    fun removeMessageListener() {
        messageListener?.let { database.removeEventListener(it) }
        messageListener = null
    }

    companion object {
        const val PAGE_SIZE = 20
    }

    override fun onCleared() {
        super.onCleared()
        messageListener?.let { database.removeEventListener(it) }
    }
}