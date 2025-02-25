package com.cwp.jinja_hub.ui.welcome

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cwp.jinja_hub.model.ProfessionalUser
import com.cwp.jinja_hub.repository.ProfessionalSignupRepository
import kotlinx.coroutines.launch

// You might define this sealed class in its own file if desired.
sealed class SignUpResult {
    data class Success(val userId: String) : SignUpResult()
    data class Error(val errorMessage: String) : SignUpResult()
}

class WelcomeViewModel : ViewModel() {
    private val repository = ProfessionalSignupRepository()

    private val _name = MutableLiveData<String>()
    val name: LiveData<String> = _name

    fun setName(newName: String) {
        _name.value = newName
    }

    fun getName(): String? {
        return _name.value
    }


    /**
     * Retrieves user info from the repository.
     *
     * When the repository returns a ProfessionalUser, we update the name LiveData
     * with the user's full name (here we combine firstName and lastName) and then pass
     * the user via the callback.
     *
     * @param userId the user's id.
     * @param callback a callback that receives the ProfessionalUser? (or null if not found).
     */
    fun getUserProfile(userId: String, callback: (ProfessionalUser?) -> Unit) {
        viewModelScope.launch {
            val user = repository.getUserProfile(userId) { user ->
                if (user != null) {

                }

            }
            if (user != null) {
                val fullName = "${user.firstName} ${user.lastName}"
                setName(fullName)
                _name.value = fullName
                callback(user)
            } else {
                callback(null)
            }
        }
    }

    class WelcomeViewModelFactory(private val repository: ProfessionalSignupRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(WelcomeViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return WelcomeViewModel() as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
