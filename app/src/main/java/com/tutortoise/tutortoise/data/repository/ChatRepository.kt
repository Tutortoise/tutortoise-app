package com.tutortoise.tutortoise.data.repository

import android.content.Context
import com.google.firebase.Firebase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.database
import com.tutortoise.tutortoise.data.model.ChatMessage
import com.tutortoise.tutortoise.data.model.ChatRoom
import com.tutortoise.tutortoise.data.model.CreateRoomRequest
import com.tutortoise.tutortoise.data.model.FirebaseMessage
import com.tutortoise.tutortoise.data.model.SendMessageRequest
import com.tutortoise.tutortoise.data.pref.ApiConfig
import com.tutortoise.tutortoise.data.pref.ApiException
import kotlinx.coroutines.tasks.await
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class ChatRepository(private val context: Context) {
    private val api = ApiConfig.getApiService(context)
    private val database = Firebase.database.reference

    suspend fun createRoom(learnerId: String, tutorId: String): Result<ChatRoom> {
        return try {
            val response = api.createRoom(CreateRoomRequest(learnerId, tutorId))
            if (response.isSuccessful) {
                response.body()?.data?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("Empty response body"))
            } else {
                val error = ApiConfig.parseError(response)
                Result.failure(ApiException(error?.message ?: "Failed to create room", error))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getRooms(): Result<List<ChatRoom>> {
        return try {
            val response = api.getRooms()
            if (response.isSuccessful) {
                response.body()?.data?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("Empty response body"))
            } else {
                val error = ApiConfig.parseError(response)
                Result.failure(ApiException(error?.message ?: "Failed to get rooms", error))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getRoomMessages(
        roomId: String,
        before: String? = null
    ): Result<List<ChatMessage>> {
        return try {
            val response = api.getRoomMessages(
                roomId,
                before = before,
                limit = 20
            )
            if (response.isSuccessful) {
                response.body()?.data?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("Empty response body"))
            } else {
                val error = ApiConfig.parseError(response)
                Result.failure(ApiException(error?.message ?: "Failed to get messages", error))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendMessage(roomId: String, content: String, type: String): Result<ChatMessage> {
        return try {
            val response = when (type) {
                "text" -> api.sendTextMessage(roomId, SendMessageRequest(content))
                "image" -> {
                    val imageBytes =
                        android.util.Base64.decode(content, android.util.Base64.DEFAULT)
                    val mediaType = "image/jpeg".toMediaTypeOrNull()
                    val requestBody = imageBytes.toRequestBody(mediaType)
                    val part = MultipartBody.Part.createFormData(
                        "image",
                        "image.jpg",
                        requestBody
                    )
                    api.sendImageMessage(roomId, part)
                }

                else -> throw IllegalArgumentException("Unsupported message type")
            }

            if (response.isSuccessful && response.body()?.data != null) {
                val message = response.body()?.data!!

                // Then store in Firebase
                val firebaseMessage = FirebaseMessage(
                    id = message.id,
                    roomId = message.roomId,
                    senderId = message.senderId,
                    senderRole = message.senderRole,
                    content = message.content,
                    type = message.type,
                    sentAt = System.currentTimeMillis(),
                    isRead = message.isRead
                )

                // Write to Firebase
                database.child("messages")
                    .child(roomId)
                    .child(message.id)
                    .setValue(firebaseMessage)
                    .await()

                // Update last message
                database.child("rooms")
                    .child(roomId)
                    .child("lastMessage")
                    .setValue(
                        mapOf(
                            "content" to message.content,
                            "type" to message.type,
                            "timestamp" to ServerValue.TIMESTAMP
                        )
                    )
                    .await()

                Result.success(message)
            } else {
                val error = ApiConfig.parseError(response)
                Result.failure(ApiException(error?.message ?: "Failed to send message", error))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}