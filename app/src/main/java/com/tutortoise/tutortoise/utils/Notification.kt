package com.tutortoise.tutortoise.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.tutortoise.tutortoise.R

object Notification {
    const val CHANNEL_CHAT_ID = "chat_notifications"

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channels = listOf(
                NotificationChannel(
                    CHANNEL_CHAT_ID,
                    "Chat Messages",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Notifications for new chat messages"
                    enableVibration(true)
                }
            )

            val notificationManager = context.getSystemService(NotificationManager::class.java)
            channels.forEach { channel ->
                notificationManager.createNotificationChannel(channel)
            }
        }
    }

    fun buildNotification(
        context: Context,
        channelId: String,
        title: String,
        message: String
    ): NotificationCompat.Builder {
        return NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
    }
}
