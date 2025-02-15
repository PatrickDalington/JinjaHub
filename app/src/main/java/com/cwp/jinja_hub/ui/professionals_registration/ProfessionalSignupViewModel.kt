package com.cwp.jinja_hub.ui.professionals_registration

import android.app.Application
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.net.Uri
import android.util.Log
import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cwp.jinja_hub.helpers.SignUpResult
import com.cwp.jinja_hub.model.ProfessionalUser
import com.cwp.jinja_hub.repository.ProfessionalSignupRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// Data class for holding form data
data class ProfessionalSignupForm(
    var fullName: String = "",
    var firstName: String = "",
    var lastName: String = "",
    var username: String = "",
    var email: String = "",
    var password: String = "",
    var gender: String = "",
    var age: String = "",
    var address: String = "",
    var workplace: String = "",
    var medicalProfessional: String = "",
    var licence: String = "",
    var yearsOfWork: String = "",
    var consultationTime: String = "",
    var profileImage: String = ""
)

class ProfessionalSignupViewModel : ViewModel() {

    // Form data
    private val _formData = MutableStateFlow(ProfessionalSignupForm())
    val formData: StateFlow<ProfessionalSignupForm> get() = _formData

    // UI state flows
    private val _progress = MutableStateFlow(false)
    val progress: StateFlow<Boolean> get() = _progress

    private val _isSignupSuccessful = MutableStateFlow(false)
    val isSignupSuccessful: StateFlow<Boolean> get() = _isSignupSuccessful

    private val _userId = MutableStateFlow<String?>(null)
    val userId: StateFlow<String?> get() = _userId

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> get() = _errorMessage

    // Repository
    private val repository = ProfessionalSignupRepository()

    // Update form data dynamically
    fun updateForm(update: (ProfessionalSignupForm) -> Unit) {
        _formData.value = _formData.value.copy().apply(update)
    }

    // Clear all the shared preference values
    private fun clearSharedPreferences(context: Context) {
        val sharedPreferences = context.getSharedPreferences("user_preferences", MODE_PRIVATE)
        sharedPreferences.edit().clear().apply()
    }

    // Signup function with error handling
    fun signup(context: Context) {
        val form = _formData.value

        // Basic validation
        if (form.fullName.isBlank() || form.username.isBlank() || form.email.isBlank() || form.password.isBlank()) {
            _errorMessage.value = "Full Name, Username, Email, and Password are required!"
            return
        }

        _progress.value = true
        _errorMessage.value = null // Clear previous errors

        viewModelScope.launch {
            try {
                val result = repository.signUpUserWithEmailAndPassword(
                    form.fullName,
                    form.firstName,
                    form.lastName,
                    form.username,
                    form.email,
                    form.password,
                    form.gender,
                    form.age,
                    form.address,
                    form.workplace,
                    form.medicalProfessional,
                    form.licence,
                    form.yearsOfWork,
                    form.consultationTime,
                    form.profileImage
                )

                _progress.value = false
                when (result) {
                    is SignUpResult.Success -> {
                        _isSignupSuccessful.value = true
                        _userId.value = result.userId
                        repository.confirmUserEmail()
                        clearSharedPreferences(context)
                    }
                    is SignUpResult.Error -> {
                        _isSignupSuccessful.value = false
                        _errorMessage.value = result.errorMessage
                    }
                }
            } catch (e: Exception) {
                _progress.value = false
                _isSignupSuccessful.value = false
                _errorMessage.value = "An error occurred: ${e.localizedMessage}"
            }
        }
    }

    fun updateUserProfile(userId: String, updates: Map<String, Any?>) {
        _progress.value = true
        viewModelScope.launch {
            val success = repository.updateUserProfile(userId, updates)
            if (success) {
                _progress.value = false
            } else {
                // Handle failure, e.g., show an error message
                _progress.value = false
                _errorMessage.value = "Failed to update profile"
            }
        }
    }

    fun getUserProfile(userId: String, callback: (ProfessionalUser?) -> Unit) {
        _progress.value = true
        viewModelScope.launch {
            val profile = repository.getUserProfile(userId)
            if (profile != null) {
                _progress.value = false
                // Update the form data with the profile data
                callback(profile)
            } else {
                // Handle failure, e.g., show an error message
                _progress.value = false
                _errorMessage.value = "Failed to fetch profile"
            }
        }
    }

    suspend fun updateUserProfileImage(userId: String, newImageUri: Uri): String? {
        _progress.value = true
        // Validate the URI.
        if (newImageUri.toString().isBlank()) {
            _errorMessage.value = "Image URI is required!"
            _progress.value = false
            return null
        }
        return try {
            // Call the repository function which now returns the new image URL.
            val newUrl = repository.updateUserProfileImage(userId, newImageUri)
            if (newUrl != null) {
                _progress.value = false
                newUrl
            } else {
                _progress.value = false
                _errorMessage.value = "Failed to update profile image."
                null
            }
        } catch (e: Exception) {
            _progress.value = false
            _errorMessage.value = "Failed to update profile image: ${e.localizedMessage}"
            null
        }
    }

    // Reset password function

    fun resetPassword(email: String, callback: (Boolean, String?) -> Unit) {
        // Validate that the email is not blank.
        if (email.isBlank()) {
            callback(false, "Email is required.")
            return
        }

        // Validate email format.
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            callback(false, "Invalid email address.")
            return
        }

        _progress.value = true

        viewModelScope.launch {
            try {

                // If the email is registered, send a password reset email.
                FirebaseAuth.getInstance().sendPasswordResetEmail(email).await()
                _progress.value = false
                callback(true, "A password reset email has been sent. Please check your email for further instructions.")
            } catch (e: Exception) {
                _progress.value = false
                _errorMessage.value = e.localizedMessage ?: "Password reset failed."
                callback(false, _errorMessage.value)
            }
        }
    }

    fun changeUserEmail(oldEmail: String, password: String, newEmail: String, callback: (Boolean, String?) -> Unit) {
        // Validate that the email is not blank.
        if (newEmail.isBlank()) {
            callback(false, "Email is required.")
            return
        }
        // Validate email format.
        if (!Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
            callback(false, "Invalid email address.")
            return
        }
        _progress.value = true
        viewModelScope.launch {
            try {
                val success = repository.changeUserEmail(
                    oldEmail,
                    password ,
                    newEmail
                )
                if (success) {
                    _progress.value = false
                    callback(true, "Email changed successfully.")
                    Log.d("ProfessionalSignupViewModel", "Email changed successfully.")
                } else {
                    _progress.value = false
                    _errorMessage.value = "Failed to change email."
                    callback(false, _errorMessage.value)
                    Log.d("ProfessionalSignupViewModel", "Failed to change email.")
                }
            } catch (e: Exception) {
                _progress.value = false
                _errorMessage.value = e.localizedMessage ?: "Failed to change email."
                callback(false, _errorMessage.value)
            }
        }
    }
}
