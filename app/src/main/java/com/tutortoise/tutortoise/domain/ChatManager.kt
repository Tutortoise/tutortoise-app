package com.tutortoise.tutortoise.domain

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.tutortoise.tutortoise.data.model.ChatRoom
import com.tutortoise.tutortoise.data.repository.ChatRepository
import com.tutortoise.tutortoise.presentation.chat.ChatRoomActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object ChatManager {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private const val TAG = "ChatUtils"

    fun navigateToChat(context: Context, tutorId: String, tutorName: String? = null) {
        scope.launch {
            try {
                Log.d(TAG, "Starting navigation to chat with tutor: $tutorId")
                val learnerId = AuthManager.getCurrentUserId() ?: run {
                    Toast.makeText(context, "User not authenticated", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                // Check for existing room first
                val chatRepository = ChatRepository(context)
                val roomsResult = chatRepository.getRooms()

                roomsResult.fold(
                    onSuccess = { rooms ->
                        // Try to find existing room
                        val existingRoom = rooms.find { it.tutorId == tutorId }

                        val intent = Intent(context, ChatRoomActivity::class.java).apply {
                            if (existingRoom != null) {
                                // If room exists, pass all room info
                                putExtra("ROOM_ID", existingRoom.id)
                                putExtra("LEARNER_ID", existingRoom.learnerId)
                                putExtra("TUTOR_ID", existingRoom.tutorId)
                                putExtra("LEARNER_NAME", existingRoom.learnerName)
                                putExtra("TUTOR_NAME", existingRoom.tutorName)
                            } else {
                                // For new room, pass what we know
                                putExtra("LEARNER_ID", learnerId)
                                putExtra("TUTOR_ID", tutorId)
                                putExtra("TUTOR_NAME", tutorName)
                            }
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(intent)
                    },
                    onFailure = { error ->
                        // If we fail to get rooms, still allow navigation but with basic info
                        val intent = Intent(context, ChatRoomActivity::class.java).apply {
                            putExtra("LEARNER_ID", learnerId)
                            putExtra("TUTOR_ID", tutorId)
                            putExtra("TUTOR_NAME", tutorName)
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(intent)
                        Log.e(TAG, "Failed to check existing rooms", error)
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Exception in navigateToChat", e)
                Toast.makeText(context, "Failed to start chat", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun navigateToChatFromTutor(context: Context, learnerId: String, learnerName: String? = null) {
        scope.launch {
            try {
                Log.d(TAG, "Starting navigation to chat with learner: $learnerId")
                val tutorId = AuthManager.getCurrentUserId() ?: run {
                    Toast.makeText(context, "User not authenticated", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                // Check for existing room first
                val chatRepository = ChatRepository(context)
                val roomsResult = chatRepository.getRooms()

                roomsResult.fold(
                    onSuccess = { rooms ->
                        // Try to find existing room
                        val existingRoom = rooms.find { it.learnerId == learnerId }

                        val intent = Intent(context, ChatRoomActivity::class.java).apply {
                            if (existingRoom != null) {
                                // If room exists, pass all room info
                                putExtra("ROOM_ID", existingRoom.id)
                                putExtra("LEARNER_ID", existingRoom.learnerId)
                                putExtra("TUTOR_ID", existingRoom.tutorId)
                                putExtra("LEARNER_NAME", existingRoom.learnerName)
                                putExtra("TUTOR_NAME", existingRoom.tutorName)
                            } else {
                                // For new room, pass what we know
                                putExtra("LEARNER_ID", learnerId)
                                putExtra("TUTOR_ID", tutorId)
                                putExtra("LEARNER_NAME", learnerName)
                            }
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(intent)
                    },
                    onFailure = { error ->
                        // If we fail to get rooms, still allow navigation but with basic info
                        val intent = Intent(context, ChatRoomActivity::class.java).apply {
                            putExtra("LEARNER_ID", learnerId)
                            putExtra("TUTOR_ID", tutorId)
                            putExtra("LEARNER_NAME", learnerName)
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(intent)
                        Log.e(TAG, "Failed to check existing rooms", error)
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Exception in navigateToChat", e)
                Toast.makeText(context, "Failed to start chat", Toast.LENGTH_SHORT).show()
            }
        }
    }

    suspend fun findOrCreateChatRoom(
        context: Context,
        learnerId: String,
        tutorId: String
    ): Result<ChatRoom> =
        withContext(Dispatchers.IO) {
            try {
                val chatRepository = ChatRepository(context)

                // First try to find existing room
                val roomsResult = chatRepository.getRooms()
                roomsResult.fold(
                    onSuccess = { rooms ->
                        Log.d(TAG, "Found ${rooms.size} chat rooms")

                        rooms.find { it.learnerId == learnerId && it.tutorId == tutorId }?.let {
                            Log.d(TAG, "Found existing chat room: ${it.id}")
                            return@withContext Result.success(it)
                        }

                    },
                    onFailure = {
                        Log.e(TAG, "Failed to get rooms", it)
                        return@withContext Result.failure(it)
                    }
                )

                // If no room exists, create new one
                Log.d(TAG, "Creating new chat room for learner: $learnerId and tutor: $tutorId")
                return@withContext chatRepository.createRoom(learnerId, tutorId)
            } catch (e: Exception) {
                Log.e(TAG, "Exception in findOrCreateChatRoom", e)
                return@withContext Result.failure(e)
            }
        }
}