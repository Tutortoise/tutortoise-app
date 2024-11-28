package com.tutortoise.tutortoise.data.model


data class LearnerData(
    override val id: String,
    override val name: String,
    override val email: String,
    val learningStyle: String?,
    override val gender: String?,
    override val phoneNumber: String?,
    override val city: String?,
    override val district: String?,
    override val createdAt: String
) : ProfileData

data class UpdateLearnerProfileRequest(
    val name: String?,
    val phoneNumber: String?,
    val email: String?,
    val gender: String?,
    val city: String?,
    val district: String?,
    val learningStyle: String?,
    val interests: List<String>?
)
