package com.cwp.jinja_hub.ui.professionals_registration.fragments

import android.content.Context
import android.content.SharedPreferences
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cwp.jinja_hub.repository.ClientSignupRepository
import kotlinx.coroutines.launch

class PersonalInfoViewModel : ViewModel() {
    private val repository = ClientSignupRepository()

    private val _fullName = MutableLiveData<String>()
    val fullName: LiveData<String> get() = _fullName

    private val _username = MutableLiveData<String?>()
    val username: MutableLiveData<String?> get() = _username

    private val _email = MutableLiveData<String?>()
    val email: MutableLiveData<String?> get() = _email

    private val _password = MutableLiveData<String?>()
    val password: MutableLiveData<String?> get() = _password

    private val _usernameError = MutableLiveData<String?>()
    val usernameError: LiveData<String?> get() = _usernameError

    private val _emailError = MutableLiveData<String?>()
    val emailError: LiveData<String?> get() = _emailError

    private val _passwordError = MutableLiveData<String?>()
    val passwordError: LiveData<String?> get() = _passwordError

    private val _isSavedSuccessful = MutableLiveData<Boolean>()
    val isSavedSuccessful: LiveData<Boolean> get() = _isSavedSuccessful

    private val _userId = MutableLiveData<String?>()
    val userId: LiveData<String?> get() = _userId

    fun setFullName(name: String) {
        _fullName.value = name
    }

    fun setUsername(username: String) {
        if (!username.matches(Regex("^[A-Za-z0-9_]+$"))) {
            _usernameError.value = "Username can only contain letters, numbers, and underscores (no spaces)."
            _username.value = null
        } else {
            _username.value = username
            _usernameError.value = null
        }
    }

    fun setEmail(email: String) {
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _emailError.value = "Invalid email format."
            _email.value = null
        } else {
            _email.value = email
            _emailError.value = null
        }
    }

    fun setPassword(password: String) {
        when {
            password.length < 8 -> {
                _passwordError.value = "Password must be at least 8 characters long."
                _password.value = null
            }
            !password.matches(Regex("^(?=.*[a-zA-z])(?=.*[0-9])(?=.*[@£\$%^*&?!±|#€∞§¶]).{8,}\$")) -> {
                _passwordError.value = "Password must contain a letter, a number, and a special character."
                _password.value = null
            }
            else -> {
                _password.value = password
                _passwordError.value = null
            }
        }
    }

    fun saveInfoToSharedPreferences(context: Context) {
        viewModelScope.launch {
            val sharedPreferences: SharedPreferences = context.getSharedPreferences("user_preferences", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()

            _fullName.value?.let { editor.putString("full_name", it) }
            _username.value?.let { editor.putString("username", it) }
            _email.value?.let { editor.putString("email", it) }
            _password.value?.let { editor.putString("password", it) }

            editor.apply()
            _isSavedSuccessful.postValue(true)
        }
    }

    fun loadInfoFromSharedPreferences(context: Context, callback: (String, String, String, String) -> Unit) {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences("user_preferences", Context.MODE_PRIVATE)
        _fullName.value = sharedPreferences.getString("full_name", "")
        _username.value = sharedPreferences.getString("username", "")
        _email.value = sharedPreferences.getString("email", "")
        _password.value = sharedPreferences.getString("password", "")
        callback(_fullName.value ?: "", _username.value ?: "", _email.value ?: "", _password.value ?: "")
    }

    fun clearAllData(context: Context) {
        viewModelScope.launch {
            val sharedPreferences: SharedPreferences = context.getSharedPreferences("user_preferences", Context.MODE_PRIVATE)
            sharedPreferences.edit().clear().apply()

            _fullName.value = ""
            _username.value = ""
            _email.value = ""
            _password.value = ""

            Toast.makeText(context, "All saved data has been cleared!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveSelection(selection: String, context: Context, preferenceKey: String) {
        val sharedPreferences = context.getSharedPreferences("user_preferences", Context.MODE_PRIVATE)
        sharedPreferences.edit().putString(preferenceKey, selection).apply()
        Toast.makeText(context, "Selection saved: $selection", Toast.LENGTH_SHORT).show()
    }
}
