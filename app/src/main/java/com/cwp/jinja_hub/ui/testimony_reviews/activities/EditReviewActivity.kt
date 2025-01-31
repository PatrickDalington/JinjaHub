package com.cwp.jinja_hub.ui.testimony_reviews.activities

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.cwp.jinja_hub.adapters.image_viewer.EditImagesAdapter
import com.cwp.jinja_hub.databinding.ActivityEditReviewBinding
import com.cwp.jinja_hub.model.ReviewModel
import com.cwp.jinja_hub.repository.ReviewRepository
import com.cwp.jinja_hub.ui.testimony_reviews.ReviewViewModel
import com.google.firebase.auth.FirebaseAuth

class EditReviewActivity : AppCompatActivity() {

    private lateinit var _binding: ActivityEditReviewBinding
    private val binding get() = _binding

    private lateinit var adapter: EditImagesAdapter
    private lateinit var imageUrls: MutableList<String>
    private lateinit var listOfImagesToDelete: MutableList<String>
    private lateinit var reviewId: String

    private val reviewViewModel: ReviewViewModel by viewModels()

    private val selectedImageUris = mutableListOf<Uri>()

    private val pickImagesLauncher = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        if (uris.size > 5) {
            Toast.makeText(this, "You can only select up to 5 images", Toast.LENGTH_SHORT).show()
        } else if (uris.isNotEmpty()) {
            selectedImageUris.clear()
            selectedImageUris.addAll(uris)
            binding.ivUploadMedia.visibility = View.VISIBLE
            binding.ivUploadMedia.setImageURI(uris.first())
            binding.numOfImages.text = "${selectedImageUris.size} images selected ✅"
        } else {
            binding.ivUploadMedia.visibility = View.GONE
            selectedImageUris.clear()
            binding.numOfImages.text = "No images selected"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize view binding
        _binding = ActivityEditReviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize lists
        imageUrls = mutableListOf()
        listOfImagesToDelete = mutableListOf()

        // Receive all incoming intent data
        val intent = intent
        reviewId = intent.getStringExtra("reviewId").orEmpty()
        val description = intent.getStringExtra("description").orEmpty()
        val vidLink = intent.getStringExtra("vidLink").orEmpty()
        val rating = intent.getFloatExtra("rating", 0.0f)
        val images = intent.getStringArrayListExtra("review_images")

        // Populate the existing image list
        images?.let { imageUrls.addAll(it) }

        // Populate the views with the received data
        binding.etReviewDescription.setText(description)
        binding.ratingBar.rating = rating
        binding.etReviewVideoLink.setText(vidLink)

        // Set up UI interactions
        setupUI()

        // Set up the RecyclerView and adapter
        binding.editImageRecycler.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.editImageRecycler.setHasFixedSize(true)
        adapter = EditImagesAdapter(imageUrls) { imageToRemove, pos ->
            imageUrls.remove(imageToRemove)
            adapter.notifyItemRemoved(pos)
            listOfImagesToDelete.add(imageToRemove)
            binding.editImageRecycler.adapter?.notifyItemRangeChanged(0, imageUrls.size)
            if (imageUrls.isEmpty()) {
                adapter.notifyDataSetChanged()
                binding.imageTextIndicator.visibility = View.GONE
                binding.editImageRecycler.visibility = View.GONE
            }else{
                binding.imageTextIndicator.visibility = View.VISIBLE
                binding.editImageRecycler.visibility = View.VISIBLE
            }

        }
        binding.editImageRecycler.adapter = adapter


        // Check if imageUrls is empty and show appropriate views
        if (imageUrls.isEmpty()) {
            binding.imageTextIndicator.visibility = View.GONE
            binding.editImageRecycler.visibility = View.GONE
        }else{
            binding.imageTextIndicator.visibility = View.VISIBLE
            binding.editImageRecycler.visibility = View.VISIBLE
        }
    }

    private fun setupUI() {
        // Image picker launcher
        binding.uploadMedia.setOnClickListener {
            pickImagesLauncher.launch("image/*")
        }

        // Update review button click listener
        binding.btnUpdateReview.setOnClickListener {
            if (validateInputs()) {
                uploadReview()
            }
        }

        // Handling back button press from toolbar
        binding.backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Handle physical back button press
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish() // Ends the activity
            }
        })
    }

    private fun validateInputs(): Boolean {
        val description = binding.etReviewDescription.text.toString().trim()
        val rating = binding.ratingBar.rating

        return when {
            description.isEmpty() -> {
                binding.etReviewDescription.error = "Testimony is required"
                false
            }
            rating == 0f -> {
                Toast.makeText(this, "Please rate jinja", Toast.LENGTH_SHORT).show()
                false
            }
            selectedImageUris.isEmpty() && imageUrls.isEmpty() -> {
                Toast.makeText(this, "Please select at least one image", Toast.LENGTH_SHORT).show()
                false
            }
            else -> true
        }
    }

    private fun uploadReview() {
        val vidLink = binding.etReviewVideoLink.text.toString().trim()
        val description = binding.etReviewDescription.text.toString().trim()
        val rating = binding.ratingBar.rating

        val review = ReviewModel(
            posterId = FirebaseAuth.getInstance().currentUser?.uid.orEmpty(),
            vidLink = vidLink,
            description = description,
            rating = rating
        )

        // Show loading indicator
        binding.progressBar.visibility = View.VISIBLE
        binding.btnUpdateReview.isEnabled = false

        Log.d("EditReviewActivity", "Starting review update for ID: $reviewId")
        Log.d("EditReviewActivity", "Selected image URIs: $selectedImageUris")
        Log.d("EditReviewActivity", "Existing image URLs: $imageUrls")

        // Call ViewModel to update the review
        reviewViewModel.editReview(
            this,
            reviewId, review, selectedImageUris, imageUrls, object : ReviewRepository.EditReviewCallback {
                override fun onSuccess() {
                    Log.d("EditReviewActivity", "Review updated successfully!")
                    Toast.makeText(this@EditReviewActivity, "Review updated successfully!", Toast.LENGTH_SHORT).show()
                    clearInputs()
                }

                override fun onFailure(exception: Exception) {
                    Log.e("EditReviewActivity", "Failed to update review: ${exception.message}")
                    Toast.makeText(this@EditReviewActivity, "Failed to update review: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
            }
        )

        // Observe progress
        reviewViewModel.uploadProgress.observe(this) { isUploading ->
            binding.progressBar.visibility = if (isUploading) View.VISIBLE else View.GONE
            binding.btnUpdateReview.isEnabled = !isUploading
        }

        reviewViewModel.uploadError.observe(this) { error ->
            error?.let {
                Log.e("EditReviewActivity", "Error during upload: $it")
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun clearInputs() {
        binding.etReviewVideoLink.text.clear()
        binding.etReviewDescription.text.clear()
        binding.ratingBar.rating = 0f
        selectedImageUris.clear()
        imageUrls.clear()
        binding.imageTextIndicator.visibility = View.GONE
        adapter.notifyDataSetChanged()
        binding.progressBar.visibility = View.GONE
        binding.ivUploadMedia.visibility = View.GONE
        binding.numOfImages.text = "No images selected"
        binding.btnUpdateReview.isEnabled = false
    }
}
