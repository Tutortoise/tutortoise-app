package com.tutortoise.tutortoise.data.pref

import android.app.ActivityManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.tutortoise.tutortoise.domain.FCMTokenManager
import com.tutortoise.tutortoise.presentation.chat.ChatRoomActivity
import com.tutortoise.tutortoise.presentation.main.MainActivity
import com.tutortoise.tutortoise.utils.Notification
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class MessagingService : FirebaseMessagingService() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val TAG = "MessagingService"

    override fun onCreate() {
        super.onCreate()
        Notification.createNotificationChannels(this)
    }

    override fun onNewToken(token: String) {
        Log.d(TAG, "New token: $token")
        scope.launch {
            try {
                FCMTokenManager.updateToken(applicationContext, token)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update FCM token", e)
            }
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        if (isAppInForeground()) {
            handleForegroundMessage(remoteMessage)
        } else {
            handleBackgroundMessage(remoteMessage)
        }
    }

    private fun isAppInForeground(): Boolean {
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val appProcesses = activityManager.runningAppProcesses ?: return false
        val packageName = packageName
        for (appProcess in appProcesses) {
            if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
                && appProcess.processName == packageName
            ) {
                return true
            }
        }
        return false
    }

    private fun handleForegroundMessage(remoteMessage: RemoteMessage) {
        when (remoteMessage.data["type"]) {
            "chat_message" -> handleChatNotification(remoteMessage.data)
            else -> handleDefaultNotification(remoteMessage)
        }
    }

    private fun handleBackgroundMessage(remoteMessage: RemoteMessage) {
        when (remoteMessage.data["type"]) {
            "chat_message" -> handleChatNotification(remoteMessage.data)
            else -> handleDefaultNotification(remoteMessage)
        }
    }

    private fun handleChatNotification(data: Map<String, String>) {
        val roomId = data["roomId"] ?: return
        val senderName = data["senderName"] ?: return
        val messageContent = data["content"] ?: return

        val intent = Intent(this, ChatRoomActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("ROOM_ID", roomId)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = Notification
            .buildNotification(
                this,
                Notification.CHANNEL_CHAT_ID,
                senderName,
                messageContent
            )
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    private fun handleDefaultNotification(remoteMessage: RemoteMessage) {
        val title = remoteMessage.notification?.title ?: return
        val message = remoteMessage.notification?.body ?: return

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = Notification
            .buildNotification(
                this,
                Notification.CHANNEL_CHAT_ID,
                title,
                message
            )
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}