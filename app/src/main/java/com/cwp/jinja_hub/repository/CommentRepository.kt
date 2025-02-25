package com.cwp.jinja_hub.repository

import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.cwp.jinja_hub.model.LatestCommentModel
import com.cwp.jinja_hub.model.ReviewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*

class CommentRepository {

    private val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().reference.child("Reviews")
    private val database = FirebaseDatabase.getInstance().getReference("Reviews")
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

    fun fetchAllSenderId(reviewId: String, callback: (List<String>) -> Unit) {
        databaseReference.child(reviewId).child("comments").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val senderIdSet = mutableSetOf<String>() // ✅ Use a Set to prevent duplicates

                for (commentSnapshot in snapshot.children) {
                    val comment = commentSnapshot.getValue(LatestCommentModel::class.java)
                    comment?.let {
                        if (it.senderId != FirebaseAuth.getInstance().currentUser?.uid) {
                            senderIdSet.add(it.senderId) // ✅ Add only unique IDs
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


    fun deleteComment(comment: LatestCommentModel, reviewId: String, callback: (Boolean) -> Unit) {
        val database = FirebaseDatabase.getInstance().getReference("Reviews").child(reviewId).child("comments")

        database.child(comment.commentId).removeValue()
            .addOnSuccessListener {
                callback(true) // Notify UI that deletion was successful
            }
            .addOnFailureListener {
                callback(false)
            }
    }




    fun fetchSpecificClickedReviews(reviewId: String, callback: (String, String, String, ReviewModel) -> Unit) {
        database.keepSynced(true)
        database.child(reviewId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val review = snapshot.getValue(ReviewModel::class.java)
                if (review != null) {
                    fetchUserDetails(review.posterId) { fullName, username, profileImage ->
                        callback(fullName, username, profileImage, review)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Log error
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
                val fullName = snapshot.child("fullName").value.toString()
                val username = snapshot.child("username").value.toString()
                val profileImage = snapshot.child("profileImage").value.toString()
                callback(fullName, username, profileImage)
            }

            override fun onCancelled(error: DatabaseError) {
                // Log error
            }
        })
    }

    fun addComment(fUser: FirebaseUser, reviewId: String, commentText: String, callback: (Boolean) -> Unit) {
        val commentId = databaseReference.child(reviewId).child("comments").push().key ?: return
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
                databaseReference.child(reviewId).child("comments").child(commentId).setValue(comment)
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
