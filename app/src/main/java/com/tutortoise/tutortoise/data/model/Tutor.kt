package com.tutortoise.tutortoise.data.model

data class TutorData(
    override val id: String,
    override val name: String,
    override val email: String,
    override val gender: String?,
    override val phoneNumber: String?,
    override val city: String?,
    override val district: String?,
    override val createdAt: String,
    val availability: Map<Int, List<String>>?,
) : ProfileData


data class UpdateTutorProfileRequest(
    val name: String? = null,
    val phoneNumber: String? = null,
    val email: String? = null,
    val gender: String? = null,
    val city: String? = null,
    val district: String? = null,
    val availability: Map<Int, List<String>>? = null
)