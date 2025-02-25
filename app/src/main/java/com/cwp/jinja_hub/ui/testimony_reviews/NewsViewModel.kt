package com.cwp.jinja_hub.com.cwp.jinja_hub.ui.testimony_reviews

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.viewModelFactory
import com.cwp.jinja_hub.com.cwp.jinja_hub.model.NewsModel
import com.cwp.jinja_hub.com.cwp.jinja_hub.repository.NewsRepository
import com.cwp.jinja_hub.model.ReviewModel
import com.cwp.jinja_hub.repository.ReviewRepository
import kotlinx.coroutines.launch

class NewsViewModel : ViewModel() {

    private val repository = NewsRepository()

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
    private val _popularNews = MutableLiveData<MutableList<NewsModel>>()
    val popularNews: LiveData<MutableList<NewsModel>> get() = _popularNews

    // LiveData for my reviews
    private val _myNews = MutableLiveData<MutableList<NewsModel>>()
    val myNews: LiveData<MutableList<NewsModel>> get() = _myNews



    init {
        // myReviews
        fetchMyNews()
        // popular reviews
        fetchPopularNews()
    }

    /**
     * Uploads a review with associated images.
     *
     * @param review The review object to be uploaded.
     * @param imageUris List of image URIs to be uploaded.
     */
    fun uploadReviewWithImages(
        news: NewsModel,
        imageUris: List<Uri>
    ) {
        _uploadProgress.value = true
        _uploadError.value = null

        repository.uploadImagesToFirebase(
            imageUris,
            onSuccess = { imageUrls ->
                // Once images are uploaded, add them to the review object and upload it to the database
                news.mediaUrl = imageUrls
                repository.submitNewsToFirebase(news) { success ->
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
    fun updateNews(
        newsId: String,
        updatedNews: NewsModel,
        oldImageUrls: List<String>,
        newImageUris: List<Uri>
    ) {
        _operationInProgress.value = true
        _operationError.value = null

        repository.updateFullNews(
            newsId,
            updatedNews,
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
    fun fetchNumberOfReviewComments(newsId: String, callback: (Int) -> Unit) {
        repository.fetchNumberOfNewsComments(newsId, callback)
    }

    // Like and unlike
    fun likeReview(newsId: String, userId: String, callback: (String) -> Unit) {
        repository.likeNews(newsId, userId, callback)
    }

    // fetch number of likes
    fun fetchNumberOfLikes(newsId: String, callback: (Int) -> Unit) {
        repository.fetchNumberOfLikes(newsId, callback)
    }

    // check if user liked review
    fun checkIfUserLikedNews(newsId: String, callback: (Boolean) -> Unit) {
        repository.checkIfUserLikedNews(newsId, callback)
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
    fun fetchPopularNews() {
       _isLoading.value = true

        viewModelScope.launch {
            try {
                val fetchedNews = mutableListOf<NewsModel>()
                val news = repository.fetchPopularNews{ fullName, username, profileImage, news ->
                    // Map additional details if needed, e.g., adding user information to the review
                    val enrichedNews = news.copy(
                        // Optionally add user details if required
                        content = news.content,
                        header = news.header,
                    )
                    fetchedNews.add(enrichedNews)
                    _popularNews.postValue(fetchedNews)
                    _isLoading.value = false
                }

            } catch (e: Exception) {
                _isLoading.value = false
            }
        }

    }

    // Update Full Review


    fun fetchMyNews() {
        val fetchedNews = mutableListOf<NewsModel>()
        repository.fetchMyNews { fullName, username, profileImage, news ->
            // Map additional details if needed, e.g., adding user information to the review
            val enrichedNews = news.copy(
                // Optionally add user details if required
                content = news.content,
                header = news.header,
            )
            fetchedNews.add(enrichedNews)

            viewModelScope.launch {
                _myNews.value = fetchedNews
            }
        }
    }


    // Delete reviews
    fun deleteReview(newsId: String, callback: (Boolean) -> Unit) {
        repository.deleteNews(newsId, callback)
    }

    // editReview
    fun editReview(
        context: Context,
        newsId: String,
        news: NewsModel,
        newImages: List<Uri>, // Paths of new images to upload
        imagesToKeep: List<String>, // Existing images to keep
        callback: NewsRepository.EditNewsCallback
    ) {
        repository.editNews(context, newsId, news, newImages, imagesToKeep, callback)
    }

    // Fetch total number of reviews
    fun fetchTotalNumberOfReviews(callback: (Int) -> Unit) {
        _isLoading.value = true
        repository.fetTotalNumberOfNews {
            callback(it)
            _isLoading.value = false
        }
    }
}
