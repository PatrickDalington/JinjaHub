package com.cwp.jinja_hub.ui.testimony_reviews.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.cwp.jinja_hub.adapters.PopularReviewAdapter
import com.cwp.jinja_hub.databinding.FragmentPopularBinding
import com.cwp.jinja_hub.model.ReviewModel
import com.cwp.jinja_hub.repository.ReviewRepository
import com.cwp.jinja_hub.ui.image_viewer.ViewAllImagesActivity
import com.cwp.jinja_hub.ui.testimony_reviews.ReviewViewModel
import com.cwp.jinja_hub.ui.testimony_reviews.fragments.comments.LatestCommentsActivity

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

        // Initialize RecyclerView
        setupRecyclerView()

        // Observe LiveData from ViewModel
        popularViewModel.popularReviews.observe(viewLifecycleOwner) { reviews ->
            updateRecyclerView(reviews)
        }

        // observe the number of comment from viewmodel


        // Show loader and fetch data
        binding.progressBar.visibility = View.VISIBLE
        popularViewModel.fetchPopularReviews()
    }

    private fun setupRecyclerView() {
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = PopularReviewAdapter(
            reviews = mutableListOf(),
            onCommentClickListener = { review -> handleCommentClick(review) },
            onProfileClickListener = { review -> handleProfileClick(review) },
            onDescriptionClickListener = { review -> handleDescriptionClicked(review) },
            onHomeImageClickListener = { review -> gotoViewAllImagesActivity(review) },
            popularRepository = ReviewRepository()
        )
        binding.recyclerView.adapter = adapter
    }

    private fun updateRecyclerView(reviews: MutableList<ReviewModel>) {
        adapter = PopularReviewAdapter(
            reviews = reviews,
            onCommentClickListener = { review -> handleCommentClick(review) },
            onProfileClickListener = { review -> handleProfileClick(review) },
            onDescriptionClickListener = { review -> handleDescriptionClicked(review) },
            onHomeImageClickListener = { review -> gotoViewAllImagesActivity(review) },
            popularRepository = ReviewRepository()
        )
        binding.recyclerView.adapter = adapter
        binding.progressBar.visibility = View.GONE
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
        intent.putStringArrayListExtra(ViewAllImagesActivity.EXTRA_IMAGE_URLS,
            review.mediaUrl?.let { ArrayList(it) })
        startActivity(intent)

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
        intent.putStringArrayListExtra(ViewAllImagesActivity.EXTRA_IMAGE_URLS,
            review.mediaUrl?.let { ArrayList(it) })
        requireActivity().startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onStart() {
        super.onStart()
        popularViewModel.fetchPopularReviews()
    }
}
