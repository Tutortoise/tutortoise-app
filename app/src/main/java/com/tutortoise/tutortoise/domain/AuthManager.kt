package com.tutortoise.tutortoise.domain

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.tutortoise.tutortoise.data.repository.AuthRepository

object AuthManager {
    @SuppressLint("StaticFieldLeak")
    private var instance: AuthRepository? = null

    fun initialize(context: Context) {
        if (instance == null) {
            instance = AuthRepository(context)
        }
    }

    fun getCurrentUserId(): String? {
        return instance?.getUserId()
    }

    fun getInstance(): AuthRepository? {
        return instance
    }

    suspend fun logout(context: Context) {
        try {
            FCMTokenManager.removeToken(context)
            instance?.clearToken()
        } catch (e: Exception) {
            Log.e("AuthManager", "Error during logout", e)
            throw e
        }
    }
}