package com.tutortoise.tutortoise.data.repository

import android.content.Context
import android.util.Log
import com.tutortoise.tutortoise.data.model.ApiResponse
import com.tutortoise.tutortoise.data.model.CreateTutoriesRequest
import com.tutortoise.tutortoise.data.model.DetailedTutoriesResponse
import com.tutortoise.tutortoise.data.model.EditTutoriesRequest
import com.tutortoise.tutortoise.data.model.ExploreTutoriesResponse
import com.tutortoise.tutortoise.data.model.GetMyTutoriesResponse
import com.tutortoise.tutortoise.data.model.MessageResponse
import com.tutortoise.tutortoise.data.pref.ApiConfig
import com.tutortoise.tutortoise.data.pref.ApiException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException

class TutoriesRepository(context: Context) {
    private val apiService = ApiConfig.getApiService(context)

    suspend fun getMyTutories(): ApiResponse<List<GetMyTutoriesResponse>>? {
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

    suspend fun searchTutories(
        query: String? = null,
        subjectId: String? = null,
        city: String? = null
    ): ApiResponse<List<ExploreTutoriesResponse>>? {
        return try {
            val response = apiService.searchTutories(query, subjectId, city)
            if (response.isSuccessful) {
                response.body()
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("TutoriesRepository", "Failed to search tutories", e)
            null
        }
    }

    suspend fun createTutories(request: CreateTutoriesRequest): Result<MessageResponse> =
        withContext(Dispatchers.IO) {
            return@withContext try {
                Log.d("TutoriesRepository", "Creating tutories with request: $request")
                val response = apiService.createTutories(
                    CreateTutoriesRequest(
                        subjectId = request.subjectId,
                        aboutYou = request.aboutYou,
                        teachingMethodology = request.teachingMethodology,
                        hourlyRate = request.hourlyRate,
                        typeLesson = request.typeLesson,
                    )
                )

                Result.success(response)
            } catch (e: HttpException) {
                val errorBody = ApiConfig.parseError(e.response()!!)
                Result.failure(ApiException(errorBody?.message ?: "Registration failed", errorBody))
            } catch (e: Exception) {
                Log.e("TutoriesRepository", "Failed to create tutories", e)
                Result.failure(e)
            }
        }

    suspend fun getTutoriesById(tutoriesId: String): Result<DetailedTutoriesResponse> {
        return try {
            val response = apiService.getTutoriesById(tutoriesId)
            if (response.isSuccessful) {
                response.body()?.data?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("Empty response body"))
            } else {
                val error = ApiConfig.parseError(response)
                Result.failure(ApiException(error?.message ?: "Failed to get tutories", error))
            }
        } catch (e: Exception) {
            Log.e("TutoriesRepository", "Failed to get tutories", e)
            Result.failure(e)
        }
    }


    suspend fun updateTutories(
        tutoriesId: String,
        request: EditTutoriesRequest
    ): Result<MessageResponse> =
        withContext(Dispatchers.IO) {
            return@withContext try {
                Log.d("TutoriesRepository", "Updating tutories with id: $tutoriesId")
                val response = apiService.updateTutories(
                    tutoriesId,
                    EditTutoriesRequest(
                        aboutYou = request.aboutYou,
                        teachingMethodology = request.teachingMethodology,
                        hourlyRate = request.hourlyRate,
                        typeLesson = request.typeLesson
                    )
                )

                Result.success(response)
            } catch (e: HttpException) {
                val errorBody = ApiConfig.parseError(e.response()!!)
                Result.failure(ApiException(errorBody?.message ?: "Update failed", errorBody))
            } catch (e: Exception) {
                Log.e("TutoriesRepository", "Failed to update tutories", e)
                Result.failure(e)
            }
        }
}