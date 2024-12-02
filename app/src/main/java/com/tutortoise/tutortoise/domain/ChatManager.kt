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

    fun navigateToChat(context: Context, tutorId: String) {
        scope.launch {
            try {
                Log.d(TAG, "Starting navigation to chat with tutor: $tutorId")
                val learnerId = AuthManager.getCurrentUserId() ?: run {
                    Toast.makeText(context, "User not authenticated", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                // Just navigate to ChatRoomActivity with necessary IDs
                val intent = Intent(context, ChatRoomActivity::class.java).apply {
                    putExtra("LEARNER_ID", learnerId)
                    putExtra("TUTOR_ID", tutorId)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)

            } catch (e: Exception) {
                Log.e(TAG, "Exception in navigateToChat", e)
                Toast.makeText(context, "Failed to start chat", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // This function will be called only when sending the first message
    suspend fun findOrCreateChatRoom(context: Context, tutorId: String): Result<ChatRoom> =
        withContext(Dispatchers.IO) {
            try {
                val chatRepository = ChatRepository(context)
                val learnerId = AuthManager.getCurrentUserId()
                if (learnerId == null) {
                    Log.e(TAG, "User not authenticated")
                    return@withContext Result.failure(Exception("User not authenticated"))
                }

                // First try to find existing room
                val roomsResult = chatRepository.getRooms()
                roomsResult.fold(
                    onSuccess = { rooms ->
                        Log.d(TAG, "Found ${rooms.size} chat rooms")
                        rooms.find { it.tutorId == tutorId }?.let {
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