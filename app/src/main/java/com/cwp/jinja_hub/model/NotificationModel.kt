package com.cwp.jinja_hub.model

import com.google.firebase.database.PropertyName

data class NotificationModel(
    var posterId: String = "",
    var id: String = "",
    var content: String = "",
    @get:PropertyName("isRead") @set:PropertyName("isRead")
    var isRead: Boolean = false,
    var timestamp: Long = System.currentTimeMillis(),
) {

}
