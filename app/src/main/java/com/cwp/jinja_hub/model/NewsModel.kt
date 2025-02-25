package com.cwp.jinja_hub.com.cwp.jinja_hub.model

data class NewsModel(
    var posterId: String = "",
    var header: String = "",
    var newsId: String = "",
    var vidLink: String = "",
    var content: String = "",
    var timestamp: Long = System.currentTimeMillis(),
    var mediaUrl: List<String>? = emptyList() // List of image URLs
) {

}
