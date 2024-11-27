package com.tutortoise.tutortoise.data.repository

import android.content.Context
import android.util.Log
import com.tutortoise.tutortoise.data.pref.ApiConfig
import com.tutortoise.tutortoise.data.pref.ApiResponse
import com.tutortoise.tutortoise.data.pref.LearnerData
import com.tutortoise.tutortoise.data.pref.MessageResponse
import com.tutortoise.tutortoise.data.pref.UpdateLearnerProfileRequest

class LearnerRepository(context: Context) {
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

    suspend fun updateLearnerProfile(data: UpdateLearnerProfileRequest): MessageResponse? {
        return try {
            val response = apiService.updateLearnerProfile(data)
            if (response.isSuccessful) {
                response.body()
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("LearnerRepository", "Failed to update learner profile", e)
            null
        }
    }

}