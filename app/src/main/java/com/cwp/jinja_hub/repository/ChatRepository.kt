package com.cwp.jinja_hub.repository

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.cwp.jinja_hub.model.ChatItem
import com.cwp.jinja_hub.model.Message
import com.cwp.jinja_hub.model.NormalUser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*

class ChatRepository(private val firebaseDatabase: FirebaseDatabase) {

    private val chats = MutableLiveData<List<ChatItem>?>()
    val firebaseUser: FirebaseUser = FirebaseAuth.getInstance().currentUser!!

    fun getChats(
        chatList: MutableList<ChatItem>,
        userList: MutableList<NormalUser>,
        fUser: FirebaseUser,
        callback: (userList: List<NormalUser>) -> Unit
    ) {
        val chatListRef = firebaseDatabase.getReference().child("ChatLists").child(fUser.uid)

        chatListRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    chatList.clear()
                    for (dataSnapshot in snapshot.children) {
                        val chat = dataSnapshot.getValue(ChatItem::class.java)
                        if (chat != null) {
                            chatList.add(chat)
                        }
                    }
                    getUsersInChatList(userList, chatList, callback)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ChatRepository", "Error fetching chat list: ${error.message}")
            }
        })
    }

    fun getUsersInChatList(userList: MutableList<NormalUser>, chatList: MutableList<ChatItem>, callback: (userList: List<NormalUser>) -> Unit) {
        val userRef = firebaseDatabase.getReference().child("Users")
        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    userList.clear()
                    for (dataSnapshot in snapshot.children) {
                        val user = dataSnapshot.getValue(NormalUser::class.java)
                        user?.let {
                            for (chat in chatList) {
                                if (user.userId == chat.id && user.userId != firebaseUser.uid) {
                                    userList.add(user)
                                }
                            }
                        }
                    }
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
        val lastMessagesMap = mutableMapOf<String, String>()

        for (chat in chatList) {
            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    // Filter messages between the current user and the receiver
                    val lastMessage = snapshot.children.filter { messageSnapshot ->
                        val senderId = messageSnapshot.child("senderId").value.toString()
                        val receiverId = messageSnapshot.child("receiverId").value.toString()

                        (senderId == currentUserId && receiverId == chat.userId) ||
                                (senderId == chat.userId && receiverId == currentUserId)
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



    fun getUnreadCount(userId: String, callback: (Int) -> Unit) {
        val userRef = firebaseDatabase.getReference().child("Chats")

        userRef.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    var unreadCount = 0
                    for (dataSnapshot in snapshot.children) {
                        val chat = dataSnapshot.getValue(Message::class.java)
                        if (chat != null) {
                            if (chat.senderId == userId && !chat.isSeen) {
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
        val updateData = mapOf(
            "lastMessage" to lastMessage,
            "time" to timestamp
        )
        chatReference.updateChildren(updateData)
    }
}
