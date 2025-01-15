package com.cwp.jinja_hub.ui.client_registration

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cwp.jinja_hub.repository.ClientSignupRepository
import kotlinx.coroutines.launch

class ClientSignupViewModel : ViewModel() {
    private val repository = ClientSignupRepository()

    private val _fullName = MutableLiveData<String>()
    val fullName: LiveData<String> get() = _fullName

    private val _username = MutableLiveData<String>()
    val username: LiveData<String> get() = _username

    private val _email = MutableLiveData<String>()
    val email: LiveData<String> get() = _email

    private val _password = MutableLiveData<String>()
    val password: LiveData<String> get() = _password

    private val _isSignupSuccessful = MutableLiveData<Boolean>()
    val isSignupSuccessful: LiveData<Boolean> get() = _isSignupSuccessful

    private val _userId = MutableLiveData<String?>()
    val userId: MutableLiveData<String?> get() = _userId

    fun setFullName(name: String) {
        _fullName.value = name
    }

    fun setUsername(username: String) {
        _username.value = username
    }

    fun setEmail(email: String) {
        _email.value = email
    }

    fun setPassword(password: String) {
        _password.value = password
    }

    fun signup() {
        viewModelScope.launch {
            val fullName = _fullName.value ?: return@launch
            val username = _username.value
            val email = _email.value
            val password = _password.value

            val userId = repository.signUpUserWithEmailAndPassword(
                fullName,
                username.orEmpty(),
                email.orEmpty(),
                password.orEmpty()
            )
            if (userId != null) {
                _isSignupSuccessful.postValue(true)
                _userId.postValue(userId)
            } else {
                _isSignupSuccessful.postValue(false)
            }

            // Send verification email if sign-up is successful
            if (userId != null) {
                repository.confirmUserEmail()
            }
        }
    }
}
