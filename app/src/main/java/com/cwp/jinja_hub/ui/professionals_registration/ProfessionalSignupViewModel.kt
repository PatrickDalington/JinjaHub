package com.cwp.jinja_hub.ui.professionals_registration

import android.app.Application
import android.content.Context
import android.content.Context.MODE_PRIVATE
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cwp.jinja_hub.repository.ProfessionalSignupRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

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
    var profession: String = "",
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
    fun clearSharedPreferences(context: Context) {
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
                val userId = repository.signUpUserWithEmailAndPassword(
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
                    form.profession,
                    form.licence,
                    form.yearsOfWork,
                    form.consultationTime,
                    form.profileImage
                )

                _progress.value = false
                _isSignupSuccessful.value = userId != null
                _userId.value = userId

                if (userId != null) {
                    repository.confirmUserEmail()
                    clearSharedPreferences(context)
                } else {
                    _errorMessage.value = "Signup failed. Please try again."
                }
            } catch (e: Exception) {
                _progress.value = false
                _isSignupSuccessful.value = false
                _errorMessage.value = "An error occurred: ${e.localizedMessage}"
            }
        }
    }
}
