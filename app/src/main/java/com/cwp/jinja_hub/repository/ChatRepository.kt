package com.cwp.jinja_hub.repository

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.cwp.jinja_hub.model.ChatItem
import com.cwp.jinja_hub.model.Message
import com.cwp.jinja_hub.model.NormalUser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import java.util.concurrent.atomic.AtomicInteger

class ChatRepository(private val firebaseDatabase: FirebaseDatabase) {

    private val chats = MutableLiveData<List<ChatItem>?>()
    val firebaseUser: FirebaseUser = FirebaseAuth.getInstance().currentUser!!

    fun getChats(
        chatList: MutableList<NormalUser>,
        fUser1: MutableList<Any>,
        fUser: FirebaseUser,
        callback: (userList: List<NormalUser>) -> Unit
    ) {
        val chatListRef = firebaseDatabase.getReference().child("ChatLists").child(fUser.uid)
        val usersRef = firebaseDatabase.getReference().child("Users")
        val chatsRef = firebaseDatabase.getReference().child("Chats")
        chatListRef.keepSynced(true)
        usersRef.keepSynced(true)
        chatsRef.keepSynced(true)

        chatListRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    chatList.clear()
                    val processedCount = AtomicInteger(0)

                    for (dataSnapshot in snapshot.children) {
                        val receiverId = dataSnapshot.key ?: ""
                        val lastMessageTimestamp = dataSnapshot.child("lastMessageTimestamp").value as? Long ?: 0L

                        usersRef.child(receiverId).addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(userSnapshot: DataSnapshot) {
                                val user = userSnapshot.getValue(NormalUser::class.java)
                                if (user != null) {
                                    fetchLastMessage(fUser.uid, receiverId) { lastMessage ->
                                        val updatedUser = user.copy(timestamp = lastMessageTimestamp)
                                        updatedUser.lastMessage = lastMessage
                                        chatList.add(updatedUser)

                                        processedCount.incrementAndGet()
                                        if (processedCount.get().toLong() == snapshot.childrenCount) {
                                            val sortedList = chatList.sortedByDescending { it.timestamp }
                                            callback(sortedList)
                                        }
                                    }
                                } else {
                                    processedCount.incrementAndGet()
                                    if (processedCount.get().toLong() == snapshot.childrenCount) {
                                        val sortedList = chatList.sortedByDescending { it.timestamp }
                                        callback(sortedList)
                                    }
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Log.e("ChatRepository", "Error fetching user details: ${error.message}")
                                processedCount.incrementAndGet()
                                if (processedCount.get().toLong() == snapshot.childrenCount) {
                                    val sortedList = chatList.sortedByDescending { it.timestamp }
                                    callback(sortedList)
                                }
                            }
                        })
                    }
                } else {
                    callback(chatList)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ChatRepository", "Error fetching chat list: ${error.message}")
            }
        })
    }

    private fun fetchLastMessage(senderId: String, receiverId: String, callback: (String) -> Unit) {
        val chatsRef = firebaseDatabase.getReference().child("Chats")
        val chatId = if (senderId < receiverId) {
            "${senderId}_${receiverId}"
        } else {
            "${receiverId}_${senderId}"
        }

        chatsRef.child(chatId).orderByChild("timestamp").limitToLast(1)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val lastMessage = snapshot.children.firstOrNull()?.child("message")?.value?.toString() ?: "No messages yet"
                    callback(lastMessage)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ChatRepository", "Error fetching last message: ${error.message}")
                    callback("Error fetching messages")
                }
            })
    }


    fun getUsersInChatList(userList: MutableList<NormalUser>, chatList: MutableList<ChatItem>, callback: (userList: List<NormalUser>) -> Unit) {
        val userRef = firebaseDatabase.getReference().child("Users")
        userRef.keepSynced(true)

        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    userList.clear()
                    for (dataSnapshot in snapshot.children) {
                        val user = dataSnapshot.getValue(NormalUser::class.java)
                        Log.d("ChatRepository", "User: $user")
                        user?.let {
                            for (chat in chatList) {
                                if (user.userId == chat.id && user.userId != firebaseUser.uid) {
                                    userList.add(user)
                                }
                            }
                        }
                    }
                    // Sort the list locally if needed
                    userList.sortByDescending { it.timestamp }

                    callback(userList)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ChatRepository", "Error fetching users: ${error.message}")
            }
        })
    }

    fun getChatTime(callback: (Long) -> Unit) {
        val chatRef = firebaseDatabase.reference.child("Chats")
        chatRef.keepSynced(true)

        chatRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    var time = 0L
                    for (dataSnapshot in snapshot.children) {
                        val chat = dataSnapshot.getValue(Message::class.java)
                        if (chat != null) {
                            time = chat.timestamp
                        }
                    }
                    callback(time)
                }
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    // Get last message on a chat
    fun getLastMessage(callback: (String) -> Unit) {
        val chatRef = firebaseDatabase.reference.child("Chats")

        chatRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    var lastMessage = ""
                    for (dataSnapshot in snapshot.children) {
                        val chat = dataSnapshot.getValue(Message::class.java)
                        if (chat != null) {
                            lastMessage = chat.message
                            break
                        }
                    }
                    callback(lastMessage)
                }
            }
                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
        })
    }

    // Get last message for each chat in the chat list
    fun getLastMessageForChatList(
        chatList: List<NormalUser>,
        currentUserId: String,
        callback: (Map<String, String>) -> Unit
    ) {
        val database = FirebaseDatabase.getInstance().reference.child("Chats")
        database.keepSynced(true)

        val lastMessagesMap = mutableMapOf<String, String>()

        for (chat in chatList) {
            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    // Filter messages between the current user and the receiver
                    val lastMessage = snapshot.children.filter { messageSnapshot ->
                        val senderId = messageSnapshot.child("senderId").value.toString()
                        val receiverId = messageSnapshot.child("receiverId").value.toString()


                        ((senderId == chat.userId && receiverId == currentUserId) ||
                                (receiverId == chat.userId && senderId == currentUserId))
                    }.maxByOrNull { messageSnapshot ->
                        // Get the most recent message by timestamp
                        messageSnapshot.child("timestamp").value.toString().toLong()
                    }?.child("message")?.value.toString()

                    // Update the last message map
                    lastMessagesMap[chat.userId] = lastMessage ?: "No messages yet"
                    callback(lastMessagesMap)
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                    lastMessagesMap[chat.userId] = "Error fetching messages"
                    callback(lastMessagesMap)
                }
            }

            // Add the listener for real-time updates
            database.addValueEventListener(listener)
        }
    }


    fun getLastMessageForEachUser(
        userList: List<NormalUser>,
        currentUserId: String,
        callback: (Map<String, String>) -> Unit
    ) {
        val database = firebaseDatabase.getReference().child("Chats")
        database.keepSynced(true)

        val lastMessagesMap = mutableMapOf<String, String>()

        for (user in userList) {
            val receiverId = user.userId

            // Query to find the last message between current user and the other user
            database.orderByChild("timestamp")
                .addValueEventListener(object : ValueEventListener { // Use addValueEventListener
                    override fun onDataChange(snapshot: DataSnapshot) {
                        var lastMessage: String? = null

                        // Iterate through the snapshot and find the latest message for this user
                        for (messageSnapshot in snapshot.children) {
                            val sender = messageSnapshot.child("senderId").value.toString()
                            val receiver = messageSnapshot.child("receiverId").value.toString()

                            if ((sender == currentUserId && receiver == receiverId) ||
                                (sender == receiverId && receiver == currentUserId)) {
                                lastMessage = messageSnapshot.child("message").value.toString()
                            }
                        }

                        // Update the map with the last message (or "No messages yet")
                        lastMessagesMap[receiverId] = lastMessage ?: "No messages yet"

                        // Call the callback immediately with the updated map
                        callback(lastMessagesMap)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("ChatRepository", "Error fetching messages: ${error.message}")
                        lastMessagesMap[receiverId] = "Error fetching messages"

                        // Call the callback immediately with the updated map
                        callback(lastMessagesMap)
                    }
                })
        }
    }


    fun getUnreadCount(userId: String, callback: (Int) -> Unit) {
        val userRef = firebaseDatabase.getReference().child("Chats")

        userRef.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    var unreadCount = 0
                    for (dataSnapshot in snapshot.children) {
                        val chat = dataSnapshot.getValue(Message::class.java)
                        if (chat != null) {
                            if (chat.senderId == userId && chat.receiverId == firebaseUser.uid && !chat.isSeen) {
                                unreadCount += 1
                            }
                        }
                    }
                    callback(unreadCount)
                }
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")

            }
        })
    }


    fun updateLastMessage(chatId: String, lastMessage: String, timestamp: Long) {
        val chatReference = firebaseDatabase.getReference("chats").child(chatId)
        chatReference.keepSynced(true)

        val updateData = mapOf(
            "lastMessage" to lastMessage,
            "time" to timestamp
        )
        chatReference.updateChildren(updateData)
    }
}
