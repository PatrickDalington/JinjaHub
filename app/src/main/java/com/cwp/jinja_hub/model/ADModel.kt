package com.cwp.jinja_hub.model

data class ADModel(
    var posterId: String = "",
    var adId: String? = null,
    var adType: String = "",
    var description: String = "",
    var city: String = "",
    var state: String = "",
    var country: String = "",
    var amount: String = "",
    var phone: String = "",
    var productName: String = "",
    var timestamp: Long = System.currentTimeMillis(),
    var mediaUrl: List<String>? = emptyList(), // List of image URLs
    var posterName: String = "",
    var posterUsername: String = "",
    var posterProfileImage: String = "",
) {

}
