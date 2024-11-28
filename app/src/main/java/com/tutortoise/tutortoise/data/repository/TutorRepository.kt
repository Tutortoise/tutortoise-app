package com.tutortoise.tutortoise.data.repository

import android.content.Context
import android.util.Log
import com.tutortoise.tutortoise.data.pref.ApiConfig
import com.tutortoise.tutortoise.data.pref.ApiResponse
import com.tutortoise.tutortoise.data.pref.MessageResponse
import com.tutortoise.tutortoise.data.pref.TutorData
import com.tutortoise.tutortoise.data.pref.UpdateTutorProfileRequest
import okhttp3.MultipartBody
import retrofit2.HttpException

class TutorRepository(context: Context) {
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

    suspend fun updateProfilePicture(picture: MultipartBody.Part): Result<MessageResponse> {
        return try {
            val response =
                apiService.updateTutorProfilePicture(picture)

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
            Log.e("TutorRepository", "Updating profile picture failed", e)
            Result.failure(e)
        }
    }
}