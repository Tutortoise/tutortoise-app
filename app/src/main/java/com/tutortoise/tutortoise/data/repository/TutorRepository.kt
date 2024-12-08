package com.tutortoise.tutortoise.data.repository

import android.content.Context
import android.util.Log
import com.tutortoise.tutortoise.data.model.ApiResponse
import com.tutortoise.tutortoise.data.model.MessageResponse
import com.tutortoise.tutortoise.data.model.TutorData
import com.tutortoise.tutortoise.data.model.UpdateTutorProfileRequest
import com.tutortoise.tutortoise.data.pref.ApiConfig
import com.tutortoise.tutortoise.data.pref.ApiException
import okhttp3.MultipartBody
import retrofit2.HttpException


class TutorRepository(context: Context) {
    private val apiService = ApiConfig.getApiService(context)

    // Tutor need to fill their profile before creating tutories
    suspend fun isProfileFilled(): Boolean {
        return try {
            val response = apiService.getTutorProfile()
            val tutorData = response.body()
            tutorData?.data.let {
                return it?.name != null && it.gender != null && it.city != null && it.district != null
            }
        } catch (e: Exception) {
            Log.e("TutorRepository", "Failed to check if profile is filled", e)
            false
        }
    }

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

    suspend fun fetchAvailability(tutorId: String): ApiResponse<List<String>>? {
        return try {
            val response = apiService.getTutorAvailability(tutorId)
            if (response.isSuccessful) {
                response.body()
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("TutoriesRepository", "Failed to fetch tutories availability", e)
            null
        }
    }
}