package com.tutortoise.tutortoise.data.repository

import android.content.Context
import android.util.Log
import com.tutortoise.tutortoise.data.pref.ApiConfig
import com.tutortoise.tutortoise.data.pref.ApiResponse
import com.tutortoise.tutortoise.data.pref.SubjectResponse


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


}