package com.cwp.jinja_hub.ui.client_registration

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cwp.jinja_hub.repository.LoginRepository
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch

class LoginViewModel(private val loginRepository: LoginRepository) : ViewModel() {

    private val _loginResult = MutableLiveData<FirebaseUser?>()
    val loginResult: LiveData<FirebaseUser?> get() = _loginResult

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    fun loginUser(email: String, password: String) {
        viewModelScope.launch {
            try {
                val user = loginRepository.login(email, password)
                if (user != null) {
                    _loginResult.value = user
                } else {
                    _errorMessage.value = "Invalid credentials or user not found."
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }

    fun getCurrentUser(): FirebaseUser? {
        return loginRepository.getCurrentUser()
    }

    class LoginViewModelFactory(private val loginRepository: LoginRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return LoginViewModel(loginRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
