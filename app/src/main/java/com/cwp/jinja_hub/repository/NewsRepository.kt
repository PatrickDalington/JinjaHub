package com.cwp.jinja_hub.com.cwp.jinja_hub.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.cwp.jinja_hub.com.cwp.jinja_hub.model.NewsModel
import com.cwp.jinja_hub.model.ReviewModel
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.dynamiclinks.DynamicLink
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.google.firebase.storage.FirebaseStorage
import id.zelory.compressor.Compressor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID


class NewsRepository {

    // Existing properties
    private val database = FirebaseDatabase.getInstance().getReference("News")
    private val storageReference = FirebaseStorage.getInstance().reference
    private val storage = FirebaseStorage.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val fUser = auth.currentUser
    private val userRef = FirebaseDatabase.getInstance().getReference("Users")



    // Callback for editing a review
    interface EditNewsCallback {
        fun onSuccess()
        fun onFailure(exception: Exception)
    }

    // Function to edit a review
    fun editNews(
        context: Context,
        newsId: String,
        news: NewsModel,
        newImages: List<Uri>, // Paths of new images to upload
        imagesToKeep: List<String>, // Existing images to keep
        callback: EditNewsCallback
    ) {
        database.child(newsId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    callback.onFailure(Exception("News not found"))
                    return
                }

                val imagesToDelete = mutableListOf<String>()
                val mediaUrlsSnapshot = snapshot.child("mediaUrl")

                // Collect images to delete
                for (child in mediaUrlsSnapshot.children) {
                    val imageUrl = child.getValue(String::class.java)
                    if (imageUrl != null && !imagesToKeep.contains(imageUrl)) {
                        imagesToDelete.add(imageUrl)
                    }
                }

                // Remove unwanted images from Firebase Storage
                removeImagesFromStorage(imagesToDelete) { success ->
                    if (!success) {
                        callback.onFailure(Exception("Failed to delete old images"))
                        return@removeImagesFromStorage
                    }else{
                        // Upload new images to Firebase Storage
                        uploadNewImages(context, newImages) { uploadedImageUrls, error ->
                            if (error != null) {
                                callback.onFailure(error)
                                return@uploadNewImages
                            }

                            Log.d("NewsReview", "Uploaded image URLs: $uploadedImageUrls")

                            // Update the review in the database
                            val updatedMediaUrls = (imagesToKeep + (uploadedImageUrls ?: emptyList())).distinct()
                            val updatedReview = mapOf(
                                "posterId" to news.posterId,
                                "vidLink" to news.vidLink,
                                "content" to news.content,
                                "mediaUrl" to updatedMediaUrls
                            )

                            database.child(newsId).updateChildren(updatedReview).addOnSuccessListener {
                                callback.onSuccess()
                            }.addOnFailureListener { exception ->
                                callback.onFailure(exception)
                            }
                        }
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                callback.onFailure(error.toException())
                Log.e("EditReview", "Faild to edit review: : ${error.message}")
            }

        })
    }



    // Function to remove images from Firebase Storage
    private fun removeImagesFromStorage(imageUrls: List<String>, onComplete: (Boolean) -> Unit) {

        val storage = FirebaseStorage.getInstance()
        val tasks = imageUrls.map { url ->
            Log.d("DeleteImages", "Deleting image URL: $url")
            val ref = storage.getReferenceFromUrl(url)
            ref.delete()
        }

        Tasks.whenAllComplete(tasks).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("DeleteImages", "All images deleted successfully")
                onComplete(true) // Nothing to delete
            } else {
                Log.e("DeleteImages", "Failed to delete some images", task.exception)
                onComplete(false)
            }
        }
    }


    private fun uploadNewImages(
        context: Context,  // Pass the context to access resources
        imageUris: List<Uri>,
        onComplete: (List<String>?, Exception?) -> Unit
    ) {
        val storageRef = storage.reference.child("news_images")
        val uploadedImageUrls = mutableListOf<String>()

        if (imageUris.isEmpty()) {
            onComplete(emptyList(), null) // No new images to upload
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                imageUris.forEach { uri ->
                    // Convert URI to a temporary file
                    val inputStream = context.contentResolver.openInputStream(uri)
                    val tempFile = File(context.cacheDir, "${UUID.randomUUID()}.jpg")
                    inputStream?.let {
                        tempFile.outputStream().use { outputStream ->
                            it.copyTo(outputStream)
                        }
                    }

                    // Compress the image using Zealotry Compressor
                    val compressedImageFile = Compressor.compress(context, tempFile)

                    // Upload compressed image to Firebase Storage
                    val imageRef = storageRef.child("${UUID.randomUUID()}.jpg")
                    imageRef.putFile(compressedImageFile.toUri()).await()
                    val downloadUri = imageRef.downloadUrl.await()
                    uploadedImageUrls.add(downloadUri.toString())

                    // Delete the temporary files (compressed and original) after uploading
                    tempFile.delete()
                    compressedImageFile.delete()
                }
                withContext(Dispatchers.Main) {
                    onComplete(uploadedImageUrls, null)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("UploadError", "Error uploading images: ${e.localizedMessage}", e)
                    onComplete(null, e)
                }
            }
        }
    }


    // Extension to convert a file path to a URI
    private fun File.toUri(): Uri = Uri.fromFile(this)


    /**
     * Updates a review in Firebase Realtime Database.
     *
     * @param reviewId The ID of the review to update.
     * @param updatedReview The updated review object.
     * @param onComplete Callback invoked with success status after the operation.
     */
    private fun updateNewsDetails(
        newsId: String,
        updatedNews: NewsModel,
        onComplete: (Boolean) -> Unit
    ) {
        database.child(newsId).setValue(updatedNews).addOnCompleteListener { task ->
            onComplete(task.isSuccessful)
        }
    }

    /**
     * Deletes old images from Firebase Storage.
     *
     * @param imageUrls List of image URLs to delete.
     * @param onComplete Callback invoked after the operation with success status.
     */
    private fun deleteOldImages(
        imageUrls: List<String>, // Explicitly require a list of strings
        onComplete: (Boolean) -> Unit
    ) {
        if (imageUrls.isEmpty()) {
            onComplete(true) // Nothing to delete
            return
        }

        var deleteCount = 0
        var hasErrorOccurred = false

        imageUrls.forEach { imageUrl ->
            try {
                // Get a reference to the image using the full URL
                val imageRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl)

                // Attempt to delete the image
                imageRef.delete()
                    .addOnSuccessListener {
                        deleteCount++
                        if (deleteCount == imageUrls.size && !hasErrorOccurred) {
                            onComplete(true) // All images deleted successfully
                        }
                    }
                    .addOnFailureListener {
                        hasErrorOccurred = true
                        deleteCount++
                        if (deleteCount == imageUrls.size) {
                            onComplete(false) // Some images failed to delete
                        }
                    }
            } catch (e: Exception) {
                // Handle cases where the URL is malformed or invalid
                hasErrorOccurred = true
                deleteCount++
                if (deleteCount == imageUrls.size) {
                    onComplete(false)
                }
            }
        }
    }



    /**
     * Updates images for a review by deleting old ones and uploading new ones.
     *
     * @param reviewId The ID of the review being updated.
     * @param oldImageUrls List of URLs of old images to delete.
     * @param newImageUris List of URIs of new images to upload.
     * @param onComplete Callback invoked with updated image URLs or an error message.
     */
    private fun updateNewsImages(
        newsId: String,
        oldImageUrls: List<String>,
        newImageUris: List<Uri>,
        onComplete: (List<String>?, String?) -> Unit
    ) {
        deleteOldImages(oldImageUrls) { deleteSuccess ->
            if (!deleteSuccess) {
                onComplete(null, "Failed to delete old images.")
                return@deleteOldImages
            }

            // Upload new images if any
            if (newImageUris.isEmpty()) {
                onComplete(listOf(), null)
                return@deleteOldImages
            }

           // Call the removeDeletedUri to delete all remove images
            removeDeletedUri(newsId, oldImageUrls) { deleteSuccess ->
                if (!deleteSuccess) {
                    onComplete(null, "Failed to delete old images.")
                    return@removeDeletedUri
                }else{
                    uploadImagesToFirebase(newImageUris, { newImageUrls ->
                        onComplete(newImageUrls, null)
                    }, { errorMessage ->
                        onComplete(null, errorMessage)
                    })
                }
            }
        }
    }


    private fun removeDeletedUri(newsId: String, oldImageUrls: List<String>, callback: (Boolean) -> Unit)
    {
        // remove the deleted images from the list of images in the firebase database
        database.child(newsId).child("mediaUrl").addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for (dataSnapshot in snapshot.children) {
                    val imageUrl = dataSnapshot.getValue(String::class.java)
                    if (imageUrl != null && oldImageUrls.contains(imageUrl)) {
                        dataSnapshot.ref.removeValue().addOnSuccessListener {
                            callback(true)
                        }
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                callback(false)
            }
        })
    }

    /**
     * Handles full review update including updating text information and images.
     *
     * @param reviewId The ID of the review to update.
     * @param updatedReview The updated review object.
     * @param oldImageUrls List of URLs of old images to delete.
     * @param newImageUris List of URIs of new images to upload.
     * @param onComplete Callback invoked with success status or error message.
     */
    fun updateFullNews(
        newsId: String,
        updatedNews: NewsModel,
        oldImageUrls: List<String>,
        newImageUris: List<Uri>,
        onComplete: (Boolean, String?) -> Unit
    ) {
        updateNewsImages(newsId, oldImageUrls, newImageUris) { newImageUrls, errorMessage ->
            if (errorMessage != null) {
                onComplete(false, errorMessage)
                return@updateNewsImages
            }

            updatedNews.mediaUrl = newImageUrls ?: listOf()
            updateNewsDetails(newsId, updatedNews) { updateSuccess ->
                if (updateSuccess) {
                    onComplete(true, null)
                } else {
                    onComplete(false, "Failed to update review details.")
                }
            }
        }
    }

    /**
     * Uploads a list of images to Firebase Storage.
     *
     * @param imageUris List of image URIs to upload.
     * @param onSuccess Callback when upload is successful, returning the list of image URLs.
     * @param onFailure Callback when upload fails, returning the error message.
     */
    fun uploadImagesToFirebase(
        imageUris: List<Uri>,
        onSuccess: (List<String>) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val uploadedImageUrls = mutableListOf<String>()
        val totalImages = imageUris.size
        var successfulUploads = 0

        // Loop through each URI and upload to Firebase Storage
        imageUris.forEach { uri ->
            val fileName = UUID.randomUUID().toString() // Generate a unique name for the file
            val fileRef = storageReference.child("news_images/$fileName")

            fileRef.putFile(uri)
                .addOnSuccessListener {
                    // Retrieve the download URL once the image is uploaded
                    fileRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                        uploadedImageUrls.add(downloadUrl.toString())
                        successfulUploads++

                        // Check if all images are uploaded
                        if (successfulUploads == totalImages) {
                            onSuccess(uploadedImageUrls)
                        }
                    }.addOnFailureListener { error ->
                        onFailure("Failed to retrieve download URL: ${error.message}")
                    }
                }
                .addOnFailureListener { error ->
                    onFailure("Failed to upload image: ${error.message}")
                }
        }
    }

    /**
     * Submits a review to Firebase Realtime Database.
     *
     * @param news The news object to be saved.
     * @param onComplete Callback invoked with success status after the operation.
     */
    fun submitNewsToFirebase(news: NewsModel, onComplete: (Boolean) -> Unit) {
        val nId = news.newsId
        news.newsId = nId

        database.child(nId).setValue(news).addOnCompleteListener { task ->
            onComplete(task.isSuccessful)
        }
    }

    /**
     * Fetches popular reviews and user details from Firebase.
     *
     * @param callback Callback invoked with user details and review data for each review.
     */
    fun fetchPopularNews(callback: (String, String, String, NewsModel) -> Unit) {
        database.keepSynced(true)
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (newsSnapshot in snapshot.children) {
                        val news = newsSnapshot.getValue(NewsModel::class.java)
                        if (news != null) {
                            fetchUserDetails(news.posterId) { fullName, username, profileImage ->
                                callback(fullName, username, profileImage, news)
                            }
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Log error
            }
        })
    }


    // Fetch my reviews only
    fun fetchMyNews(callback: (String, String, String, NewsModel) -> Unit) {
        val myNewsRef = database.orderByChild("posterId").equalTo(fUser!!.uid)
        myNewsRef.keepSynced(true)
        myNewsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Handle the data change
                for (newsSnapshot in snapshot.children) {
                    val news = newsSnapshot.getValue(NewsModel::class.java)
                    if (news != null) {
                        fetchUserDetails(news.posterId) { fullName, username, profileImage ->
                            callback(fullName, username, profileImage, news)
                        }
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                // Handle the error
            }
        })
    }


    fun deleteNews(newsId: String, callback: (Boolean) -> Unit) {
        // Reference to the mediaUrl field
        val mediaUrlRef = database.child(newsId).child("mediaUrl")

        mediaUrlRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val imageUrls = snapshot.children.mapNotNull { it.getValue(String::class.java) }

                if (imageUrls.isNotEmpty()) {
                    // Call the function to delete old images
                    deleteOldImages(imageUrls) { deleteSuccess ->
                        if (!deleteSuccess) {
                            callback(false)
                            return@deleteOldImages
                        }
                        deleteNewsFromDatabase(newsId, callback)
                    }
                } else {
                    // If there are no URLs, just delete the review from the database
                    deleteNewsFromDatabase(newsId, callback)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database read failure
                callback(false)
            }
        })
    }


    // Deleting reviews from database
    private fun deleteNewsFromDatabase(newsId: String, callback: (Boolean) -> Unit) {
        database.child(newsId).removeValue().addOnCompleteListener { task ->
            if (task.isSuccessful)
                callback(task.isSuccessful)
            else
                callback(false)
        }

    }


    // Fetch number of comments for each review
    fun fetchNumberOfNewsComments(newsId: String, callback: (Int) -> Unit) {
        val commentsRef = FirebaseDatabase.getInstance().getReference("News").child(newsId).child("comments")
        commentsRef.keepSynced(true)
        commentsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val commentCount = snapshot.childrenCount.toInt()
                    callback(commentCount)
                }else{
                    callback(0)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    // Fetch total number of reviews
    fun fetTotalNumberOfNews(callback: (Int) -> Unit){
        val newsRef = FirebaseDatabase.getInstance().getReference("News")
        newsRef.keepSynced(true)
        newsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val newsCount = snapshot.childrenCount.toInt()
                    callback(newsCount)
                } else {
                    callback(0)
                }
            }
            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    //When user click the heart icon it send like to firebase, when clicked again delete the like from the review
    fun likeNews(newsId: String, userId: String, callback: (String) -> Unit) {
        val likesRef = database.child(newsId).child("likes")
        likesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                    val likes = snapshot.children.mapNotNull { it.getValue(String::class.java) }
                    if (likes.contains(userId)) {
                        likesRef.child(userId).removeValue().addOnCompleteListener { task ->
                            if (task.isSuccessful)
                                callback("unLike")
                        }
                    } else {
                        likesRef.child(userId).setValue(userId).addOnCompleteListener { task ->
                            if (task.isSuccessful)
                                callback("like")
                        }
                    }

            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    // fetch number of likes
    fun fetchNumberOfLikes(newsId: String, callback: (Int) -> Unit) {
        val likesRef = database.child(newsId).child("likes")
        likesRef.keepSynced(true)
        likesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val likeCount = snapshot.childrenCount.toInt()
                    callback(likeCount)
                }else{
                    callback(0)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Log error or handle failure
            }
        })
    }

    // Checking if user liked review
    fun checkIfUserLikedNews(newsId: String, callback: (Boolean) -> Unit) {
        val userId = auth.currentUser?.uid ?: return callback(false)
        val likesRef = database.child(newsId).child("likes")

        likesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val userLiked = snapshot.hasChild(userId)
                    callback(userLiked)
                }
                else{
                    callback(false)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Log error or handle failure
                callback(false)
            }
        })
    }

    // Add number of shares to firebase database
    fun addNumberOfShares(newsId: String) {
        val reviewRef = database.child(newsId)
        reviewRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val currentShares = snapshot.child("shares").value.toString().toIntOrNull() ?: 0
                    val updatedShares = currentShares + 1
                    reviewRef.child("shares").setValue(updatedShares)
                }
            }
            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    // Get number of shares
    fun getNumberOfShares(newsId: String, callback: (Int) -> Unit) {
        val reviewRef = database.child(newsId)
        reviewRef.keepSynced(true)
        reviewRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val shares = snapshot.child("shares").value.toString().toIntOrNull() ?: 0
                    callback(shares)
                }
            }
            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }


    fun fetchSpecificClickedNews(callback: (String, String, String, ReviewModel) -> Unit) {
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (newsSnapshot in snapshot.children) {
                    val news = newsSnapshot.getValue(ReviewModel::class.java)
                    if (news != null) {
                        fetchUserDetails(news.posterId) { fullName, username, profileImage ->
                            callback(fullName, username, profileImage,  news)
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Log error
            }
        })
    }


    /**
     * Fetches user details based on the user ID.
     *
     * @param userId The ID of the user whose details are to be fetched.
     * @param callback Callback invoked with user details (fullName, username, profileImage).
     */
    fun fetchUserDetails(
        userId: String,
        callback: (String, String, String) -> Unit
    ) {
        userRef.keepSynced(true)
        userRef.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val fullName = snapshot.child("fullName").value.toString()
                    val username = snapshot.child("username").value.toString()
                    val profileImage = snapshot.child("profileImage").value.toString()
                    callback(fullName, username, profileImage)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Log error
            }
        })
    }


    // Add this method to the repository to handle dynamic links
    fun createDynamicLink(reviewId: String, callback: (String) -> Unit) {
        val dynamicLink = FirebaseDynamicLinks.getInstance().createDynamicLink()
            .setLink(Uri.parse("https://jinjahub.com/review/$reviewId"))
            .setDomainUriPrefix("https://jinjahub.page.link")
            .setAndroidParameters(DynamicLink.AndroidParameters.Builder().build())
            .buildDynamicLink()

        callback(dynamicLink.uri.toString())
    }

}

