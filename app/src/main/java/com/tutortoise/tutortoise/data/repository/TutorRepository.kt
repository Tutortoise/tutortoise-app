package com.tutortoise.tutortoise.data.repository

import android.content.Context
import android.util.Log
import com.tutortoise.tutortoise.data.pref.ApiConfig
import com.tutortoise.tutortoise.data.pref.ApiResponse
import com.tutortoise.tutortoise.data.pref.MessageResponse
import com.tutortoise.tutortoise.data.pref.TutorData
import com.tutortoise.tutortoise.data.pref.UpdateTutorProfileRequest

class TutorRepository(private val context: Context) {
    private val apiService = ApiConfig.getApiService(context)

    suspend fun fetchTutorProfile(): ApiResponse<TutorData>? {
        return try {
            val response = apiService.getTutorProfile()
            if (response.isSuccessful) {
                response.body()
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("TutorRepository", "Failed to fetch tutor profile", e)
            null
        }
    }

    suspend fun updateTutorProfile(data: UpdateTutorProfileRequest): MessageResponse? {
        return try {
            val response = apiService.updateTutorProfile(data)
            if (response.isSuccessful) {
                response.body()
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("TutorRepository", "Failed to update tutor profile", e)
            null
        }
    }

}