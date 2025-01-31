package com.cwp.jinja_hub.ui.testimony_reviews.fragments.comments

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.cwp.jinja_hub.model.LatestCommentModel
import com.cwp.jinja_hub.model.ReviewModel
import com.cwp.jinja_hub.repository.CommentRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class LatestCommentViewModel(private val repository: CommentRepository) : ViewModel() {

    val comments: LiveData<List<LatestCommentModel>> get() = repository.comments
    private val fUser : FirebaseUser = FirebaseAuth.getInstance().currentUser!!

    fun fetchComments(reviewId: String) {
        repository.fetchComments(reviewId)
    }

    fun addComment(reviewId: String, commentText: String) {
        repository.addComment(fUser, reviewId, commentText)
    }

    fun fetchSpecificClickedReviews(reviewId: String, callback: (String, String, String, ReviewModel) -> Unit) {
        repository.fetchSpecificClickedReviews(reviewId, callback)
    }

    class LatestCommentViewModelFactory(private val repository: CommentRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(LatestCommentViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return LatestCommentViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

}
