package com.cwp.jinja_hub.ui.testimony_reviews.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.cwp.jinja_hub.adapters.PopularReviewAdapter
import com.cwp.jinja_hub.databinding.FragmentPopularBinding
import com.cwp.jinja_hub.model.ReviewModel
import com.cwp.jinja_hub.repository.ReviewRepository
import com.cwp.jinja_hub.ui.message.MessageActivity
import com.cwp.jinja_hub.ui.multi_image_viewer.ViewAllImagesActivity
import com.cwp.jinja_hub.ui.testimony_reviews.ReviewViewModel
import com.cwp.jinja_hub.ui.testimony_reviews.fragments.comments.LatestCommentsActivity
import com.google.firebase.auth.FirebaseAuth

class Popular : Fragment() {

    private var _binding: FragmentPopularBinding? = null
    private val binding get() = _binding!!
    private val popularViewModel: ReviewViewModel by viewModels()

    private lateinit var adapter: PopularReviewAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPopularBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()

        // Observe LiveData only once
        popularViewModel.popularReviews.observe(viewLifecycleOwner) { reviews ->
            updateRecyclerView(reviews)
            //binding.swipeRefreshLayout.isRefreshing = false
        }

        // Show loader and fetch data
        binding.progressBar.visibility = View.VISIBLE
        popularViewModel.fetchPopularReviews()

        binding.smartRefreshLayout.setOnRefreshListener{
            popularViewModel.fetchPopularReviews() // Just refresh data, don't reattach observer
        }
    }

    private fun setupRecyclerView() {
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = PopularReviewAdapter(
            reviews = mutableListOf(),
            onCommentClickListener = { review -> handleCommentClick(review) },
            onProfileClickListener = { review -> handleProfileClick(review) },
            onDescriptionClickListener = { review -> handleDescriptionClicked(review) },
            onNameClickListener = { review -> gotoMessageActivity(review) },
            popularRepository = ReviewRepository()
        )
        binding.recyclerView.isNestedScrollingEnabled = false
        binding.recyclerView.adapter = adapter
    }

    private fun updateRecyclerView(reviews: MutableList<ReviewModel>) {
        adapter.refreshReviews(reviews) // Update existing adapter, don't recreate it
        binding.progressBar.visibility = View.GONE
        binding.recyclerView.visibility = View.VISIBLE
    }

    private fun handleCommentClick(review: ReviewModel) {
        goToLatestCommentsActivity(review)
    }

    private fun handleDescriptionClicked(review: ReviewModel) {
        goToLatestCommentsActivity(review)
    }

    private fun gotoMessageActivity(review: ReviewModel) {
        handleProfileClick(review)
    }

    private fun handleProfileClick(review: ReviewModel) {
        if (review.posterId != FirebaseAuth.getInstance().currentUser?.uid) {
            Intent(requireActivity(), MessageActivity::class.java).apply {
                putExtra("receiverId", review.posterId)
                requireActivity().startActivity(this)
            }
        } else {
            Toast.makeText(requireActivity(), "You cannot message yourself", Toast.LENGTH_SHORT).show()
        }
    }

    private fun goToLatestCommentsActivity(review: ReviewModel) {
        val intent = Intent(requireActivity(), LatestCommentsActivity::class.java)
        intent.putExtra("REVIEW_ID", review.reviewId)
        intent.putExtra("id", review.posterId)
        intent.putStringArrayListExtra(ViewAllImagesActivity.EXTRA_IMAGE_URLS,
            review.mediaUrl?.let { ArrayList(it) })
        requireActivity().startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
