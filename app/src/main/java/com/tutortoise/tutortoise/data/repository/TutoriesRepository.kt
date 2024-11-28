package com.tutortoise.tutortoise.data.repository

import android.content.Context
import android.util.Log
import com.tutortoise.tutortoise.data.model.ApiResponse
import com.tutortoise.tutortoise.data.model.CreateTutoriesRequest
import com.tutortoise.tutortoise.data.model.TutoriesServiceModel
import com.tutortoise.tutortoise.data.pref.ApiConfig

class TutoriesRepository(context: Context) {
    private val apiService = ApiConfig.getApiService(context)

    suspend fun getMyTutories(): ApiResponse<List<TutoriesServiceModel>>? {
        return try {
            val response = apiService.getMyTutories()
            if (response.isSuccessful) {
                response.body()
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("TutoriesRepository", "Failed to fetch tutories", e)
            null
        }
    }

    suspend fun createTutories(request: CreateTutoriesRequest): Result<TutoriesServiceModel> {
        return try {
            val response = apiService.createTutories(
                TutoriesServiceModel(
                    id = "",
                    subject = request.subject,
                    about = request.about,
                    methodology = request.methodology,
                    ratePerHour = request.ratePerHour,
                    isOnline = request.isOnline,
                    isFaceToFace = request.isFaceToFace
                )
            )

            if (response.isSuccessful && response.body()?.status == "success") {
                Result.success(response.body()?.data!!)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Failed to create tutories"))
            }
        } catch (e: Exception) {
            Log.e("TutoriesRepository", "Failed to create tutories", e)
            Result.failure(e)
        }
    }
}