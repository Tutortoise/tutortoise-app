package com.tutortoise.tutortoise.data.pref

import com.tutortoise.tutortoise.data.model.ApiResponse
import com.tutortoise.tutortoise.data.model.ChangePasswordRequest
import com.tutortoise.tutortoise.data.model.LearnerData
import com.tutortoise.tutortoise.data.model.LoginData
import com.tutortoise.tutortoise.data.model.LoginRequest
import com.tutortoise.tutortoise.data.model.MessageResponse
import com.tutortoise.tutortoise.data.model.RegisterData
import com.tutortoise.tutortoise.data.model.RegisterRequest
import com.tutortoise.tutortoise.data.model.SubjectResponse
import com.tutortoise.tutortoise.data.model.TutorData
import com.tutortoise.tutortoise.data.model.TutoriesServiceModel
import com.tutortoise.tutortoise.data.model.UpdateLearnerProfileRequest
import com.tutortoise.tutortoise.data.model.UpdateTutorProfileRequest
import com.tutortoise.tutortoise.data.model.UserResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part

interface ApiService {
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): ApiResponse<RegisterData>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): ApiResponse<LoginData>

    @POST("tutors/services")
    suspend fun createTutories(
        @Body tutories: TutoriesServiceModel
    ): Response<ApiResponse<TutoriesServiceModel>>

    @GET("auth/me")
    suspend fun getAuthenticatedUser(
    ): Response<ApiResponse<UserResponse>>

    @GET("subjects")
    suspend fun getSubjects(
    ): Response<ApiResponse<List<SubjectResponse>>>

    @GET("subjects/popular")
    suspend fun getPopularSubjects(): Response<ApiResponse<List<SubjectResponse>>>

    @GET("tutors/services/me")
    suspend fun getMyTutories(
    ): Response<ApiResponse<List<TutoriesServiceModel>>>

    @GET("learners/profile")
    suspend fun getLearnerProfile(
    ): Response<ApiResponse<LearnerData>>

    @PATCH("learners/profile")
    suspend fun updateLearnerProfile(
        @Body request: UpdateLearnerProfileRequest
    ): Response<MessageResponse>

    @Multipart
    @PUT("learners/profile/picture")
    suspend fun updateLearnerProfilePicture(
        @Part picture: MultipartBody.Part
    ): MessageResponse

    @GET("tutors/profile")
    suspend fun getTutorProfile(
    ): Response<ApiResponse<TutorData>>

    @PATCH("tutors/profile")
    suspend fun updateTutorProfile(
        @Body request: UpdateTutorProfileRequest
    ): Response<MessageResponse>

    @Multipart
    @PUT("tutors/profile/picture")
    suspend fun updateTutorProfilePicture(
        @Part picture: MultipartBody.Part
    ): MessageResponse

    @PUT("/api/v1/learners/password")
    suspend fun changeLearnerPassword(@Body request: ChangePasswordRequest): Response<MessageResponse>

    @PUT("/api/v1/tutors/password")
    suspend fun changeTutorPassword(@Body request: ChangePasswordRequest): Response<MessageResponse>
}