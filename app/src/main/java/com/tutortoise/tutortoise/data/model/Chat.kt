package com.tutortoise.tutortoise.data.model

data class ChatRoom(
    val id: String,
    val learnerId: String,
    val tutorId: String,
    val lastMessageAt: String,
    val createdAt: String,
    val learnerName: String,
    val tutorName: String,
    val lastMessage: LastMessage? = null
)

data class LastMessage(
    val content: String,
    val type: String
)

data class ChatMessage(
    val id: String,
    val roomId: String,
    val senderId: String,
    val senderRole: String,
    val content: String,
    val type: String,
    val sentAt: String,
    val isRead: Boolean
)

data class CreateRoomRequest(
    val learnerId: String,
    val tutorId: String
)

data class SendMessageRequest(
    val content: String
)


data class TypingStatus(
    val isTyping: Boolean,
    val lastTypingAt: Long
)