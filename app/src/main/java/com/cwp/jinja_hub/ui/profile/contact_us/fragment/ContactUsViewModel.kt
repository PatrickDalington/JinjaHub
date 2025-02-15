package com.cwp.jinja_hub.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cwp.jinja_hub.repository.ContactUsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ContactUsViewModel : ViewModel() {

    // StateFlow for loading state.
    private val _progress = MutableStateFlow(false)
    val progress: StateFlow<Boolean> get() = _progress

    // StateFlow for error messages.
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> get() = _errorMessage

    // Instance of your repository.
    private val repository = ContactUsRepository()

    /**
     * Sends a user message to the Firebase Database.
     *
     * @param userId The ID of the user sending the message.
     * @param subject The subject of the message.
     * @param message The message content.
     * @param callback A lambda that receives true if sending was successful, false otherwise.
     */
    fun sendMessage(userId: String, subject: String, message: String, callback: (Boolean) -> Unit) {
        // Set loading state and clear any previous error.
        _progress.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            try {
                // Call the repository function.
                val success = repository.sendUsMessage(userId, subject, message)
                _progress.value = false
                if (success) {
                    callback(true)
                } else {
                    _errorMessage.value = "Failed to send message"
                    callback(false)
                }
            } catch (e: Exception) {
                _progress.value = false
                _errorMessage.value = e.localizedMessage ?: "An error occurred"
                callback(false)
            }
        }
    }
}
