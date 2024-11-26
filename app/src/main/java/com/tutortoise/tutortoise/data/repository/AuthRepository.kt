package com.tutortoise.tutortoise.data.repository

import android.content.Context
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.tutortoise.tutortoise.data.pref.ApiConfig
import com.tutortoise.tutortoise.data.pref.ApiService
import com.tutortoise.tutortoise.data.pref.LoginRequest
import com.tutortoise.tutortoise.data.pref.RegisterRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepository(context: Context) {
    private val apiService = ApiConfig.retrofit.create(ApiService::class.java)

    private val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        "auth_prefs",
        MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    suspend fun register(name: String, email: String, password: String): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            Log.d("AuthRepository", "Registering user with email: $email")
            val response = apiService.register(RegisterRequest(name, email, password))
            saveToken(response.token)
            true
        } catch (e: Exception) {
            Log.e("AuthRepository", "Registration failed", e)
            false
        }
    }

    suspend fun login(email: String, password: String): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            val response = apiService.login(LoginRequest(email, password))
            saveToken(response.token)
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun saveToken(token: String) {
        sharedPreferences.edit().putString("auth_token", token).apply()

    }

    fun getToken(): String? {
        return sharedPreferences.getString("auth_token", null)
    }

    fun clearToken() {
        sharedPreferences.edit().clear().apply()
    }
}