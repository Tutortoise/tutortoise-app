package com.tutortoise.tutortoise.data.repository

import android.content.Context
import android.util.Log
import com.tutortoise.tutortoise.data.pref.ApiConfig
import com.tutortoise.tutortoise.data.pref.ApiResponse
import com.tutortoise.tutortoise.data.pref.LearnerData
import com.tutortoise.tutortoise.data.pref.MessageResponse
import com.tutortoise.tutortoise.data.pref.UpdateLearnerProfileRequest
import okhttp3.MultipartBody
import retrofit2.HttpException

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

    suspend fun updateProfilePicture(picture: MultipartBody.Part): Result<MessageResponse> {
        return try {
            val response =
                apiService.updateLearnerProfilePicture(picture)

            Result.success(response)
        } catch (e: HttpException) {
            val errorBody = ApiConfig.parseError(e.response()!!)
            Result.failure(
                ApiException(
                    errorBody?.message ?: "Updating profile picture failed",
                    errorBody
                )
            )
        } catch (e: Exception) {
            Log.e("LearnerRepository", "Updating profile picture failed", e)
            Result.failure(e)
        }
    }
}