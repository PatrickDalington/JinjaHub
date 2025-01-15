package com.cwp.jinja_hub.repository

import android.util.Log
import com.cwp.jinja_hub.model.ChatItem
import com.cwp.jinja_hub.model.Message
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


class ChatRepository(private val firebaseDatabase: FirebaseDatabase) {

    fun getDummyChats(): List<ChatItem> {
        return listOf(
            ChatItem(
                chatId = "1",
                userId = "1",
                userName = "John Doe",
                lastMessage = "Hey, how are you?",
                time = "12:45 PM",
                profileImageUrl = "https://example.com/john_doe.jpg",
                unreadCount = 3
            ),
            ChatItem(
                chatId = "2",
                userId = "2",
                userName = "Jane Smith",
                lastMessage = "Let's catch up soon!",
                time = "11:30 AM",
                profileImageUrl = "https://example.com/jane_smith.jpg",
                unreadCount = 0
            ),
            ChatItem(
                chatId = "3",
                userId = "3",
                userName = "Emily Johnson",
                lastMessage = "Meeting at 4 PM?",
                time = "9:15 AM",
                profileImageUrl = "https://example.com/emily_johnson.jpg",
                unreadCount = 1
            )
        )
    }
}