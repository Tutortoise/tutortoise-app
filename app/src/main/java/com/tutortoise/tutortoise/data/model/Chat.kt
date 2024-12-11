package com.tutortoise.tutortoise.data.model

import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

data class ChatRoom(
    val id: String,
    val learnerId: String,
    val tutorId: String,
    val lastMessageAt: String,
    val createdAt: String,
    val learnerName: String,
    val tutorName: String,
    val lastMessage: LastMessage? = null,
    val unreadCount: Int = 0
)

data class LastMessage(
    val content: String,
    val type: String
)

data class ChatMessage(
    val id: String = "",
    val roomId: String = "",
    val senderId: String = "",
    val senderRole: String = "",
    val content: String = "",
    val type: String = "text",
    val sentAt: String = "",
    val isRead: Boolean = false
)

data class FirebaseMessage(
    val id: String = "",
    val roomId: String = "",
    val senderId: String = "",
    val senderRole: String = "",
    val content: String = "",
    val type: String = "text",
    val sentAt: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
) {
    fun toChatMessage(): ChatMessage {
        return ChatMessage(
            id = id,
            roomId = roomId,
            senderId = senderId,
            senderRole = senderRole,
            content = content,
            type = type,
            sentAt = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                .withZone(ZoneOffset.UTC)
                .format(Instant.ofEpochMilli(sentAt)),
            isRead = isRead
        )
    }
}

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