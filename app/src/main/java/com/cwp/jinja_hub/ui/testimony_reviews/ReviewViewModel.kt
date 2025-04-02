package com.cwp.jinja_hub.ui.testimony_reviews

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.viewModelFactory
import com.cwp.jinja_hub.model.ReviewModel
import com.cwp.jinja_hub.repository.ReviewRepository
import kotlinx.coroutines.launch

class ReviewViewModel : ViewModel() {

    private val repository = ReviewRepository()

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
    private val _popularReviews = MutableLiveData<MutableList<ReviewModel>>()
    val popularReviews: LiveData<MutableList<ReviewModel>> get() = _popularReviews

    // LiveData for my reviews
    private val _myReviews = MutableLiveData<MutableList<ReviewModel>>()
    val myReviews: LiveData<MutableList<ReviewModel>> get() = _myReviews



    init {
        // myReviews
        fetchMyReviews()
        // popular reviews
        fetchPopularReviews()
    }

    /**
     * Uploads a review with associated images.
     *
     * @param review The review object to be uploaded.
     * @param imageUris List of image URIs to be uploaded.
     */
    fun uploadReviewWithImages(
        review: ReviewModel,
        imageUris: List<Uri>
    ) {
        _uploadProgress.value = true
        _uploadError.value = null

        repository.uploadImagesToFirebase(
            imageUris,
            onSuccess = { imageUrls ->
                // Once images are uploaded, add them to the review object and upload it to the database
                review.mediaUrl = imageUrls
                repository.submitReviewToFirebase(review) { success ->
                    _uploadProgress.value = false
                    _uploadSuccess.value = success
                    if (!success) _uploadError.value = "Failed to submit review."
                }
            },
            onFailure = { error ->
                // Handle image upload failure
                _uploadProgress.value = false
                _uploadError.value = error
            }
        )
    }


    /**
     * Updates an existing review with updated details and images.
     *
     * @param reviewId The ID of the review to update.
     * @param updatedReview The updated review object.
     * @param oldImageUrls List of URLs of old images to delete.
     * @param newImageUris List of URIs of new images to upload.
     */
    fun updateReview(
        reviewId: String,
        updatedReview: ReviewModel,
        oldImageUrls: List<String>,
        newImageUris: List<Uri>
    ) {
        _operationInProgress.value = true
        _operationError.value = null

        repository.updateFullReview(
            reviewId,
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

    // Fetch number of comments for each review
    fun fetchNumberOfReviewComments(reviewId: String, callback: (Int) -> Unit) {
        repository.fetchNumberOfReviewComments(reviewId, callback)
    }

    // Like and unlike
    fun likeReview(reviewId: String, userId: String, callback: (String) -> Unit) {
        repository.likeReview(reviewId, userId, callback)
    }

    // fetch number of likes
    fun fetchNumberOfLikes(reviewId: String, callback: (Int) -> Unit) {
        repository.fetchNumberOfLikes(reviewId, callback)
    }

    // check if user liked review
    fun checkIfUserLikedReview(reviewId: String, callback: (Boolean) -> Unit) {
        repository.checkIfUserLikedReview(reviewId, callback)
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
     * Fetches popular reviews from the repository.
     */
    fun fetchPopularReviews() {
       _isLoading.value = true

        viewModelScope.launch {
            try {
                val fetchedReviews = mutableListOf<ReviewModel>()
                repository.fetchPopularReviews{ fullName, username, profileImage, review ->
                    // Map additional details if needed, e.g., adding user information to the review
                    val enrichedReview = review.copy(
                        // Optionally add user details if required
                        description = review.description,
                        posterName = fullName,
                        posterUsername = username,
                        posterProfileImage = profileImage
                    )
                    fetchedReviews.add(enrichedReview)
                    val shuffledReviews = fetchedReviews.shuffled()
                    _popularReviews.postValue(shuffledReviews.toMutableList())
                    _isLoading.value = false
                }

            } catch (e: Exception) {
                _isLoading.value = false
            }
        }

    }

    // Update Full Review


    fun fetchMyReviews() {
        val fetchedReviews = mutableListOf<ReviewModel>()
        repository.fetchMyReviews { fullName, username, profileImage, review ->
            // Map additional details if needed, e.g., adding user information to the review
            val enrichedReview = review.copy(
                // Optionally add user details if required
                description = review.description,
                posterName = fullName,
                posterUsername = username,
                posterProfileImage = profileImage
            )
            fetchedReviews.add(enrichedReview)

            viewModelScope.launch {
                _myReviews.value = fetchedReviews
            }
        }
    }


    // Delete reviews
    fun deleteReview(reviewId: String, callback: (Boolean) -> Unit) {
        repository.deleteReview(reviewId, callback)
    }

    // editReview
    fun editReview(
        context: Context,
        reviewId: String,
        review: ReviewModel,
        newImages: List<Uri>, // Paths of new images to upload
        imagesToKeep: List<String>, // Existing images to keep
        callback: ReviewRepository.EditReviewCallback
    ) {
        repository.editReview(context, reviewId, review, newImages, imagesToKeep, callback)
    }

    // Fetch total number of reviews
    fun fetchTotalNumberOfReviews(callback: (Int) -> Unit) {
        _isLoading.value = true
        repository.fetTotalNumberOfReviews {
            callback(it)
            _isLoading.value = false
        }
    }
}
