package com.cwp.jinja_hub.model

data class LatestCommentModel(
    val senderId: String = "",
    val name: String = "",
    val commentId: String = "",
    val imageUrl: String = "",
    val text: String = "",
    val timestamp: Long = 0L
)
