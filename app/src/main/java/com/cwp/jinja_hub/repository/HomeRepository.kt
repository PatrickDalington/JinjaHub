package com.cwp.jinja_hub.repository

import com.cwp.jinja_hub.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase

class HomeRepository {
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firebaseDatabase = FirebaseDatabase.getInstance()
    private val usersRef = firebaseDatabase.getReference("Users")
    private val servicesRef = firebaseDatabase.getReference("services")

    fun getCurrentUser(): FirebaseUser? {
        return firebaseAuth.currentUser
    }

    fun signOut() {
        firebaseAuth.signOut()
    }

    fun getUserInfo(userId: String, callback: (User?) -> Unit) {
        usersRef.child(userId).get().addOnSuccessListener { dataSnapshot ->
            if (dataSnapshot.exists()) {
                val user = dataSnapshot.getValue(User::class.java)
                callback(user)
            } else {
                callback(null) // User not found
            }
        }.addOnFailureListener {
            callback(null) // Handle database failure
        }
    }


}