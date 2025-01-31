package com.cwp.jinja_hub.repository

import android.net.Uri
import android.util.Log
import com.cwp.jinja_hub.model.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

class MessageRepository(private val firebaseDatabase: FirebaseDatabase) {

    private val currentUser = FirebaseAuth.getInstance().currentUser
    private var seenListener: ValueEventListener? = null

    companion object {
        private const val CHATS_NODE = "Chats"
        private const val USERS_NODE = "Users"
        private const val CHAT_LISTS_NODE = "ChatLists"
        private const val UNREAD_COUNT_NODE = "unreadCount"
        private const val CHAT_IMAGES_NODE = "Chat Images"
    }

    /**
     * Fetches chats and returns them via a callback.
     */
    fun getChats(id: String, callback: (List<Message>) -> Unit) {
        val chatRef = firebaseDatabase.reference.child(CHATS_NODE)

        chatRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Collect messages where senderId or receiverId matches the given id
                val messages = snapshot.children.mapNotNull { dataSnapshot ->
                    val message = dataSnapshot.getValue(Message::class.java)
                    if (message != null && (message.senderId == id || message.receiverId == id)) {
                        // Filter messages based on id's role
                        if ((message.senderId == id && message.receiverId == currentUser!!.uid) ||
                            (message.receiverId == id && message.senderId == currentUser!!.uid)) {
                            message
                        } else {
                            null
                        }
                    } else {
                        null
                    }
                }

                // Pass the filtered list to the callback
                callback(messages)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("MessageRepository", "Failed to fetch messages: ${error.message}")
            }
        })
    }



    /**
     * Updates the last message for a given chat.
     */
    private suspend fun updateLastMessage(chatId: String, message: Message) {
        val userId = currentUser?.uid ?: return
        val chatRef = firebaseDatabase.reference.child(CHATS_NODE).child(userId).child(chatId)

        try {
            chatRef.child("lastMessage").setValue(message).await()
        } catch (e: Exception) {
            Log.e("MessageRepository", "Failed to update last message: ${e.message}")
        }
    }

    /**
     * Fetches the unread message count for a given chat and user.
     */
    fun fetchUnreadCount(chatId: String, userId: String, callback: (Int) -> Unit) {
        val user = currentUser?.uid ?: return

        firebaseDatabase.reference.child(CHATS_NODE).child(user).child(chatId)
            .child(UNREAD_COUNT_NODE).child(userId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val count = snapshot.getValue(Int::class.java) ?: 0
                    callback(count)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("MessageRepository", "Failed to fetch unread count: ${error.message}")
                }
            })
    }

    /**
     * Fetches the receiver's information (name and profile image).
     */
    fun fetchReceiverInfo(receiverId: String, callback: (String, String) -> Unit) {
        val reference = firebaseDatabase.reference.child(USERS_NODE).child(receiverId)

        reference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val name = snapshot.child("fullName").value?.toString().orEmpty()
                    val profileImage = snapshot.child("profileImage").value?.toString().orEmpty()
                    callback(name, profileImage)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("MessageRepository", "Failed to fetch receiver info: ${error.message}")
            }
        })
    }

    /**
     * Uploads an image to Firebase Storage and sends the image message.
     */
    suspend fun sendImageToStorage(senderId: String, receiverId: String, imageUri: Uri) {
        val userId = currentUser?.uid ?: return
        val storageRef = FirebaseStorage.getInstance().reference.child(CHAT_IMAGES_NODE)
        val databaseRef = firebaseDatabase.reference

        try {
            val messageId =
                databaseRef.push().key ?: throw Exception("Failed to generate message ID")
            val filePath = storageRef.child("$messageId.jpg")
            val downloadUrl = filePath.putFile(imageUri).await().storage.downloadUrl.await()

            val message = Message(
                messageId = messageId,
                senderId = senderId,
                receiverId = receiverId,
                message = "sent you an image",
                messageType = "Image",
                timestamp = System.currentTimeMillis(),
                status = "Sent",
                mediaUrl = downloadUrl.toString(),
                isSeen = false
            )

            sendMessageToUser(senderId, receiverId, message)
        } catch (e: Exception) {
            Log.e("MessageRepository", "Failed to upload image: ${e.message}")
        }
    }

    /**
     * Sends a text message to a user.
     */
    fun sendMessageToUser(senderId: String, receiverId: String, message: Message) {
        val reference = firebaseDatabase.reference.child(CHATS_NODE)
        val messageKey = reference.push().key ?: return

        val messageMap = mapOf(
            "senderId" to senderId,
            "receiverId" to receiverId,
            "message" to message.message,
            "messageId" to messageKey,
            "timestamp" to message.timestamp,
            "isSeen" to message.isSeen,
            "messageType" to message.messageType,
            "status" to message.status,
            "mediaUrl" to message.mediaUrl
        )

        reference.child(messageKey).setValue(messageMap).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                updateChatLists(senderId, receiverId)
            } else {
                Log.e("MessageRepository", "Failed to send message: ${task.exception?.message}")
            }
        }
    }

    /**
     * Handles updating seen messages.
     */
    fun seenMessage(userId: String, callback: (ValueEventListener) -> Unit) {
        val reference = firebaseDatabase.reference.child(CHATS_NODE)

        seenListener = reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (dataSnapshot in snapshot.children) {
                    val message = dataSnapshot.getValue(Message::class.java)
                    if (message != null) {
                        if (message.senderId == userId || message.receiverId == currentUser?.uid && !message.isSeen) {
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
     * Removes listeners to prevent memory leaks.
     */
    fun removeListeners() {
        seenListener?.let {
            firebaseDatabase.reference.child(CHATS_NODE).removeEventListener(it)
            seenListener = null
        }
    }

    /**
     * Updates chat lists for sender and receiver.
     */
    private fun updateChatLists(senderId: String, receiverId: String) {
        val senderChatListRef = firebaseDatabase.reference.child(CHAT_LISTS_NODE).child(senderId).child(receiverId)
        val receiverChatListRef = firebaseDatabase.reference.child(CHAT_LISTS_NODE).child(receiverId).child(senderId)

        senderChatListRef.child("id").setValue(receiverId)
        receiverChatListRef.child("id").setValue(senderId)
    }
}
