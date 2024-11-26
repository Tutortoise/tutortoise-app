package com.tutortoise.tutortoise.data.repository

import android.content.Context
import android.util.Log
import com.tutortoise.tutortoise.data.pref.ApiConfig
import com.tutortoise.tutortoise.data.pref.ApiResponse
import com.tutortoise.tutortoise.data.pref.LearnerData

class LearnerRepository(private val context: Context) {
    private val apiService = ApiConfig.getApiService(context)

    suspend fun fetchLearnerProfile(): ApiResponse<LearnerData>? {
        return try {
            val response = apiService.getLearnerProfile()
            if (response.isSuccessful) {
                response.body()
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("LearnerRepository", "Failed to fetch learner profile", e)
            null
        }
    }

}