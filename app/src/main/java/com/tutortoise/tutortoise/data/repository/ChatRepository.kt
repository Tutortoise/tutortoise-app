package com.tutortoise.tutortoise.data.repository

import android.content.Context
import com.tutortoise.tutortoise.data.model.ChatMessage
import com.tutortoise.tutortoise.data.model.ChatRoom
import com.tutortoise.tutortoise.data.model.CreateRoomRequest
import com.tutortoise.tutortoise.data.model.SendMessageRequest
import com.tutortoise.tutortoise.data.pref.ApiConfig
import com.tutortoise.tutortoise.data.pref.ApiException
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class ChatRepository(private val context: Context) {
    private val api = ApiConfig.getApiService(context)

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

            if (response.isSuccessful) {
                response.body()?.data?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("Empty response body"))
            } else {
                val error = ApiConfig.parseError(response)
                Result.failure(ApiException(error?.message ?: "Failed to send message", error))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}