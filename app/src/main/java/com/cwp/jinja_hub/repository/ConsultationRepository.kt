package com.cwp.jinja_hub.repository

import ConsultationModel
import android.util.Log
import com.cwp.jinja_hub.model.ProfessionalUser
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class ConsultationRepository {

    private val database = Firebase.database.reference
    private val userRef = FirebaseDatabase.getInstance().getReference("Users")

    // Function to load professionals from Firebase database

    suspend fun loadProfessionalsFromFirebase(category: String): List<ConsultationModel> {
        return try {
            val querySnapshot = userRef
                .orderByChild("isProfessional")
                .equalTo(true)  // Fetch only professional users
                .get()
                .await()

            val professionals = querySnapshot.children.mapNotNull { snapshot ->
                val user = snapshot.getValue(ProfessionalUser::class.java)
                val isVerified = snapshot.child("isVerified").value?.toString()?.toBoolean() ?: false
                val isProfessional = snapshot.child("isProfessional").value?.toString()?.toBoolean() ?: false

                if (user != null  && user.profession.equals(category, ignoreCase = true)) {
                    ConsultationModel(
                        user.userId,
                        user.fullName,
                        user.profession,
                        user.profileImage
                    ).also {
                        Log.d("ConsultationRepository", "Fetched Professional: ${user.fullName}")
                    }
                } else {
                    null
                }
            }

            professionals
        } catch (e: Exception) {
            Log.e("ConsultationRepository", "Error fetching professionals: ${e.message}")
            emptyList()
        }
    }

    fun getSpecialistUserInfo(userId: String, callback: (ProfessionalUser?) -> Unit) {
        userRef.child(userId).get().addOnSuccessListener { dataSnapshot ->
            if (dataSnapshot.exists()) {
                val user = dataSnapshot.getValue(ProfessionalUser::class.java)
                callback(user)
            } else {
                callback(null) // User not found
            }
        }.addOnFailureListener {
            callback(null) // Handle database failure
        }
    }



}
