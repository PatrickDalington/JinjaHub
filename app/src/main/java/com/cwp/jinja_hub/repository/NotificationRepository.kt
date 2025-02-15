package com.cwp.jinja_hub.repository

import android.util.Log
import com.cwp.jinja_hub.model.NotificationModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.tasks.await

class NotificationRepository {

    private val fUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser

    // Simulate fetching the latest notifications.
    fun fetchLatestNotifications(): List<NotificationModel> {
        return emptyList()
    }

    fun fetchNotifications(onUpdate: (List<NotificationModel>) -> Unit) {
        FirebaseDatabase.getInstance()
            .reference.child("Notifications").child(fUser!!.uid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val notifications = snapshot.children.mapNotNull { child ->
                        child.getValue(NotificationModel::class.java)
                    }
                    onUpdate(notifications)
                }
                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                }
            })
    }


    suspend fun sendNotification(id: String, notification: NotificationModel): Boolean {
        return try {
            // Push a new notification under the "Notifications" node.
            val newNotificationRef = FirebaseDatabase.getInstance()
                .reference.child("Notifications").child(id)
                .push()

            notification.isRead = false
            // Optionally, update the notification's id with the generated key.
            newNotificationRef.key?.let { generatedKey ->
                notification.id = generatedKey
            }

            // Set the value of the new node to the notification.
            newNotificationRef.setValue(notification).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun markNotificationAsRead(notificationId: String): Boolean {
        return try {
            FirebaseDatabase.getInstance().reference
                .child("Notifications").child(fUser!!.uid)
                .child(notificationId)
                .child("isRead")
                .setValue(true)
                .await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun deleteNotification(notificationId: String): Boolean {
        return try {
            FirebaseDatabase.getInstance().reference
                .child("Notifications").child(fUser!!.uid)
                .child(notificationId)
                .removeValue()
            true
        }catch (e: Exception){
            e.printStackTrace()
            false
        }
    }

    suspend fun deleteAllNotifications(): Boolean {
        return try {
            FirebaseDatabase.getInstance().reference
                .child("Notifications").child(fUser!!.uid)
                .removeValue()
            true
        }catch (e: Exception){
            e.printStackTrace()
            false
        }
    }
}
