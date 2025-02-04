package com.cwp.jinja_hub.ui.home

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.cwp.jinja_hub.model.NormalUser
import com.cwp.jinja_hub.repository.HomeRepository

class HomeViewModel(private val homeRepository: HomeRepository) : ViewModel() {

    private val _user = MutableLiveData<NormalUser?>()
    val user: MutableLiveData<NormalUser?> get() = _user

    fun getCurrentUser() {
        val currentUser = homeRepository.getCurrentUser()
        if (currentUser != null) {
            homeRepository.getUserInfo(currentUser.uid) { user ->
                _user.value = user
            }
        }
    }

    fun signOut() {
        homeRepository.signOut()
    }

    fun getUserInfo(userId: String, callback: (NormalUser?) -> Unit) {
        homeRepository.getUserInfo(userId, callback)
    }

    class HomeViewModelFactory(private val repository: HomeRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
                return HomeViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}