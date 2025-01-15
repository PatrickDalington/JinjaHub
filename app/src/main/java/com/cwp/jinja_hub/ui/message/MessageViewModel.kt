package com.cwp.jinja_hub.ui.message

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.cwp.jinja_hub.model.Message
import com.cwp.jinja_hub.repository.MessageRepository
import com.cwp.jinja_hub.repository.ServiceRepository
import com.cwp.jinja_hub.ui.services.ServicesViewModel

class MessageViewModel(private val repository: MessageRepository) : ViewModel() {

    private val _messages = MutableLiveData<List<Message>>()
    val messages: LiveData<List<Message>> get() = _messages

    private val _unreadCount = MutableLiveData<Int>()
    val unreadCount: LiveData<Int> get() = _unreadCount

    fun loadMessages(chatId: String) {
        repository.getChats(chatId) { messages ->
            _messages.postValue(messages)
        }
    }

    fun sendMessage(chatId: String, message: Message) {
        repository.sendMessage(chatId, message)
    }

    fun fetchUnreadCount(chatId: String, userId: String) {
        repository.fetchUnreadCount(chatId, userId) { count ->
            _unreadCount.postValue(count)
        }
    }

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