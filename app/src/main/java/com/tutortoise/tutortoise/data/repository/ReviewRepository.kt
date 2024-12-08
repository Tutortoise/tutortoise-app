package com.tutortoise.tutortoise.data.repository

import android.content.Context
import android.util.Log
import com.tutortoise.tutortoise.data.model.ApiResponse
import com.tutortoise.tutortoise.data.model.MessageResponse
import com.tutortoise.tutortoise.data.model.ReviewRequest
import com.tutortoise.tutortoise.data.model.ReviewResponse
import com.tutortoise.tutortoise.data.pref.ApiConfig
import com.tutortoise.tutortoise.data.pref.ApiException


class ReviewRepository(context: Context) {
    private val apiService = ApiConfig.getApiService(context)

    suspend fun getTutoriesReviews(tutoriesId: String): ApiResponse<List<ReviewResponse>>? {
        return try {
            val response = apiService.getReviews(tutoriesId)
            if (response.isSuccessful) {
                response.body()
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("ReviewRepository", "Failed to fetch reviews", e)
            null
        }
    }

    suspend fun reviewOrder(
        orderId: String,
        rating: Float,
        message: String
    ): Result<MessageResponse> {
        return try {
            val response = apiService.reviewOrder(
                orderId,
                ReviewRequest(
                    rating = rating.toInt(),
                    message = message
                )
            )

            if (response.isSuccessful) {
                response.body()?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("Empty response body"))
            } else {
                val error = ApiConfig.parseError(response)
                Result.failure(
                    ApiException(
                        error?.message ?: "Failed to review order",
                        error
                    )
                )
            }
        } catch (e: Exception) {
            Log.e("ReviewRepository", "Failed to review order", e)
            Result.failure(e)
        }
    }

    suspend fun dismissOrderPrompt(orderId: String): MessageResponse? {
        return try {
            val response = apiService.dismissOrderPrompt(orderId)
            if (response.isSuccessful) {
                response.body()
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("ReviewRepository", "Failed to dismiss order prompt", e)
            null
        }
    }
}