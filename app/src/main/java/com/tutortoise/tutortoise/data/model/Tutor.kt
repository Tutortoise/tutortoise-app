package com.tutortoise.tutortoise.data.model

data class TutorData(
    override val id: String,
    override val name: String,
    override val email: String,
    override val gender: String?,
    override val phoneNumber: String?,
    override val city: String?,
    override val district: String?,
    override val createdAt: String
) : ProfileData

data class UpdateTutorProfileRequest(
    val name: String?,
    val phoneNumber: String?,
    val email: String?,
    val gender: String?,
    val city: String?,
    val district: String?,
)