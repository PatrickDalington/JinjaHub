package com.cwp.jinja_hub.com.cwp.jinja_hub.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.cwp.jinja_hub.MainActivity
import com.cwp.jinja_hub.R
import com.cwp.jinja_hub.ui.message.MessageActivity
import com.cwp.jinja_hub.ui.testimony_reviews.fragments.comments.LatestCommentsActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        Log.d("FCM_TOKEN", "Refreshed token: $token")
        sendTokenToServer(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d("FCM_MESSAGE", "Message received: ${remoteMessage.data}")

        // Checkable extras
        val receiverId = remoteMessage.data["chat"]
        val reviewId = remoteMessage.data["reviewId"]
        val commentId = remoteMessage.data["reviewId"]
        val newsId = remoteMessage.data["newsId"]

        // type
        val type = remoteMessage.data["type"]

        // title and body
        val message = remoteMessage.data["body"] ?: "New notification received"
        val title = remoteMessage.data["title"] ?: "New Message"

        Log.d("FCM", "ReceiverId: $receiverId, Type: $type, ReviewId: $reviewId")



        if (remoteMessage.data.isNotEmpty()) {
            when (type) {
                "message" -> {
                    if (receiverId != null) {
                        // Prevent notification if user is in the chat screen
                        if (receiverId == MessageActivity.currentChatId) {
                            Log.d("FCM", "User is already in chat with $receiverId. Notification not shown.")
                            return
                        }
                        sendNotification(title, message, receiverId, type)
                    } else {
                        Log.e("FCM", "Message type received but receiverId is missing.")
                    }
                }
                "like" -> {
                    if (reviewId != null) {
                        sendNotification(title, message, reviewId, type)
                    } else {
                        Log.e("FCM", "Like type received but reviewId is missing.")
                    }
                }
                "comment" -> {
                    if (commentId != null) {
                        sendNotification(title, message, commentId, type)
                    } else {
                        Log.e("FCM", "Comment type received but reviewId is missing.")
                    }
                }
                else -> {
                    Log.e("FCM", "Unknown notification type received: $type")
                }
            }
        }
    }

    private fun sendNotification(title: String, message: String, userId: String, type: String) {
        val intent = when (type) {
            "message" -> {
                Intent(this, MessageActivity::class.java).apply {
                    putExtra("receiverId", userId)
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            }
            "like" -> {
                Intent(this, MainActivity::class.java).apply {
                    putExtra("REVIEW_ID", userId)
                    addFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            }
            "comment" -> {
                Intent(this, LatestCommentsActivity::class.java).apply {
                    putExtra("REVIEW_ID", userId)
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            }
            else -> {
                return
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            this, userId.hashCode(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = "message_channel"
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.jinja_hub_logo)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setPriority(NotificationCompat.PRIORITY_HIGH) // ✅ Ensures heads-up notification
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // ✅ Ensure Notification Channel is properly set
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, "Message Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel for chat and like notifications"
                enableVibration(true) // Ensures it vibrates
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notificationId = System.currentTimeMillis().toInt()
        notificationManager.notify(notificationId, notificationBuilder.build())

        Log.d("FCM", "Notification sent with ID: $notificationId")
    }

    private fun sendTokenToServer(token: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val database = FirebaseDatabase.getInstance().getReference("Users").child(userId)
        database.child("fcmToken").setValue(token)
    }
}
