package com.cwp.jinja_hub.com.cwp.jinja_hub.ui.message

import android.net.Uri
import android.util.Log
import androidx.lifecycle.*
import com.cwp.jinja_hub.model.Message
import com.cwp.jinja_hub.repository.MessageRepository
import com.github.kittinunf.fuel.Fuel
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MessageViewModel(private val messageRepository: MessageRepository) : ViewModel() {

    private val _messages = MutableLiveData<List<Message>>()
    val messages: LiveData<List<Message>> get() = _messages

    private val _unreadCount = MutableLiveData<Int>()
    val unreadCount: LiveData<Int> get() = _unreadCount

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _receiverInfo = MutableLiveData<Pair<String, String>>()
    val receiverInfo: LiveData<Pair<String, String>> get() = _receiverInfo

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    /**
     * Fetch chats and update LiveData.
     */
    fun getChats(chatId: String) {
        _isLoading.postValue(true)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                messageRepository.getChats(chatId) { messages ->
                    _messages.postValue(messages)
                    _isLoading.postValue(false)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _error.value = "Failed to fetch chats: ${e.message}"
                    _isLoading.value = false
                }
            }
        }
    }

    /**
     * Fetch unread message count for a chat.
     */
    fun fetchUnreadCount(chatId: String, userId: String) {
        viewModelScope.launch {
            try {
                messageRepository.getUnreadCount(chatId, userId) { count ->
                    _unreadCount.postValue(count)
                }
            } catch (e: Exception) {
                _error.postValue("Failed to fetch unread count: ${e.message}")
            }
        }
    }

    /**
     * Fetch receiver info (name and profile image).
     */
    fun fetchReceiverInfo(receiverId: String) {
        viewModelScope.launch {
            try {
                messageRepository.fetchReceiverInfo(receiverId) { name, profileImage ->
                    _receiverInfo.postValue(Pair(name, profileImage))
                }
            } catch (e: Exception) {
                _error.postValue("Failed to fetch receiver info: ${e.message}")
            }
        }
    }

    /**
     * Send a text message to a user.
     */
    fun sendMessageToUser(senderId: String, receiverId: String, message: Message, callback: (Boolean) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                messageRepository.sendMessageToUser(senderId, receiverId, message, callback)
            } catch (e: Exception) {
                _error.postValue("Failed to send message: ${e.message}")
            }
        }
    }

    /**
     * Send an image message.
     */
    fun sendImageMessage(senderId: String, receiverId: String, imageUri: List<Uri>, callback: (Boolean) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                messageRepository.sendImageToStorage(senderId, receiverId, imageUri, callback)
            } catch (e: Exception) {
                _error.postValue("Failed to send image: ${e.message}")
            }
        }
    }

    /**
     * Mark messages as seen for a given user.
     */
    fun markMessageAsSeen(userId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                messageRepository.seenMessage(userId) { listener ->
                    // Perform any additional logic with the listener if necessary
                    //messageRepository.clearListeners()
                }
            } catch (e: Exception) {
                _error.postValue("Failed to mark message as seen: ${e.message}")
            }
        }
    }

    // Delete message
    fun deleteMessage(messageId: String, callback: (Boolean) -> Unit) {
        viewModelScope
            .launch(Dispatchers.IO) {
                try {
                    messageRepository.deleteMessage(messageId, callback)
                } catch (e: Exception) {
                    _error.postValue("Failed to delete message: ${e.message}")
                }
            }
    }


    fun triggerNotification(token: String, data: Map<String, String>?) {
        viewModelScope.launch(Dispatchers.IO) { // Run network request in background thread
            try {
                val notificationData = mapOf(
                    "token" to token,
                    "data" to data
                )

                val url = "https://jinja-hub-be-98c42e1b57d2.herokuapp.com/send-notification"
                val (_, _, result) = Fuel.post(url)
                    .header("Content-Type", "application/json")
                    .body(Gson().toJson(notificationData))
                    .responseString()

                withContext(Dispatchers.Main) { // Switch back to main thread
                    result.fold(
                        success = { Log.d("FCM", "Notification sent successfully!") },
                        failure = { error -> Log.e("FCM", "Error sending notification: $error") }
                    )
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("FCM", "Failed to trigger notification: ${e.message}")
                }
            }
        }
    }


    fun clearChatWithUser(receiverId: String, callback: (Boolean) -> Unit) {
        _isLoading.postValue(true)
        messageRepository.clearChatWithUser(receiverId){
            if (it){
                _isLoading.postValue(false)
                callback(true)
            }else{
                _isLoading.postValue(false)
                callback(false)
            }
        }
    }


    fun clearUserFromChatList(receiverId: String, callback: (Boolean) -> Unit) {
        _isLoading.postValue(true)
        messageRepository.clearUserFromChatList(receiverId){
            if (it){
                _isLoading.postValue(false)
                callback(true)
            }else{
                _isLoading.postValue(false)
                callback(false)
            }
        }
    }

    fun reportUser(type: String, category: String, receiverId: String, reason: String, productId: String, callback: (Boolean) -> Unit) {
        _isLoading.postValue(true)
        messageRepository.reportUser(type, category, receiverId, reason, productId){
            if (it){
                _isLoading.postValue(false)
                callback(true)
            }else{
                _isLoading.postValue(false)
                callback(false)
            }
        }
    }

    /**
     * Check if it is a first-time chat between sender and receiver.
     */
    fun isFirstTimeChat(senderId: String, receiverId: String, callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val isFirstTime = messageRepository.checkFirstTimeChat(senderId, receiverId)
                callback(isFirstTime)
            } catch (e: Exception) {
                _error.postValue("Failed to check if it's a first-time chat: ${e.message}")
            }
        }
    }

    /**
     * Remove listeners when the ViewModel is cleared.
     */
    override fun onCleared() {
        super.onCleared()
        messageRepository.clearListeners()
    }

    /**
     * Factory class for creating MessageViewModel instances.
     */
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
