package com.cwp.jinja_hub.model

data class ChatItem(
    val chatId: String,
    val userId: String,
    val userName: String,
    val lastMessage: String,
    val time: String,
    val profileImageUrl: String,
    val unreadCount: Int
)
