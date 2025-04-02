package com.cwp.jinja_hub.model

data class Message(
    val messageId: String = "", // Unique ID for each message
    val chatId: String = "", // Chat ID to associate the message with a chat
    val senderId: String = "", // User ID of the sender
    val receiverId: String = "", // User ID of the receiver
    val message: String = "", // The message content (text)
    val messageType: String = "", // Type of the message (text, image, video, etc.)
    val timestamp: Long = System.currentTimeMillis(), // Time when the message was sent
    val status: String = "", // Status of the message (sent, delivered, read)
    val mediaUrl: List<String> = emptyList(),// URL to media file if the message is of type MEDIA
    var isSeen: Boolean = false // To track if the message has been seen by the receiver
)
