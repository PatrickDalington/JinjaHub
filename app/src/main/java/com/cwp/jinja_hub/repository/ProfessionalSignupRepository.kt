package com.cwp.jinja_hub.repository

import android.net.Uri
import android.util.Log
import at.favre.lib.crypto.bcrypt.BCrypt
import com.cwp.jinja_hub.helpers.SignUpResult
import com.cwp.jinja_hub.model.ProfessionalUser
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await


class ProfessionalSignupRepository {
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().reference
    private val userRef = FirebaseDatabase.getInstance().getReference("Users")

    suspend fun signUpUserWithEmailAndPassword(
        fullName: String,
        firstName: String,
        lastName: String,
        username: String,
        email: String,
        password: String,
        gender: String,
        age: String,
        address: String,
        workplace: String,
        medicalProfessional: String,
        licence: String,
        yearsOfWork: String,
        consultationTime: String,
        profileImage: String?
    ): SignUpResult {
        return try {
            // Sign up the user in Firebase Authentication
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val userId = result.user?.uid ?: return SignUpResult.Error("No user ID found")

            // Hash the password before storing
            val hashedPassword = BCrypt.withDefaults()
                .hashToString(12, password.toCharArray())  // 12 is the cost factor

            // Generate a random avatar if no profile image is provided
            val maleAvatar = listOf(
                "https://api.dicebear.com/6.x/avataaars/jpg?seed=Explorer",
                "https://api.dicebear.com/6.x/avataaars/jpg?seed=Adventurer",
                "https://api.dicebear.com/6.x/avataaars/jpg?seed=CreativeSoul",
                "https://api.dicebear.com/6.x/avataaars/jpg?seed=HappyFace",
                "https://api.dicebear.com/6.x/avataaars/jpg?seed=Dreamer",
                "https://api.dicebear.com/6.x/avataaars/jpg?seed=James",
                "https://api.dicebear.com/6.x/avataaars/jpg?seed=Oliver",
                "https://api.dicebear.com/6.x/avataaars/jpg?seed=Elijah",
                "https://api.dicebear.com/6.x/avataaars/jpg?seed=William",
                "https://api.dicebear.com/6.x/avataaars/jpg?seed=Henry",
                "https://api.dicebear.com/6.x/avataaars/jpg?seed=Alexander",
                "https://api.dicebear.com/6.x/avataaars/jpg?seed=Sebastian",
                "https://api.dicebear.com/6.x/avataaars/jpg?seed=Jack",
                "https://api.dicebear.com/6.x/avataaars/jpg?seed=Owen",
                "https://api.dicebear.com/6.x/avataaars/jpg?seed=Theodore",
                "https://api.dicebear.com/6.x/avataaars/jpg?seed=Caleb",
                "https://api.dicebear.com/6.x/avataaars/jpg?seed=Lucas",
                "https://api.dicebear.com/6.x/avataaars/jpg?seed=Mason",
                "https://api.dicebear.com/6.x/avataaars/jpg?seed=Leo",
                "https://api.dicebear.com/6.x/avataaars/jpg?seed=Julian",
                "https://api.dicebear.com/6.x/avataaars/jpg?seed=Isaac",
                "https://api.dicebear.com/6.x/avataaars/jpg?seed=Lincoln",
                "https://api.dicebear.com/6.x/avataaars/jpg?seed=Ezra",
                "https://api.dicebear.com/6.x/avataaars/jpg?seed=Asher",
                "https://api.dicebear.com/6.x/avataaars/jpg?seed=Dominic",
                "https://api.dicebear.com/6.x/avataaars/jpg?seed=Eli",
                "https://api.dicebear.com/6.x/avataaars/jpg?seed=Miles",
                "https://api.dicebear.com/6.x/avataaars/jpg?seed=Harrison",
                "https://api.dicebear.com/6.x/avataaars/jpg?seed=Nathaniel",
                "https://api.dicebear.com/6.x/avataaars/jpg?seed=Adam"
            ).random()

            val femaleAvatar = listOf(
                "https://api.dicebear.com/6.x/avataaars/jpg?seed=Grace",
                "https://api.dicebear.com/6.x/avataaars/jpg?seed=Sophia",
                "https://api.dicebear.com/6.x/avataaars/jpg?seed=Emma",
                "https://api.dicebear.com/6.x/avataaars/jpg?seed=Lily",
                "https://api.dicebear.com/6.x/avataaars/jpg?seed=Olivia",
                "https://api.dicebear.com/6.x/avataaars/jpg?seed=HappyFace",
                "https://api.dicebear.com/6.x/avataaars/jpg?seed=Charlotte",
                "https://api.dicebear.com/6.x/avataaars/jpg?seed=Amelia",
                "https://api.dicebear.com/6.x/avataaars/jpg?seed=Evelyn",
                "https://api.dicebear.com/6.x/avataaars/jpg?seed=Harper",
                "https://api.dicebear.com/6.x/avataaars/jpg?seed=Madison",
                "https://api.dicebear.com/6.x/avataaars/jpg?seed=Hannah",
                "https://api.dicebear.com/6.x/avataaars/jpg?seed=Zoe",
                "https://api.dicebear.com/6.x/avataaars/jpg?seed=Lucy",
                "https://api.dicebear.com/6.x/avataaars/jpg?seed=Stella",
                "https://api.dicebear.com/6.x/avataaars/jpg?seed=Aria",
                "https://api.dicebear.com/6.x/avataaars/jpg?seed=Layla",
                "https://api.dicebear.com/6.x/avataaars/jpg?seed=Nora",
                "https://api.dicebear.com/6.x/avataaars/jpg?seed=Hazel",
                "https://api.dicebear.com/6.x/avataaars/jpg?seed=Lillian",
                "https://api.dicebear.com/6.x/avataaars/jpg?seed=Paisley",
                "https://api.dicebear.com/6.x/avataaars/jpg?seed=Aurora",
                "https://api.dicebear.com/6.x/avataaars/jpg?seed=Savannah",
                "https://api.dicebear.com/6.x/avataaars/jpg?seed=Brooklyn",
                "https://api.dicebear.com/6.x/avataaars/jpg?seed=Skylar",
                "https://api.dicebear.com/6.x/avataaars/jpg?seed=Ivy",
                "https://api.dicebear.com/6.x/avataaars/jpg?seed=Eliana",
                "https://api.dicebear.com/6.x/avataaars/jpg?seed=Clara",
                "https://api.dicebear.com/6.x/avataaars/jpg?seed=Delilah",
                "https://api.dicebear.com/6.x/avataaars/jpg?seed=Isla",
                "https://api.dicebear.com/6.x/avataaars/jpg?seed=Sadie"
            ).random()

            val userMap = mapOf(
                "fullName" to fullName,
                "firstName" to firstName,
                "lastName" to lastName,
                "username" to username,
                "email" to email,
                "password" to hashedPassword,  // Storing the hashed password
                "gender" to gender,
                "age" to age,
                "address" to address,
                "workplace" to workplace,
                "medicalProfessional" to medicalProfessional,
                "licence" to licence,
                "yearsOfWork" to yearsOfWork,
                "consultationTime" to consultationTime,
                if (gender.equals("male", ignoreCase = true))
                    "profileImage" to maleAvatar
                else
                    "profileImage" to femaleAvatar,
                "isProfessional" to true,
                "userId" to userId,
                "isVerified" to false
            )

            database.child("Users").child(userId).setValue(userMap).await()
            SignUpResult.Success(userId)
        } catch (e: FirebaseAuthUserCollisionException) {
            e.printStackTrace()
            SignUpResult.Error("This email address is already registered.")
        } catch (e: Exception) {
            e.printStackTrace()
            SignUpResult.Error(e.message ?: "An unexpected error occurred")
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


    fun updateUserProfile(userId: String, updates: Map<String, Any?>, callback: (Boolean) -> Unit) {
        database.child("Users").child(userId).get()
            .addOnSuccessListener { userSnapshot ->
                if (!userSnapshot.exists()) {
                    callback(false)
                } else {
                    database.child("Users").child(userId).updateChildren(updates)
                        .addOnSuccessListener { callback(true) }
                        .addOnFailureListener { callback(false) }
                }
            }
            .addOnFailureListener { callback(false) }
    }

    // send verification link to verify email and update user verification status
    fun sendVerificationLink(userId: String, callback: (Boolean) -> Unit) {
        val user = firebaseAuth.currentUser
        user?.sendEmailVerification()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                updateUserProfile(userId, mapOf("isVerified" to true), callback)
            } else {
                callback(false)
            }
        }
    }

