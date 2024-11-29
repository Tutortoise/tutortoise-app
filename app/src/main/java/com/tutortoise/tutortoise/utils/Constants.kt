package com.tutortoise.tutortoise.utils

object Constants {
    private const val GCS_BASE_URL =
        "https://storage.googleapis.com/tutortoise-bucket"

    fun getProfilePictureUrl(userId: String): String {
        // Use current timestamp as a random value
        // This should force Glide to reload the image.
        val randomQuery = System.currentTimeMillis()
        return "${GCS_BASE_URL}/profile-pictures/${userId}.jpg?rand=$randomQuery"
    }

    fun getSubjectIconUrl(subjectName: String): String {
        return "${GCS_BASE_URL}/subjects/${subjectName}.png"
    }
}