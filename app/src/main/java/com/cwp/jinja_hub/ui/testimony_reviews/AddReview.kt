package com.cwp.jinja_hub.ui.testimony_reviews

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.cwp.jinja_hub.R
import com.cwp.jinja_hub.databinding.FragmentAddReviewBinding
import com.cwp.jinja_hub.model.NotificationModel
import com.cwp.jinja_hub.model.ReviewModel
import com.cwp.jinja_hub.ui.notifications.NotificationsViewModel
import com.cwp.jinja_hub.helpers.SendRegularNotification
import com.google.firebase.auth.FirebaseAuth

class AddReview : Fragment() {

    private var _binding: FragmentAddReviewBinding? = null
    private val binding get() = _binding!!

    private lateinit var reviewViewModel: ReviewViewModel
    private val selectedImageUris = mutableListOf<Uri>()

    private lateinit var notificationViewModel: NotificationsViewModel

    private val pickImagesLauncher = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        if (uris.isNotEmpty()) {
            selectedImageUris.clear()
            removeSelectedImages(binding.imagesContainer)
            selectedImageUris.addAll(uris)
            if (selectedImageUris.size > 5) {
                val alert = AlertDialog.Builder(requireContext())
                alert.setTitle("Warning")
                alert.setMessage("You can only select up to 5 images")
                alert.setPositiveButton("OK") { _, _ ->

                    selectedImageUris.clear()
                    removeSelectedImages(binding.imagesContainer)
                    binding.numOfImages.text = "No images selected"
                }
                alert.create().show()

            }else {
                binding.imagesContainer.visibility = View.VISIBLE
                //binding.ivUploadMedia.visibility = View.VISIBLE

          uris.map { uri ->
                    ImageView(requireContext()).also {
                        it.setImageURI(uri)
                        val params = LinearLayout.LayoutParams(100,100)

                        it.layoutParams = params
                    }
                }.forEach {

              binding.imagesContainer.addView(it)
          }



                binding.numOfImages.text = "${selectedImageUris.size} images selected ✅"
            }
        } else {
            binding.imagesContainer.visibility = View.GONE
            selectedImageUris.clear()
            removeSelectedImages(binding.imagesContainer)
            binding.numOfImages.text = "No images selected"
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddReviewBinding.inflate(inflater, container, false)
        reviewViewModel = ViewModelProvider(this)[ReviewViewModel::class.java]

        setupUI()
        setupObservers()

        return binding.root
    }


     private fun removeSelectedImages (linearLayout: LinearLayout) {
          if(linearLayout.children.count() != 0)
         linearLayout.children.iterator().forEach {
             linearLayout.removeView(it)
         }
     }
    private fun setupUI() {
        binding.uploadMedia.setOnClickListener {
            pickImagesLauncher.launch("image/*") // Opens gallery to select images
        }

        binding.btnSubmitReview.setOnClickListener {
            if (validateInputs()) {
                uploadReview()
            }
        }

        // Handling back button press from toolbar
        binding.backButton.setOnClickListener{
            // Go back to previous fragment that loaded this
            loadFragment(Reviews())
        }

        // Handling back pressed
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object: OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                loadFragment(Reviews())
            }
        })
    }

    private fun setupObservers() {
        reviewViewModel.uploadProgress.observe(viewLifecycleOwner) { isUploading ->
            binding.progressBar.visibility = if (isUploading) View.VISIBLE else View.GONE
            binding.btnSubmitReview.isEnabled = !isUploading
        }

        reviewViewModel.uploadSuccess.observe(viewLifecycleOwner) { success ->
            if (success) {
                Toast.makeText(requireContext(), "Review uploaded successfully!", Toast.LENGTH_SHORT).show()
                binding.imagesContainer.visibility = View.GONE
                clearInputs()
            }
        }

        reviewViewModel.uploadError.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun validateInputs(): Boolean {
        val vidLink = binding.etReviewTitle.text.toString().trim()
        val description = binding.etReviewDescription.text.toString().trim()
        val rating = binding.ratingBar.rating

        return when {

            description.isEmpty() -> {
                binding.etReviewDescription.error = "Description is required"
                false
            }
            rating == 0f -> {
                Toast.makeText(requireContext(), "Please rate the review", Toast.LENGTH_SHORT).show()
                false
            }
            selectedImageUris.isEmpty() -> {
                Toast.makeText(requireContext(), "Please select at least one image", Toast.LENGTH_SHORT).show()
                false
            }
            else -> true
        }
    }

    private fun uploadReview() {
        val vidLink = binding.etReviewTitle.text.toString().trim()
        val description = binding.etReviewDescription.text.toString().trim()
        val rating = binding.ratingBar.rating

        val review = ReviewModel(
            posterId = FirebaseAuth.getInstance().currentUser?.uid ?: "",
            vidLink = vidLink,
            description = description,
            rating = rating
        )

        reviewViewModel.uploadReviewWithImages(review, selectedImageUris)

        // Show loading indicator
        binding.progressBar.visibility = View.VISIBLE
        binding.btnSubmitReview.isEnabled = false

        reviewViewModel.uploadProgress.observe(viewLifecycleOwner) { isUploading ->
            if (isUploading) {
                binding.progressBar.visibility = View.VISIBLE
                binding.btnSubmitReview.isEnabled = false
            } else {
                binding.progressBar.visibility = View.GONE
                binding.btnSubmitReview.isEnabled = true
            }
        }

        reviewViewModel.uploadSuccess.observe(viewLifecycleOwner) { success ->
            if (success) {
                val notification = NotificationModel(
                    posterId = FirebaseAuth.getInstance().currentUser?.uid ?: "",
                    content = "Congratulation! Your testimony is now live and ready to be viewed by others. Go to my testimony to view it.",
                    isRead = false,
                    timestamp = System.currentTimeMillis()
                )
                val sendRegularNotification = SendRegularNotification()
                sendRegularNotification.sendNotification(FirebaseAuth.getInstance().currentUser?.uid ?: "", this, notification)
                Toast.makeText(requireContext(), "Review uploaded successfully!", Toast.LENGTH_SHORT).show()
                clearInputs()
            } else {
                Toast.makeText(requireContext(), "Failed to upload review.", Toast.LENGTH_SHORT).show()
            }
        }

        reviewViewModel.uploadError.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun clearInputs() {
        binding.etReviewTitle.text.clear()
        binding.etReviewDescription.text.clear()
        binding.ratingBar.rating = 0f
        selectedImageUris.clear()
        binding.imagesContainer.visibility = View.GONE
        binding.numOfImages.text = "No images selected"
    }

    private fun loadFragment(fragment: Fragment){
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.replace(R.id.home_container, fragment)
        transaction.addToBackStack(null)
        transaction.isAddToBackStackAllowed
        transaction.commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
