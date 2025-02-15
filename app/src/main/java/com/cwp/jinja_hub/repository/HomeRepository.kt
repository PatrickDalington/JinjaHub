package com.cwp.jinja_hub.repository

import android.util.Log
import com.cwp.jinja_hub.model.NormalUser
import com.cwp.jinja_hub.model.NotificationModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class HomeRepository {
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firebaseDatabase = FirebaseDatabase.getInstance()
    private val usersRef = firebaseDatabase.getReference("Users")
    private val servicesRef = firebaseDatabase.getReference("services")
    private val notificationRef = firebaseDatabase.getReference("Notifications")

    fun getCurrentUser(): FirebaseUser? {
        return firebaseAuth.currentUser
    }

    fun signOut() {
        firebaseAuth.signOut()
    }

    fun getUserInfo(userId: String, callback: (NormalUser?) -> Unit) {
        usersRef.keepSynced(true)
        usersRef.child(userId).get().addOnSuccessListener { dataSnapshot ->
            if (dataSnapshot.exists()) {
                val user = dataSnapshot.getValue(NormalUser::class.java)
                callback(user)
            } else {
                callback(null) // User not found
            }
        }.addOnFailureListener {
            callback(null) // Handle database failure
        }
    }

    fun checkUnreadNotifications(userId: String, callback: (Boolean) -> Unit) {
        notificationRef.keepSynced(true)
        notificationRef.child(userId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val unreadNotifications = dataSnapshot.children.any {
                        it.child("isRead").getValue(Boolean::class.java) == false
                    }
                    Log.d("HomeRep", "Unread notifications: $unreadNotifications")
                    callback(unreadNotifications)
                } else {
                    callback(false) // No notifications found
                }

            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

    }

}