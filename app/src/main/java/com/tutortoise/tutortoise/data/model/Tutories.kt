package com.tutortoise.tutortoise.data.model

object LessonType {
    const val BOTH = "both"
    const val ONLINE = "online"
    const val OFFLINE = "offline"
}

data class CreateTutoriesRequest(
    val categoryId: String,
    val aboutYou: String,
    val teachingMethodology: String,
    val hourlyRate: Int,
    val typeLesson: String,
)

data class EditTutoriesRequest(
    val aboutYou: String,
    val teachingMethodology: String,
    val hourlyRate: Int,
    val typeLesson: String
)

data class TutoriesResponse(
    val id: String,
    val category: CategoryResponse,
    val aboutYou: String,
    val teachingMethodology: String,
    val hourlyRate: Int,
    val typeLesson: String
)

data class DetailedTutoriesResponse(
    val tutories: TutoriesResponse,
    val tutors: TutorData,
    val categories: CategoryResponse,
    val alsoTeaches: List<AlsoTeachesResponse>
)

data class AlsoTeachesResponse(
    val categoryName: String,
    val hourlyRate: Int,
    val typeLesson: String,
    val tutoriesId: String? = null
)

data class GetMyTutoriesResponse(
    val id: String,
    val tutorName: String,
    val categoryName: String,
    val hourlyRate: Int,
    val typeLesson: String,
    val city: String,
    val district: String,
)

data class GetTutoriesLocationResponse(
    val cities: List<String>,
    val district: List<String>
)

data class ExploreTutoriesResponse(
    val id: String,
    val tutorId: String,
    val tutorName: String,
    val categoryName: String,
    val hourlyRate: Int,
    val typeLesson: String,
    val city: String,
    val district: String,
    val avgRating: Float,
    val totalReviews: Int
)