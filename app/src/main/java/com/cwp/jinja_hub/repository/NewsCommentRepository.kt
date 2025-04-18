package com.cwp.jinja_hub.com.cwp.jinja_hub.repository

import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.cwp.jinja_hub.com.cwp.jinja_hub.model.NewsModel
import com.cwp.jinja_hub.model.LatestCommentModel
import com.cwp.jinja_hub.model.ReviewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*

class NewsCommentRepository {

    private val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().reference.child("News")
    private val database = FirebaseDatabase.getInstance().getReference("News")
    private val userRef : DatabaseReference = FirebaseDatabase.getInstance().reference.child("Users");



    private val _comments = MutableLiveData<List<LatestCommentModel>>()
    val comments: LiveData<List<LatestCommentModel>> get() = _comments



    fun fetchComments(reviewId: String) {
        databaseReference.keepSynced(true)
        databaseReference.child(reviewId).child("comments")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val commentList = mutableListOf<LatestCommentModel>()
                    for (commentSnapshot in snapshot.children) {
                        val comment = commentSnapshot.getValue(LatestCommentModel::class.java)
                        comment?.let { commentList.add(it) }
                    }
                    _comments.postValue(commentList)
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                }
            })
    }

    fun fetchAllSenderId(callback: (List<String>) -> Unit) {
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val senderIdSet = mutableSetOf<String>()

                for (commentSnapshot in snapshot.children) {
                    val comment = commentSnapshot.getValue(LatestCommentModel::class.java)
                    comment?.let {
                        if (it.senderId != FirebaseAuth.getInstance().currentUser?.uid) {
                            senderIdSet.add(it.senderId)
                        }
                    }
                }

                callback(senderIdSet.toList()) // ✅ Convert Set to List and return
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("fetchAllSenderId", "Error fetching sender IDs: ${error.message}")
            }
        })
    }


    fun deleteComment(comment: LatestCommentModel, newsId: String, callback: (Boolean) -> Unit) {
        val database = FirebaseDatabase.getInstance().getReference("News").child(newsId).child("comments")

        database.child(comment.commentId).removeValue()
            .addOnSuccessListener {
                callback(true) // Notify UI that deletion was successful
            }
            .addOnFailureListener {
                callback(false)
            }
    }




    fun fetchSpecificClickedNews(newsId: String, callback: (String, String, String, NewsModel?) -> Unit) {
        databaseReference.keepSynced(true)

        if (newsId.isEmpty()) {
            Log.e("NewsRepo", "fetchSpecificClickedNews failed: newsId is empty")
            callback("", "", "", null)
            return
        }

        Log.d("NewsRepo", "Fetching news details for ID: $newsId")

        databaseReference.child(newsId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    Log.e("NewsRepo", "News with ID $newsId not found in Firebase.")
                    callback("", "", "", null)
                    return
                }

                val news = snapshot.getValue(NewsModel::class.java)
                if (news != null) {
                    fetchUserDetails(news.posterId) { fullName, username, profileImage ->
                        Log.d("NewsRepo", "News fetched successfully: ${news.header}")
                        callback(fullName, username, profileImage, news)
                    }
                } else {
                    Log.e("NewsRepo", "Failed to parse NewsModel from Firebase")
                    callback("", "", "", null)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("NewsRepo", "fetchSpecificClickedNews failed: ${error.message}")
                callback("", "", "", null)
            }
        })
    }


    private fun fetchUserDetails(
        userId: String,
        callback: (String, String, String) -> Unit
    ) {
        userRef.keepSynced(true)
        userRef.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val fullName = snapshot.child("name").value.toString()
                val username = snapshot.child("username").value.toString()
                val profileImage = snapshot.child("profileImage").value.toString()
                callback(fullName, username, profileImage)
            }

            override fun onCancelled(error: DatabaseError) {
                // Log error
            }
        })
    }

    fun addComment(fUser: FirebaseUser, newsId: String, commentText: String, callback: (Boolean) -> Unit) {
        val commentId = databaseReference.child(newsId).child("comments").push().key ?: return
        userRef.child(fUser.uid).addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val name = snapshot.child("fullName").value.toString()
                val imageUrl = snapshot.child("profileImage").value.toString()
                val comment = LatestCommentModel(
                    fUser.uid,
                    name,
                    commentId,
                    imageUrl,
                    commentText,
                    System.currentTimeMillis()
                )
                databaseReference.child(newsId).child("comments").child(commentId).setValue(comment)
                    .addOnSuccessListener {
                        // Comment added successfully
                        callback(true)
                    }
                    .addOnFailureListener {
                        // Handle error
                        callback(false)
                    }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })


    }

    // Get current user profile ImageUrl
}
