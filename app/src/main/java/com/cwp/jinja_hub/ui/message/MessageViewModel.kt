package com.cwp.jinja_hub.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cwp.jinja_hub.model.Message
import com.cwp.jinja_hub.repository.MessageRepository
import com.google.firebase.database.DatabaseError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MessageViewModel(private val messageRepository: MessageRepository) : ViewModel() {

    // LiveData for messages
    private val _messages = MutableLiveData<List<Message>>()
    val messages: LiveData<List<Message>> get() = _messages

    // LiveData for unread count
    private val _unreadCount = MutableLiveData<Int>()
    val unreadCount: LiveData<Int> get() = _unreadCount

    // LiveData for receiver's info (name and profile image)
    private val _receiverInfo = MutableLiveData<Pair<String, String>>()
    val receiverInfo: LiveData<Pair<String, String>> get() = _receiverInfo

    // LiveData for error handling
    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    /**
     * Fetch chats and update LiveData
     */
    fun getChats() {
        viewModelScope.launch {
            try {
                messageRepository.getChats { messages ->
                    _messages.value = messages
                }
            } catch (e: Exception) {
                _error.value = "Failed to fetch chats: ${e.message}"
            }
        }
    }

    /**
     * Fetch unread message count for a chat and user
     */
    fun fetchUnreadCount(chatId: String, userId: String) {
        viewModelScope.launch {
            try {
                messageRepository.fetchUnreadCount(chatId, userId) { count ->
                    _unreadCount.value = count
                }
            } catch (e: Exception) {
                _error.value = "Failed to fetch unread count: ${e.message}"
            }
        }
    }

    /**
     * Fetch receiver info (name and profile image)
     */
    fun fetchReceiverInfo(receiverId: String) {
        viewModelScope.launch {
            try {
                messageRepository.fetchReceiverInfo(receiverId) { name, profileImage ->
                    _receiverInfo.value = Pair(name, profileImage)
                }
            } catch (e: Exception) {
                _error.value = "Failed to fetch receiver info: ${e.message}"
            }
        }
    }

    /**
     * Send a text message to a user
     */
    fun sendMessageToUser(senderId: String, receiverId: String, message: Message) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                messageRepository.sendMessageToUser(senderId, receiverId, message)
            } catch (e: Exception) {
                _error.postValue("Failed to send message: ${e.message}")
            }
        }
    }

    /**
     * Send an image message
     */
    fun sendImageMessage(senderId: String, receiverId: String, imageUri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                messageRepository.sendImageToStorage(senderId, receiverId, imageUri)
            } catch (e: Exception) {
                _error.postValue("Failed to send image: ${e.message}")
            }
        }
    }

    /**
     * Mark message as seen for the given user
     */
    fun markMessageAsSeen(userId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                messageRepository.seenMessage(userId) { listener ->
                    // Perform any additional logic with the listener if necessary
                }
            } catch (e: Exception) {
                _error.postValue("Failed to mark message as seen: ${e.message}")
            }
        }
    }

    /**
     * Remove any listeners when the ViewModel is cleared
     */
    override fun onCleared() {
        super.onCleared()
        messageRepository.removeListeners()
    }

    // Factory class for creating MessageViewModel instances
    class MessageViewModelFactory(private val repository: MessageRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MessageViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MessageViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
