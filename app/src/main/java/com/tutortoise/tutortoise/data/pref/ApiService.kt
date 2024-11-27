package com.tutortoise.tutortoise.data.pref

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT

data class ApiResponse<T>(
    val status: String,         // "error" | "fail" | "success"
    val message: String?,       // Optional message
    val data: T?                // Generic payload
)

data class MessageResponse(
    val status: String,
    val message: String
)

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    val role: String
)

data class LoginRequest(val email: String, val password: String)
data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String,
    val confirmPassword: String
)

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

data class UserResponse(
    val id: String,
    val name: String,
    val email: String,
    val role: String,
)

interface ProfileData {
    val name: String
    val email: String
    val gender: String?
    val phoneNumber: String?
    val city: String?
    val district: String?
    val createdAt: String
}

data class LearnerData(
    val id: String,
    override val name: String,
    override val email: String,
    val learningStyle: String?,
    override val gender: String?,
    override val phoneNumber: String?,
    override val city: String?,
    override val district: String?,
    override val createdAt: String
) : ProfileData

data class TutorData(
    val id: String,
    override val name: String,
    override val email: String,
    override val gender: String?,
    override val phoneNumber: String?,
    override val city: String?,
    override val district: String?,
    override val createdAt: String
) : ProfileData

data class UpdateLearnerProfileRequest(
    val name: String?,
    val phoneNumber: String?,
    val email: String?,
    val gender: String?,
    val city: String?,
    val district: String?,
    val learningStyle: String?,
    val interests: List<String>?
)

data class UpdateTutorProfileRequest(
    val name: String?,
    val phoneNumber: String?,
    val email: String?,
    val gender: String?,
    val city: String?,
    val district: String?,
)


interface ApiService {
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): ApiResponse<RegisterData>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): ApiResponse<LoginData>

    @GET("auth/me")
    suspend fun getAuthenticatedUser(
    ): Response<ApiResponse<UserResponse>>

    @GET("learners/profile")
    suspend fun getLearnerProfile(
    ): Response<ApiResponse<LearnerData>>

    @PATCH("learners/profile")
    suspend fun updateLearnerProfile(
        @Body request: UpdateLearnerProfileRequest
    ): Response<MessageResponse>

    @GET("tutors/profile")
    suspend fun getTutorProfile(
    ): Response<ApiResponse<TutorData>>

    @PATCH("tutors/profile")
    suspend fun updateTutorProfile(
        @Body request: UpdateTutorProfileRequest
    ): Response<MessageResponse>


    @PUT("/api/v1/learners/password")
    suspend fun changeLearnerPassword(@Body request: ChangePasswordRequest): Response<MessageResponse>

    @PUT("/api/v1/tutors/password")
    suspend fun changeTutorPassword(@Body request: ChangePasswordRequest): Response<MessageResponse>
}