package com.cwp.jinja_hub.repository


import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

class ContactUsRepository {

    // Get a reference to the Firebase Realtime Database.
    private val database = FirebaseDatabase.getInstance().reference

    /**
     * Sends a user message to the Firebase Database.
     *
     * @param userId The ID of the user sending the message.
     * @param subject The subject of the message.
     * @param message The message content.
     * @return True if the message was sent successfully; false otherwise.
     */
    suspend fun sendUsMessage(userId: String, subject: String, message: String): Boolean {
        val key = database.child("ContactUs").push().key ?: return false
        return try {
            // Prepare a map containing the message data.
            val messageData = mapOf(
                "userId" to userId,
                "subject" to subject,
                "message" to message,
                "id" to key,
                "timestamp" to System.currentTimeMillis()
            )
            // Push the message under the "ContactUs" node.
            database.child("ContactUs").child(userId).child(key).setValue(messageData).await()
            true
        } catch (e: Exception) {
            // Optionally, log e.message or e.printStackTrace() here.
            false
        }
    }
}
