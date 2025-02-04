package com.cwp.jinja_hub.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.cwp.jinja_hub.model.ADModel
import com.cwp.jinja_hub.repository.ADRepository

class UserProfileViewModel() : ViewModel() {

    private val errorMessage = MutableLiveData<String>()
    val errorMessageLiveData: LiveData<String> get() = errorMessage

    private val repository = ADRepository()

    // LiveData to observe upload progress
    private val _uploadProgress = MutableLiveData<Boolean>()
    val uploadProgress: LiveData<Boolean> get() = _uploadProgress

    // LiveData to observe upload progress
    private var _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    // LiveData to observe errors during upload
    private val _uploadError = MutableLiveData<String?>()
    val uploadError: LiveData<String?> get() = _uploadError

    // LiveData for success confirmation
    private val _uploadSuccess = MutableLiveData<Boolean>()
    val uploadSuccess: LiveData<Boolean> get() = _uploadSuccess


    private val _operationInProgress = MutableLiveData<Boolean>()
    val operationInProgress: LiveData<Boolean> get() = _operationInProgress

    // LiveData to observe errors during upload or update
    private val _operationError = MutableLiveData<String?>()
    val operationError: LiveData<String?> get() = _operationError

    // LiveData for success confirmation
    private val _operationSuccess = MutableLiveData<Boolean>()
    val operationSuccess: LiveData<Boolean> get() = _operationSuccess

    // LiveData for popular reviews
    private val _popularAd = MutableLiveData<MutableList<ADModel>>()
    val popularAd: LiveData<MutableList<ADModel>> get() = _popularAd

    // LiveData for my reviews
    private val _myAdDrink = MutableLiveData<MutableList<ADModel>>()
    val myAdDrink: LiveData<MutableList<ADModel>> get() = _myAdDrink

    private val _myAdSoap = MutableLiveData<MutableList<ADModel>>()
    val myAdSoap: LiveData<MutableList<ADModel>> get() = _myAdSoap



    init {

    }

    // fetchUserDetails
    fun fetchUserDetails(userId: String, callback: (String, String, String, Boolean) -> Unit) {
        repository.fetchUserDetails(userId, callback)
    }



}
