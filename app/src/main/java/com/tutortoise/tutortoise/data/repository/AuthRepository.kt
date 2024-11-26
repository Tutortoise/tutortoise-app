package com.tutortoise.tutortoise.data.repository

import android.content.Context
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.tutortoise.tutortoise.data.pref.ApiConfig
import com.tutortoise.tutortoise.data.pref.AuthResponse
import com.tutortoise.tutortoise.data.pref.ErrorResponse
import com.tutortoise.tutortoise.data.pref.LoginRequest
import com.tutortoise.tutortoise.data.pref.RegisterRequest
import com.tutortoise.tutortoise.data.pref.UserResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException

class AuthRepository(private val context: Context) {
    private val apiService = ApiConfig.getApiService(context)

    private val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        "auth_prefs",
        MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    suspend fun register(name: String, email: String, password: String, role: String): Result<AuthResponse> = withContext(Dispatchers.IO) {
        return@withContext try {
            Log.d("AuthRepository", "Registering user with email: $email as $role")
            val response = apiService.register(RegisterRequest(
                name = name,
                email = email,
                password = password,
                role = role
            ))
            Result.success(response)
        } catch (e: HttpException) {
            val errorBody = ApiConfig.parseError(e.response()!!)
            Result.failure(ApiException(errorBody?.message ?: "Registration failed", errorBody))
        } catch (e: Exception) {
            Log.e("AuthRepository", "Registration failed", e)
            Result.failure(e)
        }
    }

    suspend fun login(email: String, password: String): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            val response = apiService.login(LoginRequest(email, password))
            if (response.status == "success") {
                val token = response.data.token
                Log.d("AuthRepository", "Received token: $token")
                if (token != null) {
                    saveToken(token)
                    val userDetails = fetchUserDetails(token)
                    userDetails?.let {
                        saveUserInfo(it.name, it.email)
                    }
                    true
                } else {
                    Log.d("AuthRepository", "Token is null in login response.")
                    false
                }
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Login failed", e)
            false
        }
    }

    private fun saveToken(token: String) {
        Log.d("AuthRepository", "Saving token: $token")
        sharedPreferences.edit().putString("auth_token", token).apply()
    }

    private fun saveUserInfo(name: String, email: String) {
        sharedPreferences.edit()
            .putString("user_name", name)
            .putString("user_email", email)
            .apply()
    }

    fun getToken(): String? {
        return sharedPreferences.getString("auth_token", null)
    }

    fun clearToken() {
        sharedPreferences.edit().clear().apply()
    }

    suspend fun fetchUserDetails(token: String): UserResponse? {
        return try {
            val response = apiService.getUserDetail("Bearer $token")
            if (response.isSuccessful) {
                response.body()
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Failed to fetch user details", e)
            null
        }
    }
}


class ApiException(
    override val message: String,
    val errorResponse: ErrorResponse?
) : Exception(message)