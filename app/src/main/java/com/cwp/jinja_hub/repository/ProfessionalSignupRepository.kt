package com.cwp.jinja_hub.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import at.favre.lib.crypto.bcrypt.BCrypt // Import BCrypt for hashing

class ProfessionalSignupRepository {
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().reference

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
        profession: String,
        licence: String,
        yearsOfWork: String,
        consultationTime: String,
        profileImage: String?
    ): String? {
        return try {
            // Sign up the user in Firebase Authentication
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val userId = result.user?.uid ?: return "No user ID found"

            // Hash the password before storing
            val hashedPassword = BCrypt.withDefaults().hashToString(12, password.toCharArray())  // 12 is the cost factor

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


            // Save user details in Firebase Database
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
                "profession" to profession,
                "licence" to licence,
                "yearsOfWork" to yearsOfWork,
                "consultationTime" to consultationTime,
                if (gender.equals("male", ignoreCase = true)) "profileImage" to maleAvatar else "profileImage" to femaleAvatar,
                "isProfessional" to true,
                "userId" to userId,
                "isVerified" to false,

            )

            database.child("Users").child(userId).setValue(userMap).await()
            userId
        } catch (e: Exception) {
            e.message ?: "An unexpected error occurred"
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
