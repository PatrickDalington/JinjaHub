package com.cwp.jinja_hub.ui.market_place

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.*
import com.cwp.jinja_hub.model.ADModel
import com.cwp.jinja_hub.model.ReviewModel
import com.cwp.jinja_hub.repository.ADRepository
import com.cwp.jinja_hub.repository.HomeRepository
import com.cwp.jinja_hub.ui.home.HomeViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class ADViewModel(private val repository: ADRepository) : ViewModel() {

    private val _errorMessage = MutableLiveData<String>()
    val errorMessageLiveData: LiveData<String> get() = _errorMessage

    private val _uploadProgress = MutableLiveData<Boolean>()
    val uploadProgress: LiveData<Boolean> get() = _uploadProgress

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _uploadError = MutableLiveData<String?>()
    val uploadError: LiveData<String?> get() = _uploadError

    private val _uploadSuccess = MutableLiveData<Boolean>()
    val uploadSuccess: LiveData<Boolean> get() = _uploadSuccess

    private val _operationInProgress = MutableLiveData<Boolean>()
    val operationInProgress: LiveData<Boolean> get() = _operationInProgress

    private val _operationError = MutableLiveData<String?>()
    val operationError: LiveData<String?> get() = _operationError

    private val _operationSuccess = MutableLiveData<Boolean>()
    val operationSuccess: LiveData<Boolean> get() = _operationSuccess

    private val _popularAdDrink = MutableLiveData<MutableList<ADModel>>()
    val popularAdDrink: LiveData<MutableList<ADModel>> get() = _popularAdDrink

    private val _popularAdSoap = MutableLiveData<MutableList<ADModel>>()
    val popularAdSoap: LiveData<MutableList<ADModel>> get() = _popularAdSoap

    private val _myAdDrink = MutableLiveData<MutableList<ADModel>>()
    val myAdDrink: LiveData<MutableList<ADModel>> get() = _myAdDrink

    private val _myAdSoap = MutableLiveData<MutableList<ADModel>>()
    val myAdSoap: LiveData<MutableList<ADModel>> get() = _myAdSoap

    init {
        fetchMyAds("Jinja Herbal Extract", _myAdDrink)
        fetchMyAds("Iru Soap", _myAdSoap)
        fetchAllAdsDrinks()
        fetchAllAdsSoap()
    }

    fun uploadAdWithImages(ad: ADModel, imageUris: List<Uri>) {
        _uploadProgress.value = true
        _uploadError.value = null

        repository.uploadImagesToFirebase(
            imageUris,
            onSuccess = { imageUrls ->
                ad.mediaUrl = imageUrls
                repository.submitADToFirebase(ad, ad.adType) { success ->
                    _uploadProgress.value = false
                    _uploadSuccess.value = success
                    if (!success) _uploadError.value = "Failed to submit ads."
                }
            },
            onFailure = { error ->
                _uploadProgress.value = false
                _uploadError.value = error
            }
        )
    }

    fun updateFullAD(adId: String, adType: String, updatedReview: ReviewModel, oldImageUrls: List<String>, newImageUris: List<Uri>) {
        _operationInProgress.value = true
        _operationError.value = null
        repository.updateFullAD(adId, adType, updatedReview, oldImageUrls, newImageUris) { success, errorMessage ->
            _operationInProgress.value = false
            _operationSuccess.value = success
            if (!success) {
                _operationError.value = errorMessage ?: "Unknown error occurred."
            }
        }
    }

    fun editAD(
        context: Context,
        adId: String,
        adType: String,
        ad: ADModel,
        newImages: List<Uri>,
        imagesToKeep: List<String>,
        callback: ADRepository.EditADCallback
    ) {
        _operationInProgress.value = true
        _operationError.value = null
        repository.editAD(context, adId, adType, ad, newImages, imagesToKeep, callback)
        _operationInProgress.value = false
        _uploadSuccess.value = true
    }

    fun fetchUserDetails(userId: String, callback: (String, String, String, Boolean) -> Unit) {
        repository.fetchUserDetails(userId, callback)
    }

    fun updateAD(
        adId: String,
        adType: String,
        updatedReview: ReviewModel,
        oldImageUrls: List<String>,
        newImageUris: List<Uri>
    ) {
        _operationInProgress.value = true
        _operationError.value = null

        repository.updateFullAD(
            adId, adType, updatedReview, oldImageUrls, newImageUris
        ) { success, errorMessage ->
            _operationInProgress.value = false
            _operationSuccess.value = success
            if (!success) {
                _operationError.value = errorMessage ?: "Unknown error occurred."
            }
        }
    }

    fun fetchSpecificClickedAD(adId: String, adType: String, callback2: (Boolean) -> Unit, callback: (ADModel) -> Unit) {
        repository.fetchSpecificClickedAD(adId, adType, callback2, callback)
    }

    fun likeAD(adId: String, adType: String, userId: String, callback: (String) -> Unit) {
        repository.likeAD(adId, userId, adType, callback)
    }


    private fun fetchAllAdsDrinks() {
        _isLoading.value = true

        viewModelScope.launch {
            try {
                val fetchedAds = mutableListOf<ADModel>()
                repository.fetchPopularAD("Jinja Herbal Extract") { fullName, username, profileImage, ad ->
                    fetchedAds.add(
                        ad.copy(
                            description = ad.description,
                            posterName = fullName,
                            posterUsername = username,
                            posterProfileImage = profileImage
                        )
                    )
                    _popularAdDrink.postValue(fetchedAds)
                }
            } catch (e: Exception) {
                Log.e("ADViewModel", "Error fetching ads", e)
                _operationError.postValue(e.localizedMessage)
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun fetchAllAdsSoap() {
        _isLoading.value = true

        viewModelScope.launch {
            try {
                val fetchedAds = mutableListOf<ADModel>()
                repository.fetchPopularAD("Iru Soap") { fullName, username, profileImage, ad ->
                    fetchedAds.add(
                        ad.copy(
                            description = ad.description,
                            posterName = fullName,
                            posterUsername = username,
                            posterProfileImage = profileImage
                        )
                    )
                    _popularAdSoap.postValue(fetchedAds)
                }
            } catch (e: Exception) {
                Log.e("ADViewModel", "Error fetching ads", e)
                _operationError.postValue(e.localizedMessage)
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun fetchMyAds(adType: String, liveData: MutableLiveData<MutableList<ADModel>>) {
        val fetchedAds = mutableListOf<ADModel>()
        repository.fetchMyADs(adType) { fullName, username, profileImage, review ->
            fetchedAds.add(
                review.copy(
                    description = review.description,
                    posterName = fullName,
                    posterUsername = username,
                    posterProfileImage = profileImage
                )
            )

            viewModelScope.launch {
                liveData.postValue(fetchedAds)
                if (review.adType == "Jinja Herbal Extract" && review.posterId == FirebaseAuth.getInstance().currentUser?.uid)
                    _myAdDrink.postValue(fetchedAds)
                else
                    _myAdSoap.postValue(fetchedAds)

            }
        }
    }

    fun refreshMyDrinkAds() {
        fetchMyAds("Jinja Herbal Extract", _myAdDrink)
    }

    fun refreshMySoapAds() {
        fetchMyAds("Iru Soap", _myAdSoap)
    }

    fun refreshPopularDrinkAds() {
        fetchAllAdsDrinks()
    }

    fun refreshPopularSoapAds() {
        fetchAllAdsSoap()
    }


    fun deleteAd(adId: String, adType: String, callback: (Boolean) -> Unit) {
        _isLoading.value = true
        repository.deleteAD(adId, adType, callback)
        _isLoading.value = false
    }

    fun editAd(
        context: Context,
        adId: String,
        adType: String,
        ad: ADModel,
        newImages: List<Uri>,
        imagesToKeep: List<String>,
        callback: ADRepository.EditADCallback
    ) {
        repository.editAD(context, adId, adType, ad, newImages, imagesToKeep, callback)
    }

    fun filterAdsByLocation(adType: String, country: String, state: String, city: String, callback: (List<ADModel>) -> Unit) {
        repository.filterAdsByLocation(adType, country, state, city, callback)
    }

    class ADViewModelFactory(private val repository: ADRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ADViewModel::class.java)) {
                return ADViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
