package com.cwp.jinja_hub.ui.chat

import androidx.lifecycle.*
import com.cwp.jinja_hub.model.ChatItem
import com.cwp.jinja_hub.model.NormalUser
import com.cwp.jinja_hub.repository.ChatRepository
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChatViewModel(private val chatRepository: ChatRepository, private val fUser: FirebaseUser) : ViewModel() {

    private val _chats = MutableLiveData<List<NormalUser>>()
    val chats: LiveData<List<NormalUser>> get() = _chats

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    private val loadedUserIds = mutableSetOf<String>()  // To prevent duplicate loads

    init {
        fetchChats()
    }

    private fun fetchChats() {
        _isLoading.postValue(true)
        chatRepository.getChats(mutableListOf(), mutableListOf(), fUser) { userList ->
            val uniqueUsers = userList.distinctBy { it.userId }
            if (uniqueUsers.isNotEmpty()) {
                _isLoading.postValue(false)
                _chats.postValue(uniqueUsers)
                loadedUserIds.addAll(uniqueUsers.map { it.userId })  // Track loaded users
            } else {
                _isLoading.postValue(false)
                _error.postValue("No chats available")
            }
        }
    }

    fun refreshChats() {
        loadedUserIds.clear()
        fetchChats()
    }

    fun clearObservers(owner: LifecycleOwner) {
        _chats.removeObservers(owner)
        _error.removeObservers(owner)
    }

    class ChatViewModelFactory(private val chatRepository: ChatRepository, private val f: FirebaseUser) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
                return ChatViewModel(chatRepository, f) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
