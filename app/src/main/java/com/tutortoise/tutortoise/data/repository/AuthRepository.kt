package com.tutortoise.tutortoise.data.repository

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.tutortoise.tutortoise.data.model.ApiResponse
import com.tutortoise.tutortoise.data.model.ChangePasswordRequest
import com.tutortoise.tutortoise.data.model.LoginRequest
import com.tutortoise.tutortoise.data.model.MessageResponse
import com.tutortoise.tutortoise.data.model.OAuthData
import com.tutortoise.tutortoise.data.model.OAuthRequest
import com.tutortoise.tutortoise.data.model.RegisterData
import com.tutortoise.tutortoise.data.model.RegisterRequest
import com.tutortoise.tutortoise.data.model.UserResponse
import com.tutortoise.tutortoise.data.pref.ApiConfig
import com.tutortoise.tutortoise.data.pref.ApiException
import com.tutortoise.tutortoise.data.pref.OAuthConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import retrofit2.HttpException

class CustomOAuthException(message: String) : Exception(message)

class AuthRepository(private val context: Context) {
    private val apiService = ApiConfig.getApiService(context)
    private val oauthConfig = OAuthConfig()

    private val googleSignInClient: GoogleSignInClient by lazy {
        GoogleSignIn.getClient(context, oauthConfig.getGoogleSignInOptions())
    }

    private val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        "auth_prefs",
        MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun getSharedPreferences() = sharedPreferences

    suspend fun register(
        name: String,
        email: String,
        password: String,
        role: String
    ): Result<ApiResponse<RegisterData>> = withContext(Dispatchers.IO) {
        return@withContext try {
            Log.d("AuthRepository", "Registering user with email: $email as $role")
            val response = apiService.register(
                RegisterRequest(
                    name = name,
                    email = email,
                    password = password,
                    role = role
                )
            )
            Result.success(response)
        } catch (e: HttpException) {
            val errorBody = ApiConfig.parseError(e.response()!!)
            Result.failure(ApiException(errorBody?.message ?: "Registration failed", errorBody))
        } catch (e: Exception) {
            Log.e("AuthRepository", "Registration failed", e)
            Result.failure(e)
        }
    }

    suspend fun authenticateWithGoogle(role: String?): Result<ApiResponse<OAuthData>> {
        return try {
            val credentialManager = CredentialManager.create(context)

            try {
                // Try modern Credential Manager first
                val result = credentialManager.getCredential(
                    request = oauthConfig.getCredentialRequest(),
                    context = context
                )

                val googleIdTokenCredential =
                    GoogleIdTokenCredential.createFrom(result.credential.data)
                handleGoogleAuthentication(googleIdTokenCredential.idToken, role)

            } catch (e: GetCredentialCancellationException) {
                // User canceled the flow
                Result.failure(e)
            } catch (e: NoCredentialException) {
                // Fall back to traditional Google Sign-In
                handleTraditionalGoogleSignIn(role)
            }
        } catch (e: HttpException) {
            val errorBody = ApiConfig.parseError(e.response()!!)
            Result.failure(ApiException(errorBody?.message ?: "Google sign-in failed", errorBody))
        } catch (e: CustomOAuthException) {
            Result.failure(e)
        } catch (e: GetCredentialCancellationException) {
            Result.failure(e)
        } catch (e: Exception) {
            Log.e("AuthRepository", "Google sign-in failed", e)
            Result.failure(e)
        }
    }

    private suspend fun handleTraditionalGoogleSignIn(role: String?): Result<ApiResponse<OAuthData>> {
        return try {
            // Sign out first to show account picker
            googleSignInClient.signOut().await()

            // Let the Activity handle the sign in process and return the result
            throw CustomOAuthException("LAUNCH_GOOGLE_SIGNIN")

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun handleGoogleAuthentication(
        idToken: String,
        role: String?
    ): Result<ApiResponse<OAuthData>> {
        return try {
            val response = apiService.authenticateWithGoogle(
                OAuthRequest(
                    idToken = idToken,
                    role = role
                )
            )

            response.data?.let { data ->
                role?.takeIf { it != data.user.role }?.let {
                    return Result.failure(CustomOAuthException("You are already registered as ${data.user.role}"))
                }

                saveToken(data.token)
                fetchUserDetails()?.data?.let { user ->
                    saveUserInfo(user.id, user.name, user.email, user.role)
                }
                Result.success(response)
            } ?: Result.failure(Exception("Google sign-in failed: ${response.message}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(email: String, password: String): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            val response = apiService.login(LoginRequest(email, password))
            if (response.status != "success") {
                Log.d("AuthRepository", "Login failed ${response.message}")
                return@withContext false
            }

            val token = response.data!!.token
            Log.d("AuthRepository", "Received token: $token")

            saveToken(token)

            val userDetails = fetchUserDetails()
            userDetails?.data?.let { data ->
                saveUserInfo(data.id, data.name, data.email, data.role)
            }

            true
        } catch (e: Exception) {
            Log.e("AuthRepository", "Login failed", e)
            false
        }
    }

    suspend fun changePassword(
        currentPassword: String,
        newPassword: String,
        confirmPassword: String
    ): Result<MessageResponse> {
        return try {
            val request = ChangePasswordRequest(
                currentPassword = currentPassword,
                newPassword = newPassword,
                confirmPassword = confirmPassword
            )

            val response = when (getUserRole()) {
                "learner" -> apiService.changeLearnerPassword(request)
                else -> apiService.changeTutorPassword(request)
            }

            if (response.isSuccessful) {
                response.body()?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("Empty response body"))
            } else {
                val error = ApiConfig.parseError(response)
                Result.failure(
                    ApiException(
                        error?.message ?: "Failed to change password",
                        error
                    )
                )
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Failed to change password", e)
            Result.failure(e)
        }
    }

    private fun saveToken(token: String) {
        Log.d("AuthRepository", "Saving token: $token")
        sharedPreferences.edit().putString("auth_token", token).apply()
    }

    private fun saveUserInfo(id: String, name: String, email: String, role: String) {
        sharedPreferences.edit()
            .putString("user_id", id)
            .putString("user_name", name)
            .putString("user_email", email)
            .putString("user_role", role)
            .apply()
    }

    fun getToken(): String? {
        return sharedPreferences.getString("auth_token", null)
    }

    fun getGoogleSignInOptions(): GoogleSignInOptions {
        return oauthConfig.getGoogleSignInOptions()
    }

    fun clearToken() {
        sharedPreferences.edit().clear().apply()
    }

    private suspend fun fetchUserDetails(): ApiResponse<UserResponse>? {
        return try {
            val response = apiService.getAuthenticatedUser()
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

    fun getUserRole(): String? {
        return sharedPreferences.getString("user_role", null)
    }

    fun getUserId(): String? {
        return sharedPreferences.getString("user_id", null)?.also {
            if (it.isEmpty()) return null
        }
    }
}