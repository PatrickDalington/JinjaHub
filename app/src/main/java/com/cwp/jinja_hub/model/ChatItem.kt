package com.cwp.jinja_hub.model

data class ChatItem(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val lastMessage: String = "",
    val time: Long = 0L,
    val profileImageUrl: String = "",
    val unreadCount: Int = 0
) {
    // No-argument constructor required by Firebase
    constructor() : this("", "", "", "", 0L, "", 0)
}