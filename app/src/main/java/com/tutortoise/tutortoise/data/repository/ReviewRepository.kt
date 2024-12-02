package com.tutortoise.tutortoise.data.repository

import android.content.Context
import android.util.Log
import com.tutortoise.tutortoise.data.model.ApiResponse
import com.tutortoise.tutortoise.data.model.ReviewResponse
import com.tutortoise.tutortoise.data.pref.ApiConfig


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
}