package com.cwp.jinja_hub.ui.testimony_reviews.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.cwp.jinja_hub.adapters.MyReviewAdapter
import com.cwp.jinja_hub.databinding.FragmentMyReviewBinding
import com.cwp.jinja_hub.model.ReviewModel
import com.cwp.jinja_hub.repository.ReviewRepository
import com.cwp.jinja_hub.ui.multi_image_viewer.ViewAllImagesActivity
import com.cwp.jinja_hub.ui.testimony_reviews.ReviewViewModel
import com.cwp.jinja_hub.ui.testimony_reviews.activities.EditReviewActivity
import com.cwp.jinja_hub.ui.testimony_reviews.fragments.comments.LatestCommentsActivity

class MyReviewFragment : Fragment() {
    private var _binding: FragmentMyReviewBinding? = null
    private val binding get() = _binding!!
    private val myReviewViewModel: ReviewViewModel by viewModels()

    private lateinit var adapter: MyReviewAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyReviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize RecyclerView
        setupRecyclerView()


        // Observe LiveData from ViewModel
        myReviewViewModel.myReviews.observe(viewLifecycleOwner) { reviews ->
            updateRecyclerView(reviews)
        }



        // observe the number of comment from viewmodel


        // fetch reviews from ViewModel
        myReviewViewModel.fetchMyReviews()
    }

    private fun setupRecyclerView() {
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = MyReviewAdapter(
            reviews = mutableListOf(),
            onCommentClickListener = { review -> handleCommentClick(review) },
            onProfileClickListener = { review -> handleProfileClick(review) },
            onDescriptionClickListener = { review -> handleDescriptionClicked(review) },
            onHomeImageClickListener = { review -> gotoViewAllImagesActivity(review) },
            onEditReviewClickListener = { review -> goToEditReviewActivity(review) },
            onDeleteReviewClickListener = { review ->gotoDeleteReviewActivity(review) },
            popularRepository = ReviewRepository()
        )
        binding.recyclerView.adapter = adapter
    }

    private fun updateRecyclerView(reviews: MutableList<ReviewModel>) {
        // Show progress bar initially
        binding.progressBar.visibility = if (reviews.isEmpty()) View.VISIBLE else View.GONE
        binding.indicator.visibility = if (reviews.isEmpty()) View.VISIBLE else View.GONE


            adapter = MyReviewAdapter(
                reviews = reviews,
                onCommentClickListener = { review -> handleCommentClick(review) },
                onProfileClickListener = { review -> handleProfileClick(review) },
                onDescriptionClickListener = { review -> handleDescriptionClicked(review) },
                onHomeImageClickListener = { review -> gotoViewAllImagesActivity(review) },
                onEditReviewClickListener = { review -> goToEditReviewActivity(review) },
                onDeleteReviewClickListener = { review -> gotoDeleteReviewActivity(review) },
                popularRepository = ReviewRepository()
            )

            adapter.submitList(reviews)
            binding.recyclerView.adapter = adapter
    }

    private fun handleCommentClick(review: ReviewModel) {
        // Handle comment click
        goToLatestCommentsActivity(review)
    }

    private fun handleDescriptionClicked(review: ReviewModel) {
        // handle description click
        goToLatestCommentsActivity(review)
    }

    private fun gotoViewAllImagesActivity(review: ReviewModel) {
        // Handle go to all images activity
        val intent = Intent(requireActivity(), ViewAllImagesActivity::class.java)
        intent.putExtra("id", review.posterId)
        intent.putStringArrayListExtra(
            ViewAllImagesActivity.EXTRA_IMAGE_URLS,
            review.mediaUrl?.let { ArrayList(it) })
        startActivity(intent)

    }

    private fun goToEditReviewActivity(review: ReviewModel){
        val intent = Intent(requireActivity(), EditReviewActivity::class.java)
        intent.putExtra("id", review.posterId)
        intent.putExtra("reviewId", review.reviewId)
        intent.putStringArrayListExtra(
            "review_images",
            review.mediaUrl?.let { ArrayList(it) })
        intent.putExtra("description", review.description)
        intent.putExtra("vidLink", review.vidLink)
        intent.putExtra("rating", review.rating)
        startActivity(intent)
    }

    private fun gotoDeleteReviewActivity(review: ReviewModel) {
        val alertDialog = AlertDialog.Builder(requireContext())
        alertDialog.setTitle("Delete Review")
        alertDialog.setMessage("Are you sure you want to delete this review?")
        alertDialog.setPositiveButton("Yes") { dialog, _ ->
            myReviewViewModel.deleteReview(review.reviewId ?: "") { success ->
                if (success) {
                    // Show success message
                    Toast.makeText(requireContext(), "Review deleted successfully!", Toast.LENGTH_SHORT).show()

                    // Update the RecyclerView after successful deletion
                    val updatedReviews = myReviewViewModel.myReviews.value?.toMutableList() ?: mutableListOf()
                    updatedReviews.remove(review) // Remove the deleted review from the list

                    // Notify the adapter with updated list
                    adapter.refreshReviews(updatedReviews)

                    // Optionally, call fetchMyReviews to refresh the list from the source
                    // myReviewViewModel.fetchMyReviews()

                } else {
                    Toast.makeText(requireContext(), "Failed to delete review", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
        }
        alertDialog.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }
        alertDialog.create().show()
    }


    private fun handleHeartClick(review: ReviewModel) {
        // Handle heart click
    }

    private fun handleProfileClick(review: ReviewModel) {
        // Handle profile click
    }

    private fun goToLatestCommentsActivity(review: ReviewModel) {
        val intent = Intent(requireActivity(), LatestCommentsActivity::class.java)
        intent.putExtra("REVIEW_ID", review.reviewId)
        intent.putExtra("id", review.posterId)
        intent.putStringArrayListExtra(
            ViewAllImagesActivity.EXTRA_IMAGE_URLS,
            review.mediaUrl?.let { ArrayList(it) })
        requireActivity().startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onStart() {
        super.onStart()
        myReviewViewModel.fetchMyReviews()
    }
}