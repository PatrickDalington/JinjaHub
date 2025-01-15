package com.cwp.jinja_hub.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

class OTPVerificationRepository {

    private val firebaseAuth = FirebaseAuth.getInstance()

    // Verify OTP entered by the user
    fun verifyOtp(verificationId: String, otp: String, onSuccess: (Boolean) -> Unit) {
        val credential = PhoneAuthProvider.getCredential(verificationId, otp)

        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onSuccess(true) // OTP verified successfully
                } else {
                    onSuccess(false) // OTP verification failed
                }
            }
    }
}
