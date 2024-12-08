package com.tutortoise.tutortoise.data.model

data class ReviewRequest(
    val rating: Int,
    val message: String,
)

data class ReviewResponse(
    val id: String,
    val learnerId: String,
    val rating: Float,
    val message: String,
    val createdAt: String,
    val learnerName: String,
)