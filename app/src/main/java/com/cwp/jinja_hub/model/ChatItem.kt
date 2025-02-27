package com.cwp.jinja_hub.model

data class ChatItem(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val lastMessage: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val profileImageUrl: String = "",
    val unreadCount: Int = 0
) {
}