package com.tutortoise.tutortoise.data.pref

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    val role: String
)

data class LoginRequest(val email: String, val password: String)
data class AuthResponse(
    val status: String,
    val message: String,
    val data: AuthData
)

data class AuthData(
    val userId: String,
    val token: String
)

data class ErrorResponse(
    val status: String,
    val message: String,
    val errors: List<ValidationError>?
)

data class ValidationError(
    val field: String,
    val message: String
)

data class UserResponse(
    val id: String,
    val name: String,
    val email: String,
    val role: String
)

interface ApiService {
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): AuthResponse

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    @GET("auth/user")
    suspend fun getUserDetail(
        @Header("Authorization") token: String
    ) : Response<UserResponse>
}