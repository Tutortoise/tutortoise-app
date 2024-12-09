package com.tutortoise.tutortoise.data.model

enum class LessonType(val value: String) {
    ONLINE("online"),
    OFFLINE("offline"),
    BOTH("both");

    companion object {
        fun fromString(value: String): LessonType {
            return LessonType.entries.find { it.value == value.lowercase() }
                ?: throw IllegalArgumentException("Unknown lesson type: $value")
        }
    }
}

data class CreateTutoriesRequest(
    val name: String,
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
    val typeLesson: String,
    val isEnabled: Boolean
)

data class DetailedTutoriesResponse(
    val id: String,
    val name: String,
    val tutorId: String,
    val categoryName: String,
    val categoryId: String,
    val tutorName: String,
    val aboutYou: String,
    val teachingMethodology: String,
    val hourlyRate: Int,
    val typeLesson: String,
    val city: String,
    val district: String,
    val isEnabled: Boolean,
    val alsoTeaches: List<AlsoTeachesResponse>,
    val avgRating: Float,
    val totalReviews: Int,
    val totalOrders: Int,
    val totalLearners: Int
)

data class AlsoTeachesResponse(
    val id: String,
    val categoryName: String,
    val hourlyRate: Int,
    val typeLesson: String,
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
    val name: String,
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