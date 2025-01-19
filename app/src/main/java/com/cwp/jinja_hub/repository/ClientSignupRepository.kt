package com.cwp.jinja_hub.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

class ClientSignupRepository {
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().reference

    suspend fun signUpUserWithEmailAndPassword(
        fullName: String,
        username: String,
        email: String,
        password: String
    ): String? {
        return try {
            // Sign up the user
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val userId = result.user?.uid ?: return "No user ID found"

            val randomAvaterImage = listOf(
                "https://api.dicebear.com/6.x/avataaars/jpg?seed=Explorer",
                "https://api.dicebear.com/6.x/avataaars/jpg?seed=Adventurer",
                "https://api.dicebear.com/6.x/avataaars/jpg?seed=CreativeSoul",
                "https://api.dicebear.com/6.x/avataaars/jpg?seed=HappyFace",
                "https://api.dicebear.com/6.x/avataaars/jpg?seed=Dreamer",
            )

            // Save additional user details in the database
            val userMap = mapOf(
                "fullName" to fullName,
                "firstName" to fullName.split(" ")[0],
                "lastName" to fullName.split(" ").last(),
                "username" to username,
                "email" to email,
                "password" to password,
                "userId" to userId,
                "userType" to "client",
                "isVerified" to false,
                "profileImage" to randomAvaterImage.random() // generate random avatar image
            )
            database.child("Users").child(userId).setValue(userMap).await()
            userId
        } catch (e: Exception) {
            "An error occurred: ${e.message}"
        }
    }

    suspend fun confirmUserEmail(): Boolean {
        return try {
            val user = firebaseAuth.currentUser ?: return false
            user.sendEmailVerification().await()
            true
        } catch (e: Exception) {
            false
        }
    }
}
