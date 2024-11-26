package com.tutortoise.tutortoise.data.pref

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

data class ApiResponse<T>(
    val status: String,         // "error" | "fail" | "success"
    val message: String?,       // Optional message
    val data: T?                // Generic payload
)

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    val role: String
)

data class LoginRequest(val email: String, val password: String)

data class RegisterData(
    val userId: String
)

data class LoginData(
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

data class UserResponse (
    val id: String,
    val name: String,
    val email: String,
    val role: String,
)

data class LearnerData(
    val id: String,
    val name: String,
    val email: String,
    val learningStyle: String,
    val gender: String,
    val phoneNumber: String,
    val city: String,
    val district: String,
    val createdAt: String
)

data class TutorData(
    val id: String,
    val name: String,
    val email: String,
    val gender: String,
    val phoneNumber: String,
    val city: String,
    val district: String,
    val createdAt: String
)


interface ApiService {
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): ApiResponse<RegisterData>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): ApiResponse<LoginData>

    @GET("auth/me")
    suspend fun getAuthenticatedUser(
    ) : Response<ApiResponse<UserResponse>>

    @GET("learners/profile")
    suspend fun getLearnerProfile(
    ): Response<ApiResponse<LearnerData>>

    @GET("tutors/profile")
    suspend fun getTutorProfile(
    ): Response<ApiResponse<TutorData>>
}