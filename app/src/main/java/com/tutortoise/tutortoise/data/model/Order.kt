package com.tutortoise.tutortoise.data.model

data class OrderRequest(
    val tutoriesId: String,
    val typeLesson: String,
    val sessionTime: String, // Must be in ISO String, e.g. "2021-08-31T10:00:00" (GMT 0)
    val totalHours: Int,
    val notes: String? = null
)

data class OrderResponse(
    val orderId: String
)