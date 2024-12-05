package com.tutortoise.tutortoise.data.repository

import android.content.Context
import android.util.Log
import com.tutortoise.tutortoise.data.model.OrderRequest
import com.tutortoise.tutortoise.data.model.OrderResponse
import com.tutortoise.tutortoise.data.pref.ApiConfig
import com.tutortoise.tutortoise.data.pref.ApiException

class OrderRepository(context: Context) {
    private val api = ApiConfig.getApiService(context)

    suspend fun reserveOrder(
        tutoriesId: String, typeLesson: String, sessionTime: String, totalHours: Int, notes: String?
    ): Result<OrderResponse> {
        return try {
            val response =
                api.order(OrderRequest(tutoriesId, typeLesson, sessionTime, totalHours, notes))
            if (response.isSuccessful) {
                response.body()?.data?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("Empty response body"))
            } else {
                val error = ApiConfig.parseError(response)
                Result.failure(ApiException(error?.message ?: "Failed to create room", error))
            }
        } catch (e: Exception) {
            Log.e("OrderRepository", "Failed to order", e)
            Result.failure(e)
        }
    }
}