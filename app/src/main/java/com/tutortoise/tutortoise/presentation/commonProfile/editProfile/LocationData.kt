package com.tutortoise.tutortoise.presentation.commonProfile.editProfile

data class LocationData(
    var city: String? = null,
    var district: String? = null
) {
    fun hasValidLocation(): Boolean = !city.isNullOrEmpty() || !district.isNullOrEmpty()
}