package com.tutortoise.tutortoise.data.model

data class TutoriesServiceModel(
    val id: String,
    val subject: String,
    val about: String,
    val methodology: String,
    val ratePerHour: Int,
    val isOnline: Boolean,
    val isFaceToFace: Boolean
)

data class CreateTutoriesRequest(
    val subject: String,
    val about: String,
    val methodology: String,
    val ratePerHour: Int,
    val isOnline: Boolean,
    val isFaceToFace: Boolean
)