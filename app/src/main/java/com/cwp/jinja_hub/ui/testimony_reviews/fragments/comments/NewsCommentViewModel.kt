package com.cwp.jinja_hub.com.cwp.jinja_hub.ui.testimony_reviews.fragments.comments

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.cwp.jinja_hub.com.cwp.jinja_hub.model.NewsModel
import com.cwp.jinja_hub.com.cwp.jinja_hub.repository.NewsCommentRepository
import com.cwp.jinja_hub.model.LatestCommentModel
import com.cwp.jinja_hub.model.ReviewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class NewsCommentViewModel(private val repository: NewsCommentRepository) : ViewModel() {

    val comments: LiveData<List<LatestCommentModel>> get() = repository.comments
    private val fUser : FirebaseUser = FirebaseAuth.getInstance().currentUser!!

    fun fetchComments(newsId: String) {
        repository.fetchComments(newsId)
    }

    fun addComment(newsId: String, commentText: String, callback: (Boolean) -> Unit) {
        repository.addComment(fUser, newsId, commentText) {
            if (it) {
                callback(true)
            } else
                callback(false)
        }
    }

    fun fetchSpecificClickedNews(newsId: String, callback: (String, String, String, NewsModel?) -> Unit) {
        repository.fetchSpecificClickedNews(newsId, callback)
    }

    fun fetchAllSenderId(callback: (List<String>) -> Unit){
        repository.fetchAllSenderId(callback)
    }

    fun deleteComment(comment: LatestCommentModel, newsId: String, callback: (Boolean) -> Unit) {
        repository.deleteComment(comment, newsId, callback)
    }

    class NewsCommentViewModelFactory(private val repository: NewsCommentRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(NewsCommentViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return NewsCommentViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

}
