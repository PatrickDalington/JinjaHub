package com.cwp.jinja_hub.repository

import android.util.Log
import com.cwp.jinja_hub.model.Message
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MessageRepository(private val firebaseDatabase: FirebaseDatabase) {

    fun getChats(chatId: String, callback: (List<Message>) -> Unit) {
        val chatRef = firebaseDatabase.reference.child("chats").child(chatId).child("messages")
        chatRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val messages = mutableListOf<Message>()
                for (messageSnapshot in snapshot.children) {
                    val message = messageSnapshot.getValue(Message::class.java)
                    if (message != null) messages.add(message)
                }
                callback(messages)
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun sendMessage(chatId: String, message: Message) {
        val chatRef = firebaseDatabase.reference.child("chats").child(chatId).child("messages")
        chatRef.push().setValue(message)
        updateLastMessage(chatId, message)
    }

    private fun updateLastMessage(chatId: String, message: Message) {
        val chatRef = firebaseDatabase.reference.child("chats").child(chatId).child("lastMessage")
        chatRef.setValue(message)
    }

    fun fetchUnreadCount(chatId: String, userId: String, callback: (Int) -> Unit) {
        val unreadCountRef = firebaseDatabase.reference.child("chats").child(chatId).child("unreadCount").child(userId)
        unreadCountRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val count = snapshot.getValue(Int::class.java) ?: 0
                callback(count)
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
}