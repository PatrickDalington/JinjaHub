package com.cwp.jinja_hub.ui.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cwp.jinja_hub.model.ChatItem
import com.cwp.jinja_hub.model.User
import com.cwp.jinja_hub.repository.ChatRepository
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
class ChatViewModel(private val chatRepository: ChatRepository, private val fUser: FirebaseUser) : ViewModel() {

    private val _unreadCounts = MutableLiveData<Int>()
    val unreadCounts: LiveData<Int> get() = _unreadCounts

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    private val _chats = MutableLiveData<List<User>>()
    val chats: LiveData<List<User>> get() = _chats

    private val _userUnreadCounts = MutableLiveData<Map<String, Int>>() // Map of userId to unreadCount
    val userUnreadCounts: LiveData<Map<String, Int>> get() = _userUnreadCounts


    init {
        fetchChats()
    }

    private fun fetchChats() {
        // Assuming a method in the repository that fetches both chat items and users
        chatRepository.getChats(
            mutableListOf(), mutableListOf(),
            fUser = fUser
        ) { userList ->
            if (userList.isNotEmpty()) {
                _chats.postValue(userList)
            } else {
                _error.postValue("No chats available")
            }
        }
    }

    fun getUserInChatList(userList: MutableList<User>, chatList: MutableList<ChatItem>) {
        chatRepository.getUsersInChatList(userList, chatList) { chatsInList ->
            _chats.postValue(chatsInList)
        }
    }

    fun getUnreadCount(userId: String) {
        chatRepository.getUnreadCount(userId) { count ->
            viewModelScope.launch {
                withContext(Dispatchers.Main) {
                    _unreadCounts.value = count
                }
            }
        }
    }

    fun updateLastMessage(chatId: String, lastMessage: String, timestamp: Long) {
        chatRepository.updateLastMessage(chatId, lastMessage, timestamp)
    }



    class ChatViewModelFactory(private val chatRepository: ChatRepository, private val f: FirebaseUser) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
                return ChatViewModel(
                    chatRepository,
                    fUser = f
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