    // check if user is verified
    /**
     * Fetches user details based on the user ID.
     *
     * @param userId The ID of the user whose details are to be fetched.
     * @param callback Callback invoked with user details (fullName, username, profileImage).
     */
    fun fetchUserDetails(
        userId: String,
        callback: (String, String, String, Boolean) -> Unit
    ) {
        userRef.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val fullName = snapshot.child("fullName").value.toString()
                    val username = snapshot.child("username").value.toString()
                    val profileImage = snapshot.child("profileImage").value.toString()
                    val isVerified = snapshot.child("isVerified").value.toString().toBoolean()
                    callback(fullName, username, profileImage, isVerified)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Log error
            }
        })
    }


    suspend fun getUserProfile(userId: String, param: (Any) -> Unit): ProfessionalUser? {
        return try {
            val snapshot = database.child("Users").child(userId).get().await()
            snapshot.getValue(ProfessionalUser::class.java)
        } catch (e: Exception) {
            null
        }
    }


    suspend fun updateUserProfileImage(userId: String, newImageUri: Uri): String? {
        return try {
            // Initialize Firebase Database
            val database = FirebaseDatabase.getInstance().reference
            database.keepSynced(true)

            // Get the current profile snapshot
            val snapshot = database.child("Users").child(userId).get().await()
            val currentProfileImageUrl = snapshot.child("profileImage").value as? String

            // Delete the previous image if it exists
            if (!currentProfileImageUrl.isNullOrEmpty()) {
                try {
                    val currentImageRef = FirebaseStorage.getInstance().getReferenceFromUrl(currentProfileImageUrl)
                    currentImageRef.delete().await()
                } catch (e: Exception) {
                    Log.e("FirebaseStorage", "Failed to delete previous image: ${e.localizedMessage}")
                }
            }

            // Upload the new image with a unique filename
            val storageRef = FirebaseStorage.getInstance().reference
            val newImageRef = storageRef.child("profileImages/$userId-${System.currentTimeMillis()}.jpg")
            newImageRef.putFile(newImageUri).await()

            // Retrieve the download URL of the new image
            val newImageUrl = newImageRef.downloadUrl.await().toString()

            // Update the user's profile image URL in Firebase Database
            database.child("Users").child(userId).child("profileImage").setValue(newImageUrl).await()

            Log.d("ProfileUpdate", "Profile image updated successfully: $newImageUrl")
            newImageUrl
        } catch (e: Exception) {
            Log.e("ProfileUpdate", "Error updating profile image: ${e.localizedMessage}")
            null
        }
    }


    suspend fun resetPassword(email: String): Boolean {
        return try {
            FirebaseAuth.getInstance().sendPasswordResetEmail(email).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun changeUserEmail(oldEmail: String, password: String, newEmail: String): Boolean {
        return try {
            // Get the currently authenticated user.
            val currentUser = firebaseAuth.currentUser ?: return false

            // Ensure that the current user's email matches the provided oldEmail.
            if (currentUser.email != oldEmail) {
                return false
            }

            // Retrieve the stored hashed password from the Realtime Database.
            val snapshot = database.child("Users").child(currentUser.uid).child("password").get().await()
            val storedHashedPassword = snapshot.value as? String ?: return false

            // Verify that the provided password matches the stored hashed password.
            val verificationResult = BCrypt.verifyer().verify(password.toCharArray(), storedHashedPassword)
            if (!verificationResult.verified) {
                return false
            }

            // ReAuthenticate the user with their current credentials.
            val credential = EmailAuthProvider.getCredential(oldEmail, password)
            currentUser.reauthenticate(credential).await()

            // Update the user's email in Firebase Authentication.
            @Suppress("DEPRECATION")
            currentUser.updateEmail(newEmail).await()

            // Optionally, send an email verification to the new email address.
            currentUser.sendEmailVerification().await()

            // Update the email field in the Firebase Realtime Database.
            database.child("Users").child(currentUser.uid).child("email").setValue(newEmail).await()

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }


}
