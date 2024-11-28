package com.tutortoise.tutortoise.data.model


interface ProfileData {
    val id: String
    val name: String
    val email: String
    val gender: String?
    val phoneNumber: String?
    val city: String?
    val district: String?
    val createdAt: String
}