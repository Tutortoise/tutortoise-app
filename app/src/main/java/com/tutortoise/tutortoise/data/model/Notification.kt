package com.tutortoise.tutortoise.data.model

data class NotificationResponse(
    val id: String,
    val type: String,
    val title: String,
    val message: String,
    val data: Map<String, Any>?,
    val isRead: Boolean,
    val createdAt: String
)