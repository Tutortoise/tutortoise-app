package com.tutortoise.tutortoise.data.model

data class ApiResponse<T>(
    val status: String,         // "error" | "fail" | "success"
    val message: String?,       // Optional message
    val data: T?                // Generic payload
)

data class MessageResponse(
    val status: String,
    val message: String
)

data class ErrorResponse(
    val status: String,
    val message: String,
    val errors: List<ValidationError>?
)

data class ValidationError(
    val field: String,
    val message: String
)