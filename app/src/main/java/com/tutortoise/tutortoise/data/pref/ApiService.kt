package com.tutortoise.tutortoise.data.pref

import retrofit2.http.Body
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

interface ApiService {
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): AuthResponse

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse
}