package com.cwp.jinja_hub.model

import com.google.firebase.database.IgnoreExtraProperties


@IgnoreExtraProperties
data class ProfessionalUser(
    val userId: String = "",
    val fullName: String = "",
    val email: String = "",
    val password: String = "",
    val phoneNumber: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val specialty: String = "",
    val medicalProfessional: String = "",
    val profileImage: String = "",
    val isVerified: Boolean = false,
    val isProfessional: Boolean = true,
    val userType: String = "",
    val gender: String = "",
    val age: String = "",
    val address: String = "",
    val workplace: String = "",
    val licence: String = "",
    val consultationTime: String = "",
    val yearsOfWork: String = "",
    val rating: Double = 0.0,
    val username: String = "",
){

}
