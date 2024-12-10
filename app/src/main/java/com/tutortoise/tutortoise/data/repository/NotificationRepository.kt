package com.tutortoise.tutortoise.data.repository

import android.content.Context
import android.util.Log
import com.tutortoise.tutortoise.data.model.MessageResponse
import com.tutortoise.tutortoise.data.model.NotificationResponse
import com.tutortoise.tutortoise.data.pref.ApiConfig
import com.tutortoise.tutortoise.data.pref.ApiException

class NotificationRepository(context: Context) {
    private val apiService = ApiConfig.getApiService(context)

    suspend fun getNotifications(): Result<List<NotificationResponse>> {
        return try {
            val response = apiService.getNotifications()
            if (response.isSuccessful) {
                response.body()?.data?.let {
                    // Filter out chat notifications
                    Result.success(it.filter { notification -> notification.type != "chat_message" })
                } ?: Result.failure(Exception("Empty response body"))
            } else {
                val error = ApiConfig.parseError(response)
                Result.failure(ApiException(error?.message ?: "Failed to get notifications", error))
            }
        } catch (e: Exception) {
            Log.e("NotificationRepository", "Failed to get notifications", e)
            Result.failure(e)
        }
    }

    suspend fun markAsRead(notificationId: String): Result<MessageResponse> {
        return try {
            val response = apiService.markNotificationAsRead(notificationId)
            if (response.isSuccessful) {
                response.body()?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("Empty response body"))
            } else {
                val error = ApiConfig.parseError(response)
                Result.failure(
                    ApiException(
                        error?.message ?: "Failed to mark notification as read",
                        error
                    )
                )
            }
        } catch (e: Exception) {
            Log.e("NotificationRepository", "Failed to mark notification as read", e)
            Result.failure(e)
        }
    }

    suspend fun markAllAsRead(): Result<MessageResponse> {
        return try {
            val response = apiService.markAllNotificationsAsRead()
            if (response.isSuccessful) {
                response.body()?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("Empty response body"))
            } else {
                val error = ApiConfig.parseError(response)
                Result.failure(
                    ApiException(
                        error?.message ?: "Failed to mark all notifications as read", error
                    )
                )
            }
        } catch (e: Exception) {
            Log.e("NotificationRepository", "Failed to mark all notifications as read", e)
            Result.failure(e)
        }
    }
}