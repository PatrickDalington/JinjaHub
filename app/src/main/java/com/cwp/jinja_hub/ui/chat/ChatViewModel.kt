package com.cwp.jinja_hub.ui.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cwp.jinja_hub.model.ChatItem
import com.cwp.jinja_hub.model.Message
import com.cwp.jinja_hub.repository.ChatRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChatViewModel(private val chatRepository: ChatRepository) : ViewModel() {

    private val _chats = MutableLiveData<List<ChatItem>>()
    val chats: LiveData<List<ChatItem>> get() = _chats

    private val _unreadCounts = MutableLiveData<Map<String, Int>>()
    val unreadCounts: LiveData<Map<String, Int>> get() = _unreadCounts

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error





}