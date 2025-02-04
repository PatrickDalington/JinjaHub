package com.cwp.jinja_hub.ui.market_place

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cwp.jinja_hub.model.ADModel
import com.cwp.jinja_hub.model.ReviewModel
import com.cwp.jinja_hub.repository.ADRepository
import kotlinx.coroutines.launch

class ADViewModel() : ViewModel() {

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
        // myADDrinks
        fetchMyDrinkAds()

        //myADSoap
        fetchMySoapAds()

        // popular reviews
        fetchPopularAD("")
    }




    fun uploadAdWithImages(
        ad: ADModel,
        imageUris: List<Uri>
    ) {
        _uploadProgress.value = true
        _uploadError.value = null

        repository.uploadImagesToFirebase(
            imageUris,
            onSuccess = { imageUrls ->
                // Once images are uploaded, add them to the review object and upload it to the database
                ad.mediaUrl = imageUrls
                repository.submitADToFirebase(ad, ad.adType) { success ->
                    _uploadProgress.value = false
                    _uploadSuccess.value = success
                    if (!success) _uploadError.value = "Failed to submit ads."
                }
            },
            onFailure = { error ->
                // Handle image upload failure
                _uploadProgress.value = false
                _uploadError.value = error
            }
        )
    }

    // fetchUserDetails
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
            adId,
            adType,
            updatedReview,
            oldImageUrls,
            newImageUris
        ) { success, errorMessage ->
            _operationInProgress.value = false
            _operationSuccess.value = success
            if (!success) {
                _operationError.value = errorMessage ?: "Unknown error occurred."
            }
        }
    }

    // Fetch specific clicked ad
    fun fetchSpecificClickedAD(adId: String, adType: String,  callback2: (Boolean) -> Unit, callback: (ADModel) -> Unit) {
        repository.fetchSpecificClickedAD(adId, adType, callback2,callback )
    }


    // Like and unlike
    fun likeAD(adId: String, adType: String, userId: String, callback: (String) -> Unit) {
        repository.likeAD(adId, userId, adType, callback)
    }

    // fetch number of likes
    fun fetchNumberOfLikes(adId: String, adType: String, callback: (Int) -> Unit) {
        repository.fetchNumberOfLikes(adId, adType, callback)
    }

    // check if user liked review
    fun checkIfUserLikedAD(adId: String, adType: String, callback: (Boolean) -> Unit) {
        repository.checkIfUserLikedAD(adId, adType, callback)
    }

    // Add number of shares to firebase database
    fun addNumberOfShares(reviewId: String) {
        repository.addNumberOfShares(reviewId)
    }

    // fetch number of shares
    fun getNumberOfShares(reviewId: String, callback: (Int) -> Unit) {
        repository.getNumberOfShares(reviewId, callback)
    }

    /**
     * Fetches popular ads from the repository.
     */
    private fun fetchPopularAD(adType: String) {
       _isLoading.value = true

        viewModelScope.launch {
            try {
                val fetchedADs = mutableListOf<ADModel>()
                val ads = repository.fetchPopularAD(adType){ fullName, username, profileImage, ad ->
                    // Map additional details if needed, e.g., adding user information to the review
                    val enrichedReview = ad.copy(
                        // Optionally add user details if required
                        description = ad.description,
                        posterName = fullName,
                        posterUsername = username,
                        posterProfileImage = profileImage
                    )
                    fetchedADs.add(enrichedReview)
                    _popularAd.postValue(fetchedADs)
                    _isLoading.value = false
                }

            } catch (e: Exception) {
                _isLoading.value = false
            }
        }

    }

    // Update Full Review


    private fun fetchMyDrinkAds() {
        val fetchedAds = mutableListOf<ADModel>()
        repository.fetchMyADs("Jinja Herbal Extract") { fullName, username, profileImage, review ->
            // Map additional details if needed, e.g., adding user information to the review
            val enrichedAds = review.copy(
                // Optionally add user details if required
                description = review.description,
                posterName = fullName,
                posterUsername = username,
                posterProfileImage = profileImage
            )
            fetchedAds.add(enrichedAds)

            viewModelScope.launch {
                _myAdDrink.value = fetchedAds
            }
        }
    }

    private fun fetchMySoapAds() {
        val fetchedAds = mutableListOf<ADModel>()
        repository.fetchMyADs("Iru Soap") { fullName, username, profileImage, review ->
            // Map additional details if needed, e.g., adding user information to the review
            val enrichedAds = review.copy(
                // Optionally add user details if required
                description = review.description,
                posterName = fullName,
                posterUsername = username,
                posterProfileImage = profileImage
            )
            fetchedAds.add(enrichedAds)

            viewModelScope.launch {
                _myAdSoap.value = fetchedAds
            }
        }
    }

    // Delete ad
    fun deleteAd(adId: String, adType: String, callback: (Boolean) -> Unit) {
        repository.deleteAD(adId, adType, callback)
    }

    // editAds
    fun editAd(
        context: Context,
        adId: String,
        adType: String,
        ad: ADModel,
        newImages: List<Uri>, // Paths of new images to upload
        imagesToKeep: List<String>, // Existing images to keep
        callback: ADRepository.EditADCallback
    ) {
        repository.editAD(context, adId, adType, ad, newImages, imagesToKeep, callback)
    }
}
