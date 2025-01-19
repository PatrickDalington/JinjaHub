package com.cwp.jinja_hub.model

import com.google.firebase.database.IgnoreExtraProperties


@IgnoreExtraProperties
data class User(
    val userId: String = "",
    val name: String = "",
    val fullName: String = "",
    val email: String = "",
    val password: String = "",
    val phoneNumber: String = "",
    val address: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val specialty: String = "",
    val profileImage: String = ""
)
