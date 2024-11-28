package com.tutortoise.tutortoise.data.model

data class RegisterData(
    val userId: String
)

data class LoginData(
    val token: String
)

data class UserResponse(
    val id: String,
    val name: String,
    val email: String,
    val role: String,
)


data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    val role: String
)

data class LoginRequest(val email: String, val password: String)
data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String,
    val confirmPassword: String
)