package com.tutortoise.tutortoise.data.pref

import retrofit2.http.Body
import retrofit2.http.POST

data class RegisterRequest(val email: String, val password: String, val name: String)
data class LoginRequest(val email: String, val password: String)
data class AuthResponse(val token: String, val refreshToken: String?)

interface ApiService {
    @POST("register")
    suspend fun register(@Body request: RegisterRequest): AuthResponse

    @POST("login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

}