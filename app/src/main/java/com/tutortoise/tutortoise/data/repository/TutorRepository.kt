package com.tutortoise.tutortoise.data.repository

import android.content.Context
import android.util.Log
import com.tutortoise.tutortoise.data.pref.ApiConfig
import com.tutortoise.tutortoise.data.pref.ApiResponse
import com.tutortoise.tutortoise.data.pref.TutorData

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

}