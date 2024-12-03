package com.tutortoise.tutortoise.data.repository

import android.content.Context
import android.util.Log
import com.tutortoise.tutortoise.data.model.ApiResponse
import com.tutortoise.tutortoise.data.model.LearnerData
import com.tutortoise.tutortoise.data.model.MessageResponse
import com.tutortoise.tutortoise.data.model.UpdateInterestsRequest
import com.tutortoise.tutortoise.data.model.UpdateLearnerProfileRequest
import com.tutortoise.tutortoise.data.model.UpdateLearningStyleRequest
import com.tutortoise.tutortoise.data.pref.ApiConfig
import com.tutortoise.tutortoise.data.pref.ApiException
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

    suspend fun updateLearnerProfile(data: UpdateLearnerProfileRequest): Result<MessageResponse> {
        return try {
            val response = apiService.updateLearnerProfile(data)
            if (response.isSuccessful) {
                response.body()?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("Empty response body"))
            } else {
                val error = ApiConfig.parseError(response)
                Result.failure(ApiException(error?.message ?: "Failed to update profile", error))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    suspend fun updateLearningStyle(style: String): Result<MessageResponse> {
        return try {
            val response = apiService.updateLearningStyle(
                UpdateLearningStyleRequest(style)
            )
            if (response.isSuccessful) {
                response.body()?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("Empty response body"))
            } else {
                val error = ApiConfig.parseError(response)
                Result.failure(
                    ApiException(
                        error?.message ?: "Failed to update learning style",
                        error
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateInterests(interests: List<String>): Result<MessageResponse> {
        return try {
            val response = apiService.updateInterests(
                UpdateInterestsRequest(interests)
            )
            if (response.isSuccessful) {
                response.body()?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("Empty response body"))
            } else {
                val error = ApiConfig.parseError(response)
                Result.failure(ApiException(error?.message ?: "Failed to update interests", error))
            }
        } catch (e: Exception) {
            Result.failure(e)
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