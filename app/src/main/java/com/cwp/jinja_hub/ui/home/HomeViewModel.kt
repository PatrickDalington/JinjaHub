package com.cwp.jinja_hub.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.cwp.jinja_hub.model.NormalUser
import com.cwp.jinja_hub.repository.HomeRepository

class HomeViewModel(private val homeRepository: HomeRepository) : ViewModel() {

    // LiveData for user info.
    private val _user = MutableLiveData<NormalUser?>()
    val user: LiveData<NormalUser?> get() = _user

    // LiveData for loading state.
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    // LiveData for error messages.
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    // New LiveData for unread notifications.
    private val _hasUnreadNotifications = MutableLiveData<Boolean>()
    val hasUnreadNotifications: LiveData<Boolean> get() = _hasUnreadNotifications


    init {
        updateUnreadNotifications(homeRepository.getCurrentUser()?.uid ?: "")
    }
    /**
     * Gets the current user information.
     */
    fun getCurrentUser() {
        _isLoading.value = true
        val currentUser = homeRepository.getCurrentUser()
        if (currentUser != null) {
            homeRepository.getUserInfo(currentUser.uid) { user ->
                _user.value = user
                _isLoading.value = false
                _errorMessage.value = null
            }
        } else {
            _isLoading.value = false
            _errorMessage.value = "No current user found."
        }
    }

    /**
     * Signs out the current user.
     */
    fun signOut() {
        homeRepository.signOut()
    }

    /**
     * Checks unread notifications for the given user and updates LiveData.
     */
    fun updateUnreadNotifications(userId: String) {
        homeRepository.checkUnreadNotifications(userId) { hasUnread ->
            _hasUnreadNotifications.value = hasUnread
        }
    }

    /**
     * Gets user information.
     */
    fun getUserInfo(userId: String, callback: (NormalUser?) -> Unit) {
        _isLoading.value = true
        homeRepository.getUserInfo(userId, callback)
        _isLoading.value = false
    }

    class HomeViewModelFactory(private val repository: HomeRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return HomeViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
