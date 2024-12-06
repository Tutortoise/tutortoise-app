package com.tutortoise.tutortoise.data.repository

import android.content.Context
import android.util.Log
import com.tutortoise.tutortoise.data.model.CreateOrderResponse
import com.tutortoise.tutortoise.data.model.MessageResponse
import com.tutortoise.tutortoise.data.model.OrderRequest
import com.tutortoise.tutortoise.data.model.OrderResponse
import com.tutortoise.tutortoise.data.pref.ApiConfig
import com.tutortoise.tutortoise.data.pref.ApiException

class OrderRepository(context: Context) {
    private val api = ApiConfig.getApiService(context)

    suspend fun reserveOrder(
        tutoriesId: String, typeLesson: String, sessionTime: String, totalHours: Int, notes: String?
    ): Result<CreateOrderResponse> {
        return try {
            val response =
                api.order(OrderRequest(tutoriesId, typeLesson, sessionTime, totalHours, notes))
            if (response.isSuccessful) {
                response.body()?.data?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("Empty response body"))
            } else {
                val error = ApiConfig.parseError(response)
                Result.failure(ApiException(error?.message ?: "Failed to create order", error))
            }
        } catch (e: Exception) {
            Log.e("OrderRepository", "Failed to order", e)
            Result.failure(e)
        }
    }

    suspend fun getMyOrders(status: String): Result<List<OrderResponse>> {
        return try {
            val response = api.getMyOrders(status)
            if (response.isSuccessful) {
                response.body()?.data?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("Empty response body"))
            } else {
                val error = ApiConfig.parseError(response)
                Result.failure(ApiException(error?.message ?: "Failed to get my orders", error))
            }
        } catch (e: Exception) {
            Log.e("OrderRepository", "Failed to get my orders", e)
            Result.failure(e)
        }
    }

    suspend fun acceptOrder(orderId: String): Result<MessageResponse> {
        return try {
            val response = api.acceptOrder(orderId)
            if (response.isSuccessful) {
                response.body()?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("Empty response body"))
            } else {
                val error = ApiConfig.parseError(response)
                Result.failure(ApiException(error?.message ?: "Failed to accept order", error))
            }
        } catch (e: Exception) {
            Log.e("OrderRepository", "Failed to accept order", e)
            Result.failure(e)
        }
    }

    suspend fun rejectOrder(orderId: String): Result<MessageResponse> {
        return try {
            val response = api.rejectOrder(orderId)
            if (response.isSuccessful) {
                response.body()?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("Empty response body"))
            } else {
                val error = ApiConfig.parseError(response)
                Result.failure(ApiException(error?.message ?: "Failed to reject order", error))
            }
        } catch (e: Exception) {
            Log.e("OrderRepository", "Failed to reject order", e)
            Result.failure(e)
        }
    }
}