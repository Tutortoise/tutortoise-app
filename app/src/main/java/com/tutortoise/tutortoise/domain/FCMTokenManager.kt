package com.tutortoise.tutortoise.domain

import android.content.Context
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import com.tutortoise.tutortoise.data.repository.AuthRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await

object FCMTokenManager {
    private const val TAG = "FCMTokenManager"
    private const val MAX_RETRIES = 3
    private const val RETRY_DELAY_MS = 1000L

    suspend fun initialize(context: Context) {
        try {
            val token = FirebaseMessaging.getInstance().token.await()
            Log.d(TAG, "FCM Token: $token")
            updateToken(context, token)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize FCM", e)
        }
    }

    suspend fun updateToken(context: Context, token: String) {
        repeat(MAX_RETRIES) { attempt ->
            try {
                val authRepository = AuthRepository(context)
                val result = authRepository.updateFCMToken(token)
                result.fold(
                    onSuccess = {
                        Log.d(TAG, "FCM token updated successfully")
                        return
                    },
                    onFailure = { error ->
                        if (attempt == MAX_RETRIES - 1) throw error
                        Log.w(TAG, "Retry ${attempt + 1} failed: ${error.message}")
                        delay(RETRY_DELAY_MS * (attempt + 1))
                    }
                )
            } catch (e: Exception) {
                if (attempt == MAX_RETRIES - 1) throw e
                Log.w(TAG, "Retry ${attempt + 1} failed: ${e.message}")
                delay(RETRY_DELAY_MS * (attempt + 1))
            }
        }
    }

    suspend fun removeToken(context: Context) {
        try {
            val token = FirebaseMessaging.getInstance().token.await()
            val authRepository = AuthRepository(context)
            val result = authRepository.removeFCMToken(token)

            result.fold(
                onSuccess = {
                    Log.d(TAG, "FCM token removed successfully")
                },
                onFailure = { error ->
                    Log.e(TAG, "Failed to remove FCM token", error)
                    throw error
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to remove FCM token", e)
            throw e
        }
    }
}