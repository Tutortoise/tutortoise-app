package com.tutortoise.tutortoise.data.model

data class ReviewResponse(
    val id: String,
    val learnerId: String,
    val rating: Float,
    val message: String,
    val createdAt: String,
    val learnerName: String,
)