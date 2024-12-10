package com.tutortoise.tutortoise.data.pref

import com.tutortoise.tutortoise.data.model.ApiResponse
import com.tutortoise.tutortoise.data.model.CategoryResponse
import com.tutortoise.tutortoise.data.model.ChangePasswordRequest
import com.tutortoise.tutortoise.data.model.ChatMessage
import com.tutortoise.tutortoise.data.model.ChatRoom
import com.tutortoise.tutortoise.data.model.CreateOrderResponse
import com.tutortoise.tutortoise.data.model.CreateRoomRequest
import com.tutortoise.tutortoise.data.model.CreateTutoriesRequest
import com.tutortoise.tutortoise.data.model.DetailedTutoriesResponse
import com.tutortoise.tutortoise.data.model.EditTutoriesRequest
import com.tutortoise.tutortoise.data.model.ExploreTutoriesResponse
import com.tutortoise.tutortoise.data.model.FCMTokenRequest
import com.tutortoise.tutortoise.data.model.GetMyTutoriesResponse
import com.tutortoise.tutortoise.data.model.GetTutoriesLocationResponse
import com.tutortoise.tutortoise.data.model.LearnerData
import com.tutortoise.tutortoise.data.model.LoginData
import com.tutortoise.tutortoise.data.model.LoginRequest
import com.tutortoise.tutortoise.data.model.MessageResponse
import com.tutortoise.tutortoise.data.model.NotificationResponse
import com.tutortoise.tutortoise.data.model.OAuthData
import com.tutortoise.tutortoise.data.model.OAuthRequest
import com.tutortoise.tutortoise.data.model.OrderRequest
import com.tutortoise.tutortoise.data.model.OrderResponse
import com.tutortoise.tutortoise.data.model.RegisterData
import com.tutortoise.tutortoise.data.model.RegisterRequest
import com.tutortoise.tutortoise.data.model.ReviewRequest
import com.tutortoise.tutortoise.data.model.ReviewResponse
import com.tutortoise.tutortoise.data.model.SendMessageRequest
import com.tutortoise.tutortoise.data.model.TutorData
import com.tutortoise.tutortoise.data.model.TypingStatus
import com.tutortoise.tutortoise.data.model.UpdateInterestsRequest
import com.tutortoise.tutortoise.data.model.UpdateLearnerProfileRequest
import com.tutortoise.tutortoise.data.model.UpdateLearningStyleRequest
import com.tutortoise.tutortoise.data.model.UpdateTutorProfileRequest
import com.tutortoise.tutortoise.data.model.UserResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    // Auth
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): ApiResponse<RegisterData>

    @POST("auth/google")
    suspend fun authenticateWithGoogle(@Body request: OAuthRequest): ApiResponse<OAuthData>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): ApiResponse<LoginData>

    @GET("auth/me")
    suspend fun getAuthenticatedUser(
    ): Response<ApiResponse<UserResponse>>

    // Categories
    @GET("categories")
    suspend fun getCategories(
    ): Response<ApiResponse<List<CategoryResponse>>>

    @GET("categories/popular")
    suspend fun getPopularCategories(): Response<ApiResponse<List<CategoryResponse>>>

    @GET("categories/available")
    suspend fun getAvailableCategories(): Response<ApiResponse<List<CategoryResponse>>>

    // Learners
    @GET("learners/profile")
    suspend fun getLearnerProfile(
    ): Response<ApiResponse<LearnerData>>

    @PATCH("learners/profile")
    suspend fun updateLearnerProfile(
        @Body request: UpdateLearnerProfileRequest
    ): Response<MessageResponse>

    @PATCH("learners/profile/learning-style")
    suspend fun updateLearningStyle(
        @Body request: UpdateLearningStyleRequest
    ): Response<MessageResponse>

    @PATCH("learners/profile/interests")
    suspend fun updateInterests(
        @Body request: UpdateInterestsRequest
    ): Response<MessageResponse>

    @Multipart
    @PUT("learners/profile/picture")
    suspend fun updateLearnerProfilePicture(
        @Part picture: MultipartBody.Part
    ): MessageResponse

    @PUT("learners/password")
    suspend fun changeLearnerPassword(@Body request: ChangePasswordRequest): Response<MessageResponse>

    // Tutor
    @GET("tutors/profile")
    suspend fun getTutorProfile(
    ): Response<ApiResponse<TutorData>>

    @PATCH("tutors/profile")
    suspend fun updateTutorProfile(
        @Body request: UpdateTutorProfileRequest
    ): Response<MessageResponse>

    @GET("tutors/{tutorId}/availability")
    suspend fun getTutorAvailability(
        @Path("tutorId") tutorId: String,
    ): Response<ApiResponse<List<String>>>

    @Multipart
    @PUT("tutors/profile/picture")
    suspend fun updateTutorProfilePicture(
        @Part picture: MultipartBody.Part
    ): MessageResponse

    @PUT("tutors/password")
    suspend fun changeTutorPassword(@Body request: ChangePasswordRequest): Response<MessageResponse>

    @GET("tutors/services/me")
    suspend fun getMyTutories(
    ): Response<ApiResponse<List<GetMyTutoriesResponse>>>

    @GET("tutors/services/locations")
    suspend fun getLocations(
    ): Response<ApiResponse<GetTutoriesLocationResponse>>

    @GET("tutors/services")
    suspend fun searchTutories(
        @Query("q") q: String? = null,
        @Query("categoryId[]") categoryId: List<String>? = null,
        @Query("city[]") city: List<String>? = null,
        @Query("minHourlyRate") minHourlyRate: String? = null,
        @Query("maxHourlyRate") maxHourlyRate: String? = null,
        @Query("minRating") minRating: String? = null,
        @Query("typeLesson") typeLesson: String? = null
    ): Response<ApiResponse<List<ExploreTutoriesResponse>>>

    @POST("tutors/services")
    suspend fun createTutories(
        @Body tutories: CreateTutoriesRequest
    ): MessageResponse

    @GET("tutors/services/{tutoriesId}")
    suspend fun getTutoriesById(
        @Path("tutoriesId") tutoriesId: String
    ): Response<ApiResponse<DetailedTutoriesResponse>>

    @PATCH("tutors/services/{tutoriesId}")
    suspend fun updateTutories(
        @Path("tutoriesId") tutoriesId: String,
        @Body request: EditTutoriesRequest
    ): MessageResponse

    @DELETE("tutors/services/{tutoriesId}")
    suspend fun deleteTutories(
        @Path("tutoriesId") tutoriesId: String,
    ): MessageResponse

    @GET("tutors/services/avg-rate")
    suspend fun getTutoriesAverageRate(
        @Query("categoryId") categoryId: String,
        @Query("city") city: String,
        @Query("district") district: String? = null,
    ): Response<ApiResponse<Float?>>

    // Review
    @GET("reviews/tutories/{tutoriesId}")
    suspend fun getReviews(
        @Path("tutoriesId") tutoriesId: String
    ): Response<ApiResponse<List<ReviewResponse>>>

    @POST("reviews/orders/{orderId}")
    suspend fun reviewOrder(
        @Path("orderId") orderId: String,
        @Body request: ReviewRequest
    ): Response<MessageResponse>

    @POST("reviews/orders/{orderId}/dismiss")
    suspend fun dismissOrderPrompt(
        @Path("orderId") orderId: String
    ): Response<MessageResponse>

    // Chat endpoints
    @POST("chat/rooms")
    suspend fun createRoom(
        @Body request: CreateRoomRequest
    ): Response<ApiResponse<ChatRoom>>

    @GET("chat/rooms")
    suspend fun getRooms(): Response<ApiResponse<List<ChatRoom>>>

    @GET("chat/rooms/{roomId}/messages")
    suspend fun getRoomMessages(
        @Path("roomId") roomId: String,
        @Query("before") before: String? = null,
        @Query("limit") limit: Int? = null
    ): Response<ApiResponse<List<ChatMessage>>>

    @POST("chat/rooms/{roomId}/messages/text")
    suspend fun sendTextMessage(
        @Path("roomId") roomId: String,
        @Body request: SendMessageRequest
    ): Response<ApiResponse<ChatMessage>>

    @Multipart
    @POST("chat/rooms/{roomId}/messages/image")
    suspend fun sendImageMessage(
        @Path("roomId") roomId: String,
        @Part image: MultipartBody.Part
    ): Response<ApiResponse<ChatMessage>>

    @POST("chat/rooms/{roomId}/typing")
    suspend fun updateTypingStatus(
        @Path("roomId") roomId: String,
        @Body isTyping: Boolean
    ): Response<MessageResponse>

    @GET("chat/rooms/{roomId}/presence")
    suspend fun getRoomPresence(
        @Path("roomId") roomId: String
    ): Response<ApiResponse<Map<String, TypingStatus>>>


    // Notifications
    @GET("notification")
    suspend fun getNotifications(
        @Query("limit") limit: Int? = null,
        @Query("before") before: String? = null
    ): Response<ApiResponse<List<NotificationResponse>>>

    @POST("notification/{notificationId}/read")
    suspend fun markNotificationAsRead(
        @Path("notificationId") notificationId: String
    ): Response<MessageResponse>

    @POST("notification/read-all")
    suspend fun markAllNotificationsAsRead(): Response<MessageResponse>


    // Firebase Cloud Messaging Token
    @POST("auth/fcm-token")
    suspend fun updateFCMToken(@Body request: FCMTokenRequest): Response<MessageResponse>

    @HTTP(method = "DELETE", path = "auth/fcm-token", hasBody = true)
    suspend fun removeFCMToken(@Body request: FCMTokenRequest): Response<MessageResponse>

    // Orders
    @POST("orders")
    suspend fun order(@Body order: OrderRequest): Response<ApiResponse<CreateOrderResponse>>

    @GET("orders/me")
    suspend fun getMyOrders(@Query("status") status: String? = null): Response<ApiResponse<List<OrderResponse>>>

    @POST("orders/{orderId}/accept")
    suspend fun acceptOrder(@Path("orderId") orderId: String): Response<MessageResponse>

    @POST("orders/{orderId}/decline")
    suspend fun rejectOrder(@Path("orderId") orderId: String): Response<MessageResponse>

    @POST("orders/{orderId}/cancel")
    suspend fun cancelOrder(@Path("orderId") orderId: String): Response<MessageResponse>

    @GET("orders/unreviewed")
    suspend fun getUnreviewedOrders(): Response<ApiResponse<List<OrderResponse>>>
}