package com.tutortoise.tutortoise.data.model

data class OrderRequest(
    val tutoriesId: String,
    val typeLesson: String,
    val sessionTime: String, // Must be in ISO String, e.g. "2021-08-31T10:00:00" (GMT 0)
    val totalHours: Int,
    val notes: String? = null
)

data class CreateOrderResponse(
    val orderId: String
)

data class OrderResponse(
    val id: String,
    val status: String,
    val sessionTime: String,
    val estimatedEndTime: String,
    val categoryName: String,
    val tutorId: String,
    val tutorName: String,
    val learnerId: String,
    val learnerName: String,
    val typeLesson: String,
    val price: Int,
    val notes: String? = null,
    val createdAt: String,
    val updatedAt: String?
)