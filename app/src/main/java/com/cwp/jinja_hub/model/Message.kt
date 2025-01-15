package com.cwp.jinja_hub.model

data class Message(
    val sender: String = "",
    val message: String = "",
    val timestamp: Long = 0,
    val seen: Boolean = false,
    val messageId: String = "",
    val senderId: String = "",
    val text: String = "",
    val read: Boolean = false
)