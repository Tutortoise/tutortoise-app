package com.tutortoise.tutortoise.data.model

data class Tutories(
    val id: String,
    val subjectId: String,
    val aboutYou: String,
    val teachingMethodology: String,
    val hourlyRate: Int,
    val typeLesson: String, // "online" | "offline" | "both"
)

data class CreateTutoriesRequest(
    val subjectId: String,
    val aboutYou: String,
    val teachingMethodology: String,
    val hourlyRate: Int,
    val typeLesson: String,
)

data class GetMyTutoriesResponse(
    val id: String,
    val tutorName: String,
    val subjectName: String,
    val hourlyRate: Int,
    val typeLesson: String,
    val city: String,
    val district: String,
)