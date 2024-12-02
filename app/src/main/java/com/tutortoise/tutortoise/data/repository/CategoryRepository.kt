package com.tutortoise.tutortoise.data.repository

import android.content.Context
import android.util.Log
import com.tutortoise.tutortoise.data.model.ApiResponse
import com.tutortoise.tutortoise.data.model.CategoryResponse
import com.tutortoise.tutortoise.data.pref.ApiConfig

class CategoryRepository(context: Context) {
    private val apiService = ApiConfig.getApiService(context)

    suspend fun fetchCategories(): ApiResponse<List<CategoryResponse>>? {
        return try {
            val response = apiService.getCategories()
            if (response.isSuccessful) {
                response.body()
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("CategoryRepository", "Failed to fetch categories", e)
            null
        }
    }

    suspend fun fetchPopularCategories(): ApiResponse<List<CategoryResponse>>? {
        return try {
            val response = apiService.getPopularCategories()
            if (response.isSuccessful) {
                response.body()
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("CategoryRepository", "Failed to fetch popular categories", e)
            null
        }
    }

    suspend fun fetchAvailableCategories(): ApiResponse<List<CategoryResponse>>? {
        return try {
            val response = apiService.getAvailableCategories()
            if (response.isSuccessful) {
                response.body()
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("CategoryRepository", "Failed to fetch available categories", e)
            null
        }
    }
}