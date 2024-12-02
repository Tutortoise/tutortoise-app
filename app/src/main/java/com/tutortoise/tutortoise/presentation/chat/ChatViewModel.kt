package com.tutortoise.tutortoise.presentation.chat

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tutortoise.tutortoise.data.model.ChatMessage
import com.tutortoise.tutortoise.data.model.ChatRoom
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

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private var _isLoadingMore = MutableLiveData<Boolean>(false)
    val isLoadingMore: LiveData<Boolean> = _isLoadingMore

    private var _hasMoreMessages = MutableLiveData<Boolean>(true)

    private val _roomCreated = MutableLiveData<String>()
    val roomCreated: LiveData<String> = _roomCreated

    init {
        _isLoading.value = false
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

                result.fold(
                    onSuccess = { message ->
                        val currentMessages = _messages.value?.toMutableList() ?: mutableListOf()
                        currentMessages.add(0, message)
                        _messages.value = currentMessages
                    },
                    onFailure = { _error.value = it.message }
                )
            } catch (e: Exception) {
                _error.value = "Failed to send message"
            }
        }
    }

    fun createRoomAndSendMessage(learnerId: String, tutorId: String, message: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val result = withContext(Dispatchers.IO) {
                    ChatManager.findOrCreateChatRoom(context, tutorId)
                }

                result.fold(
                    onSuccess = { room ->
                        _roomCreated.value = room.id
                        sendMessage(room.id, message)
                    },
                    onFailure = {
                        _error.value = it.message
                    }
                )
            } catch (e: Exception) {
                _error.value = "Failed to create chat room"
            } finally {
                _isLoading.value = false
            }
        }
    }

    companion object {
        const val PAGE_SIZE = 20
    }

    override fun onCleared() {
        super.onCleared()
    }
}