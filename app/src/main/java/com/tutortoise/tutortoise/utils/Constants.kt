package com.tutortoise.tutortoise.utils

object Constants {
    private const val GCS_BASE_URL =
        "https://storage.googleapis.com/tutortoise-bucket"

    fun getProfilePictureUrl(userId: String): String {
        return "${GCS_BASE_URL}/profile-pictures/${userId}.jpg"
    }

    fun getProfilePictureUrlNotCached(userId: String): String {
        // Use current timestamp as a random value
        // This should force Glide to reload the image.
        // This is useful when the user changes their profile picture.
        val randomQuery = System.currentTimeMillis()
        return "${GCS_BASE_URL}/profile-pictures/${userId}.jpg?rand=$randomQuery"
    }

    fun getCategoryIconUrl(categoryName: String): String {
        return "${GCS_BASE_URL}/categories/${categoryName}.png"
    }
}