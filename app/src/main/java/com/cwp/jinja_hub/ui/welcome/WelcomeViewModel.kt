package com.cwp.jinja_hub.ui.welcome

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class WelcomeViewModel: ViewModel() {
    private val _name = MutableLiveData<String>()
    val name: LiveData<String> = _name

    fun setName(newName: String) {
        _name.value = newName
    }

    fun getName(): String? {
        return _name.value
    }

}