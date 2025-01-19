package com.cwp.jinja_hub.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.cwp.jinja_hub.model.ChatItem
import com.cwp.jinja_hub.model.Message
import com.cwp.jinja_hub.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import kotlinx.coroutines.tasks.await

class ChatRepository(private val firebaseDatabase: FirebaseDatabase) {

    private val chats = MutableLiveData<List<ChatItem>?>()
    val firebaseUser: FirebaseUser = FirebaseAuth.getInstance().currentUser!!

    fun getChats(
        chatList: MutableList<ChatItem>,
        userList: MutableList<User>,
        fUser: FirebaseUser,
        callback: (userList: List<User>) -> Unit
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

    fun getUsersInChatList(userList: MutableList<User>, chatList: MutableList<ChatItem>, callback: (userList: List<User>) -> Unit) {
        val userRef = firebaseDatabase.getReference().child("Users")
        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    userList.clear()
                    for (dataSnapshot in snapshot.children) {
                        val user = dataSnapshot.getValue(User::class.java)
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
