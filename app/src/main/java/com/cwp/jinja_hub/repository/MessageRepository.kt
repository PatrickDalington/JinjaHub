package com.cwp.jinja_hub.repository

import android.net.Uri
import android.util.Log
import com.cwp.jinja_hub.model.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class MessageRepository(private val firebaseDatabase: FirebaseDatabase) {

    private val currentUser = FirebaseAuth.getInstance().currentUser
    private var seenListener: ValueEventListener? = null
    private val client = OkHttpClient()

    companion object {
        private const val CHATS_NODE = "Chats"
        private const val USERS_NODE = "Users"
        private const val CHAT_LISTS_NODE = "ChatLists"
        private const val CHAT_IMAGES_NODE = "ChatImages"
    }

    init {
        firebaseDatabase.reference.child(CHATS_NODE).keepSynced(true)
        firebaseDatabase.reference.child(USERS_NODE).keepSynced(true)
        firebaseDatabase.reference.child(CHAT_LISTS_NODE).keepSynced(true)
    }

    /**
     * Fetches chats and updates them via a callback.
     */
    fun getChats(receiverId: String, callback: (List<Message>) -> Unit) {
        val chatRef = firebaseDatabase.reference.child(CHATS_NODE)

        chatRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val messages = snapshot.children.mapNotNull { dataSnapshot ->
                    val message = dataSnapshot.getValue(Message::class.java)
                    message?.takeIf {
                        (it.senderId == receiverId && it.receiverId == currentUser?.uid) ||
                                (it.receiverId == receiverId && it.senderId == currentUser?.uid)
                    }
                }.sortedBy { it.timestamp }

                callback(messages)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("MessageRepository", "Error fetching messages: ${error.message}")
            }
        })
    }

    /**
     * Fetch receiver's info (name & profile image).
     */
    fun fetchReceiverInfo(receiverId: String, callback: (String, String) -> Unit) {
        val reference = firebaseDatabase.reference.child(USERS_NODE).child(receiverId)

        reference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val name = snapshot.child("fullName").value?.toString().orEmpty()
                val profileImage = snapshot.child("profileImage").value?.toString().orEmpty()
                callback(name, profileImage)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("MessageRepository", "Error fetching receiver info: ${error.message}")
            }
        })
    }

    /**
     * Sends a text message.
     */
    fun sendMessageToUser(senderId: String, receiverId: String, message: Message, callback: (Boolean) -> Unit) {
        val messageKey = firebaseDatabase.reference.child(CHATS_NODE).push().key ?: return
        val messageData = message.copy(messageId = messageKey)

        firebaseDatabase.reference.child(CHATS_NODE).child(messageKey)
            .setValue(messageData)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    updateChatLists(senderId, receiverId, message.timestamp)
                    callback(true)
                } else {
                    Log.e("MessageRepository", "Error sending message: ${task.exception?.message}")
                    callback(false)
                }
            }
    }

    /**
     * Uploads an image and sends the message.
     */
    suspend fun sendImageToStorage(senderId: String, receiverId: String, imageUri: Uri, callback: (Boolean) -> Unit) {
        val storageRef = FirebaseStorage.getInstance().reference.child(CHAT_IMAGES_NODE)
        val databaseRef = firebaseDatabase.reference

        try {
            val messageId = databaseRef.push().key ?: throw Exception("Failed to generate message ID")
            val filePath = storageRef.child("$messageId.jpg")
            val downloadUrl = filePath.putFile(imageUri).await().storage.downloadUrl.await()

            val message = Message(
                messageId = messageId,
                chatId = messageId,
                senderId = senderId,
                receiverId = receiverId,
                message = "sent an image",
                messageType = "Image",
                timestamp = System.currentTimeMillis(),
                status = "Sent",
                mediaUrl = downloadUrl.toString(),
                isSeen = false
            )

            sendMessageToUser(senderId, receiverId, message, callback)

        } catch (e: Exception) {
            Log.e("MessageRepository", "Error uploading image: ${e.message}")
            callback(false)
        }
    }

    /**
     * Updates seen messages.
     */
    fun seenMessage(userId: String, callback: (ValueEventListener) -> Unit) {
        val reference = firebaseDatabase.reference.child(CHATS_NODE)

        seenListener = reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (dataSnapshot in snapshot.children) {
                    val message = dataSnapshot.getValue(Message::class.java)
                    if (message != null) {
                        if (message.receiverId == currentUser?.uid && message.senderId == userId && !message.isSeen) {
                            message.isSeen = true
                            dataSnapshot.ref.setValue(message)
                        }
                    }
                }
                seenListener?.let { callback(it) }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("MessageRepository", "Failed to update seen status: ${error.message}")
            }
        })
    }



    /**
     * Updates chat lists for both sender and receiver.
     */
    private fun updateChatLists(senderId: String, receiverId: String, timestamp: Long) {
        val senderChatListRef = firebaseDatabase.reference.child(CHAT_LISTS_NODE).child(senderId).child(receiverId)
        val receiverChatListRef = firebaseDatabase.reference.child(CHAT_LISTS_NODE).child(receiverId).child(senderId)

        senderChatListRef.child("id").setValue(receiverId)
        senderChatListRef.child("lastMessageTimestamp").setValue(timestamp)
        receiverChatListRef.child("id").setValue(senderId)
        receiverChatListRef.child("lastMessageTimestamp").setValue(timestamp)
    }

    suspend fun getReceiverFCMToken(receiverId: String): String {
        return try {
            val snapshot = firebaseDatabase.reference.child("Users").child(receiverId).child("fcmToken").get().await()
            snapshot.getValue(String::class.java) ?: ""
        } catch (e: Exception) {
            Log.e("MessageRepository", "Error fetching FCM token: ${e.message}")
            ""
        }
    }
    /**
     * Triggers push notification via API.
     */
    suspend fun triggerNotification(
        token: String,
        title: String,
        body: String,
        data: Map<String, String>? = null
    ) {
        withContext(Dispatchers.IO) {
            try {
                Log.d("TriggerNotification", "Sending notification to token: $token")

                val messageJson = JSONObject().apply {
                    put("token", token)
                    put("title", title)
                    put("body", body)
                    if (!data.isNullOrEmpty()) put("data", JSONObject(data))
                }

                val requestBody = messageJson.toString().toRequestBody("application/json".toMediaTypeOrNull())
                val request = Request.Builder()
                    .url("https://1913-84-239-7-155.ngrok-free.app/send-notification")
                    .post(requestBody)
                    .header("Content-Type", "application/json")
                    .build()

                client.newCall(request).execute().use { response ->
                    val responseBody = response.body?.string()
                    if (!response.isSuccessful) {
                        Log.e("TriggerNotification", "Error: ${response.message}, Code: ${response.code}, Body: $responseBody")
                    } else {
                        Log.d("TriggerNotification", "Notification sent successfully: $responseBody")
                    }
                }
            } catch (e: Exception) {
                Log.e("TriggerNotification", "Error sending notification: ${e.message}", e)
            }
        }
    }

    fun getUnreadCount(chatId: String, userId: String, callback: (Int) -> Unit) {
        val userRef = firebaseDatabase.reference.child("Chats").child(userId).child(chatId).child("unreadCount")
        userRef.get().addOnSuccessListener { snapshot ->
            val count = snapshot.getValue(Int::class.java) ?: 0
            callback(count)
        }.addOnFailureListener {
            Log.e("MessageRepository", "Failed to fetch unread count: ${it.message}")
        }
    }

    suspend fun checkFirstTimeChat(senderId: String, receiverId: String): Boolean {
        return try {
            val snapshot = firebaseDatabase.reference.child("ChatLists").child(senderId).child(receiverId).get().await()
            !snapshot.exists() // If it doesn't exist, it's a first-time chat
        } catch (e: Exception) {
            Log.e("MessageRepository", "Error checking first-time chat: ${e.message}")
            false
        }
    }

    fun clearListeners() {
        seenListener?.let {
            firebaseDatabase.reference.child("Chats").removeEventListener(it)
            seenListener = null
        }
    }
}
