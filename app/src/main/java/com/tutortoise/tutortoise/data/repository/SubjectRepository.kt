package com.tutortoise.tutortoise.data.repository

import android.content.Context
import android.util.Log
import com.tutortoise.tutortoise.data.model.ApiResponse
import com.tutortoise.tutortoise.data.model.SubjectResponse
import com.tutortoise.tutortoise.data.pref.ApiConfig

class SubjectRepository(context: Context) {
    private val apiService = ApiConfig.getApiService(context)

    suspend fun fetchSubjects(): ApiResponse<List<SubjectResponse>>? {
        return try {
            val response = apiService.getSubjects()
            if (response.isSuccessful) {
                response.body()
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("SubjectRepository", "Failed to fetch subjects", e)
            null
        }
    }

    suspend fun fetchPopularSubjects(): ApiResponse<List<SubjectResponse>>? {
        return try {
            val response = apiService.getPopularSubjects()
            if (response.isSuccessful) {
                response.body()
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("SubjectRepository", "Failed to fetch popular subjects", e)
            null
        }
    }

    suspend fun fetchAvailableSubjects(): ApiResponse<List<SubjectResponse>>? {
        return try {
            val response = apiService.getAvailableSubjects()
            if (response.isSuccessful) {
                response.body()
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("SubjectRepository", "Failed to fetch available subjects", e)
            null
        }
    }
}