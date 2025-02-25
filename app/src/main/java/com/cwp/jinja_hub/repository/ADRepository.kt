package com.cwp.jinja_hub.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.cwp.jinja_hub.model.ADModel
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


class ADRepository {

    // Existing properties
    private val database = FirebaseDatabase.getInstance().getReference("AD")
    private val storageReference = FirebaseStorage.getInstance().reference
    private val storage = FirebaseStorage.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val fUser = auth.currentUser
    private val userRef = FirebaseDatabase.getInstance().getReference("Users")



    // Callback for editing a review
    interface EditADCallback {
        fun onSuccess()
        fun onFailure(exception: Exception)
    }

    // Function to edit a review
    fun editAD(
        context: Context,
        adId: String,
        adType: String,
        ad: ADModel,
        newImages: List<Uri>, // Paths of new images to upload
        imagesToKeep: List<String>, // Existing images to keep
        callback: EditADCallback
    ) {
        database.child(adType).child(adId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    callback.onFailure(Exception("AD not found"))
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

                // Define function to proceed with updating the ad
                fun updateAdWithNewImages(uploadedImageUrls: List<String>?) {
                    val updatedMediaUrls = (uploadedImageUrls ?: imagesToKeep).distinct()
                    val updatedAD = mapOf(
                        "posterId" to ad.posterId,
                        "adId" to ad.adId,
                        "adType" to ad.adType,
                        "description" to ad.description,
                        "city" to ad.city,
                        "state" to ad.state,
                        "country" to ad.country,
                        "amount" to ad.amount,
                        "phone" to ad.phone,
                        "productName" to ad.productName,
                        "timestamp" to ad.timestamp,
                        "mediaUrl" to updatedMediaUrls
                    )

                    database.child(adType).child(adId).updateChildren(updatedAD)
                        .addOnSuccessListener { callback.onSuccess() }
                        .addOnFailureListener { exception -> callback.onFailure(exception) }
                }

                // Skip image deletion if no images need to be removed
                if (imagesToDelete.isEmpty()) {
                    // Skip removeImagesFromStorage and directly handle new images
                    if (newImages.isEmpty()) {
                        // No new images, directly update the ad
                        updateAdWithNewImages(null)
                    } else {
                        // Upload new images
                        uploadNewImages(context, newImages) { uploadedImageUrls, error ->
                            if (error != null) {
                                callback.onFailure(error)
                            } else {
                                updateAdWithNewImages(uploadedImageUrls)
                            }
                        }
                    }
                } else {
                    // Remove unwanted images from Firebase Storage
                    removeImagesFromStorage(imagesToDelete) { success ->
                        if (!success) {
                            callback.onFailure(Exception("Failed to delete old images"))
                        } else {
                            // Proceed with new image upload if needed
                            if (newImages.isEmpty()) {
                                updateAdWithNewImages(null)
                            } else {
                                uploadNewImages(context, newImages) { uploadedImageUrls, error ->
                                    if (error != null) {
                                        callback.onFailure(error)
                                    } else {
                                        updateAdWithNewImages(uploadedImageUrls)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback.onFailure(error.toException())
                Log.e("EditAD", "Failed to edit ad: ${error.message}")
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
        val storageRef = storage.reference.child("ad_images")
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



    private fun updateADDetails(
        adId: String,
        adType: String,
        updatedReview: ReviewModel,
        onComplete: (Boolean) -> Unit
    ) {
        database.child(adType).child(adId).setValue(updatedReview).addOnCompleteListener { task ->
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




    private fun updateADImages(
        adId: String,
        adType: String,
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
            removeDeletedUri(adId, adType, oldImageUrls) { deleteSuccess ->
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


    private fun removeDeletedUri(adId: String,adType: String, oldImageUrls: List<String>, callback: (Boolean) -> Unit)
    {
        // remove the deleted images from the list of images in the firebase database
        database.child(adType).child(adId).child("mediaUrl").addListenerForSingleValueEvent(object : ValueEventListener{
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

    fun updateFullAD(
        adId: String,
        adType: String,
        updatedReview: ReviewModel,
        oldImageUrls: List<String>,
        newImageUris: List<Uri>,
        onComplete: (Boolean, String?) -> Unit
    ) {
        updateADImages(adId, adType, oldImageUrls, newImageUris) { newImageUrls, errorMessage ->
            if (errorMessage != null) {
                onComplete(false, errorMessage)
                return@updateADImages
            }

            updatedReview.mediaUrl = newImageUrls ?: listOf()
            updateADDetails(adId, adType, updatedReview) { updateSuccess ->
                if (updateSuccess) {
                    onComplete(true, null)
                } else {
                    onComplete(false, "Failed to update ad details.")
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
            val fileRef = storageReference.child("ad_images/$fileName")

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


    fun submitADToFirebase(ad: ADModel, adType: String, onComplete: (Boolean) -> Unit) {
        val adId = ad.adId ?: database.push().key ?: UUID.randomUUID().toString()
        ad.adId = adId

        database.child(adType).child(adId).setValue(ad).addOnCompleteListener { task ->
            onComplete(task.isSuccessful)
        }
    }


    fun fetchPopularAD(adType: String, callback: (String, String, String, ADModel) -> Unit) {
        database.keepSynced(true)
        database.child(adType).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (adSnapshot in snapshot.children) {
                        val ad = adSnapshot.getValue(ADModel::class.java)
                        if (ad != null) {
                            fetchUserDetails(ad.posterId) { fullName, username, profileImage , isVerified->
                                callback(fullName, username, profileImage, ad)
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

    fun fetchMyADs(adType: String, callback: (String, String, String, ADModel) -> Unit) {
        val userId = fUser?.uid ?: return  // Safely check if user is logged in
        val myADRef = database.child(adType).orderByChild("posterId").equalTo(userId)
        myADRef.keepSynced(true)


        myADRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (adSnapshot in snapshot.children) {
                    val ad = adSnapshot.getValue(ADModel::class.java)
                    if (ad?.posterId != null) { // Ensure posterId is not null
                        fetchUserDetails(ad.posterId) { fullName, username, profileImage, _ ->
                            callback(fullName, username, profileImage, ad)  // Ignore 'isVerified' since it's not needed
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error properly (e.g., logging)
            }
        })
    }



    fun deleteAD(adId: String, adType: String, callback: (Boolean) -> Unit) {
        // Reference to the mediaUrl field
        val mediaUrlRef = database.child(adType).child(adId).child("mediaUrl")

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
                        deleteADFromDatabase(adId, adType, callback)
                    }
                } else {
                    // If there are no URLs, just delete the review from the database
                    deleteADFromDatabase(adId, adType, callback)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database read failure
                callback(false)
            }
        })
    }


    // Deleting reviews from database
    private fun deleteADFromDatabase(adId: String, adType: String, callback: (Boolean) -> Unit) {
        database.child(adType).child(adId).removeValue().addOnCompleteListener { task ->
            if (task.isSuccessful)
                callback(task.isSuccessful)
            else
                callback(false)
        }

    }



    //When user click the heart icon it send like to firebase, when clicked again delete the like from the review
    fun likeAD(adId: String, adType: String, userId: String, callback: (String) -> Unit) {
        val likesRef = database.child(adType).child(adId).child("likes")
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
    fun fetchNumberOfLikes(adId: String, adType: String,  callback: (Int) -> Unit) {
        val likesRef = database.child(adType).child(adId).child("likes")
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
    fun checkIfUserLikedAD(adId: String, adType: String, callback: (Boolean) -> Unit) {
        val userId = auth.currentUser?.uid ?: return callback(false)
        val likesRef = database.child(adType).child(adId).child("likes")

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
    fun addNumberOfShares(reviewId: String) {
        val reviewRef = database.child(reviewId)
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
    fun getNumberOfShares(reviewId: String, callback: (Int) -> Unit) {
        val reviewRef = database.child(reviewId)
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


    // Get current user profile image link and full name
    fun getCurrentUserProfile(userId: String, callback: (String, String) -> Unit) {
        fetchUserDetails(userId) { fullName, username, profileImage, isVerified ->
            callback(fullName, profileImage)
        }
    }

    fun fetchSpecificClickedAD(
        adId: String,
        adType: String,
        loadingCallback: (Boolean) -> Unit,
        callback: (ADModel) -> Unit
    ) {
        if (adType.isEmpty() || adId.isEmpty()) {
            Log.e("fetchSpecificClickedAD", "adType or adId is missing.")
            loadingCallback(false)
            return
        }

        // Signal that loading has started
        loadingCallback(true)

        // Firebase query to fetch the ad
        database.child(adType).child(adId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    // Loading finished
                    loadingCallback(false)

                    if (snapshot.exists()) {
                        // Try to parse the ADModel from the snapshot
                        val ad = snapshot.getValue(ADModel::class.java)
                        if (ad != null) {
                            callback(ad)
                        } else {
                            callback(ADModel())
                            Log.e("fetchSpecificClickedAD", "Failed to parse ADModel.")
                        }
                    } else {
                        Log.e("fetchSpecificClickedAD", "No data found for adType: $adType, adId: $adId.")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Loading finished even if an error occurred
                    loadingCallback(false)
                    Log.e("fetchSpecificClickedAD", "Error fetching ad: ${error.message}")
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
        callback: (String, String, String, Boolean) -> Unit
    ) {
        userRef.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val fullName = snapshot.child("fullName").value.toString()
                    val username = snapshot.child("username").value.toString()
                    val profileImage = snapshot.child("profileImage").value.toString()
                    val isVerified = snapshot.child("isVerified").value.toString().toBoolean()
                    callback(fullName, username, profileImage, isVerified)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Log error
            }
        })
    }


    fun filterAdsByLocation(
        adType: String,
        country: String,
        state: String? = null,
        city: String? = null,
        callback: (List<ADModel>) -> Unit
    ) {
        val query: Query = database.child(adType)
            .orderByChild("country")
            .equalTo(country)

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val filteredAds = mutableListOf<ADModel>()

                for (adSnapshot in snapshot.children) {
                    val ad = adSnapshot.getValue(ADModel::class.java)

                    if (ad != null) {
                        val matchesState = state?.trim()?.lowercase()?.isNotEmpty() != true || ad.state.lowercase() == state.lowercase()
                        val matchesCity = city?.trim()?.isNotEmpty() != true || ad.city == city

                        if (matchesState && matchesCity) {
                            filteredAds.add(ad)
                        }
                    }
                }

                callback(filteredAds)
            }

            override fun onCancelled(error: DatabaseError) {
                callback(emptyList()) // Return an empty list if there is an error
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

