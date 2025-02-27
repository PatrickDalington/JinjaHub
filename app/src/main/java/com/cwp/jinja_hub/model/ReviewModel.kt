package com.cwp.jinja_hub.model

data class ReviewModel(
    var posterId: String = "",
    var posterName: String = "",
    var posterUsername: String = "",
    var posterProfileImage: String = "",
    //create a nullable string variable called reviewId
    var reviewId: String? = null,
    var vidLink: String = "",
    var description: String = "",
    var rating: Float = 0.0f,
    var timestamp: Long = System.currentTimeMillis(),
    var mediaUrl: List<String>? = emptyList() // List of image URLs
) {

}
