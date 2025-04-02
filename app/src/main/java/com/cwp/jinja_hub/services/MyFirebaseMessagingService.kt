package com.cwp.jinja_hub.com.cwp.jinja_hub.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.cwp.jinja_hub.MainActivity
import com.cwp.jinja_hub.R
import com.cwp.jinja_hub.com.cwp.jinja_hub.ui.testimony_reviews.fragments.comments.NewsCommentsActivity
import com.cwp.jinja_hub.ui.message.MessageActivity
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

        val receiverId = remoteMessage.data["chat"]
        val reviewId = remoteMessage.data["reviewId"]
        val commentId = remoteMessage.data["reviewId"]
        val newsId = remoteMessage.data["newsId"]
        val type = remoteMessage.data["type"]
        val imageUrl = remoteMessage.data["imageUrl"]
        val imageUrlsString = remoteMessage.data["imageUrls"]

        val message = remoteMessage.data["body"] ?: "New notification received"
        val title = remoteMessage.data["title"] ?: "New Message"

        Log.d("FCM", "ReceiverId: $receiverId, Type: $type, ReviewId: $reviewId, CommentId: $commentId, NewsId: $newsId")

        if (remoteMessage.data.isNotEmpty()) {
            when (type) {
                "message" -> {
                    if (receiverId != null) {
                        if (receiverId == MessageActivity.currentChatId) {
                            return
                        }
                        val displayMessage = if (imageUrlsString != null) "Sent you a photo" else message
                        sendNotification(title, displayMessage, receiverId, type, imageUrl) // Pass the URL
                    } else {
                        Log.e("FCM", "Message type received but receiverId is missing.")
                    }
                }
                "testimonyLike" -> {
                    if (reviewId != null) {
                        sendNotification(title, message, reviewId, type, imageUrl)
                    } else {
                        Log.e("FCM", "Like type received but reviewId is missing.")
                    }
                }
                "testimonyComment" -> {
                    if (commentId != null) {
                        sendNotification(title, message, commentId, type, imageUrl)
                    } else {
                        Log.e("FCM", "Comment type received but reviewId is missing.")
                    }
                }
                "news" -> {
                    if (newsId != null) {
                        sendNotification(title, message, newsId, type, imageUrl)
                    } else {
                        Log.e("FCM", "News type received but newsId is missing.")
                    }
                }
                else -> {
                    Log.e("FCM", "Unknown notification type received: $type")
                }
            }
        }
    }

    private fun sendNotification(title: String, message: String, userId: String, type: String, imageUrl: String? = null) {
        val intent = when (type) {
            "message" -> {
                Intent(this, MessageActivity::class.java).apply {
                    putExtra("receiverId", userId)
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            }
            "testimonyLike" -> {
                Intent(this, MainActivity::class.java).apply {
                    putExtra("REVIEW_ID", userId)
                    putExtra("type", "testimonyLike")
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            }
            "testimonyComment" -> {
                Intent(this, MainActivity::class.java).apply {
                    putExtra("REVIEW_ID", userId)
                    putExtra("type", "testimonyComment")
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            }
            "news" -> {
                Intent(this, MainActivity::class.java).apply {
                    putExtra("News_ID", userId)
                    putExtra("type", "newsComment")
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
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, "Message Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel for chat and like notifications"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        if (imageUrl != null) {
            if (type == "news"){
                Glide.with(this)
                    .asBitmap()
                    .load(imageUrl)
                    .into(object : CustomTarget<Bitmap>() {
                        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                            notificationBuilder.setStyle(NotificationCompat.BigPictureStyle().bigPicture(resource))
                            showNotification(notificationManager, notificationBuilder)
                        }

                        override fun onLoadCleared(placeholder: android.graphics.drawable.Drawable?) {
                            showNotification(notificationManager, notificationBuilder)
                        }
                    })
            }else{
                Glide.with(this)
                    .asBitmap()
                    .load(imageUrl)
                    .into(object : CustomTarget<Bitmap>() {
                        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                            notificationBuilder.setLargeIcon(resource)
                            showNotification(notificationManager, notificationBuilder)
                        }

                        override fun onLoadCleared(placeholder: android.graphics.drawable.Drawable?) {
                            showNotification(notificationManager, notificationBuilder)
                        }
                    })
            }

        } else {
            showNotification(notificationManager, notificationBuilder)
        }
    }

    private fun showNotification(notificationManager: NotificationManager, notificationBuilder: NotificationCompat.Builder) {
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