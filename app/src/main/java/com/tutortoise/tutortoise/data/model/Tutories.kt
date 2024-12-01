package com.tutortoise.tutortoise.data.model

object LessonType {
    const val BOTH = "both"
    const val ONLINE = "online"
    const val OFFLINE = "offline"
}

data class CreateTutoriesRequest(
    val subjectId: String,
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
    val subject: SubjectResponse,
    val aboutYou: String,
    val teachingMethodology: String,
    val hourlyRate: Int,
    val typeLesson: String
)

data class DetailedTutoriesResponse(
    val tutories: TutoriesResponse,
    val tutors: TutorData,
    val subjects: SubjectResponse,
    val alsoTeaches: List<AlsoTeachesResponse>
)

data class AlsoTeachesResponse(
    val subjectName: String,
    val hourlyRate: Int,
    val typeLesson: String,
    val tutoriesId: String? = null
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

data class GetTutoriesLocationResponse(
    val cities: List<String>,
    val district: List<String>
)

data class ExploreTutoriesResponse(
    val id: String,
    val tutorId: String,
    val tutorName: String,
    val subjectName: String,
    val hourlyRate: Int,
    val typeLesson: String,
    val city: String,
    val district: String,
    val avgRating: Float,
    val totalReviews: Int
)