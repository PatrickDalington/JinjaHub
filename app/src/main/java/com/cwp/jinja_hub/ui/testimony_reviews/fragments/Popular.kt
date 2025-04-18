package com.cwp.jinja_hub.ui.testimony_reviews.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.cwp.jinja_hub.R
import com.cwp.jinja_hub.adapters.PopularReviewAdapter
import com.cwp.jinja_hub.alert_dialogs.ReportBottomSheet
import com.cwp.jinja_hub.alert_dialogs.ReportDialog
import com.cwp.jinja_hub.com.cwp.jinja_hub.listeners.OnLikeStatusChangedListener
import com.cwp.jinja_hub.com.cwp.jinja_hub.ui.message.MessageViewModel
import com.cwp.jinja_hub.databinding.FragmentPopularBinding
import com.cwp.jinja_hub.model.ReviewModel
import com.cwp.jinja_hub.repository.MessageRepository
import com.cwp.jinja_hub.repository.ReviewRepository
import com.cwp.jinja_hub.ui.message.MessageActivity
import com.cwp.jinja_hub.ui.professionals_registration.ProfessionalSignupViewModel
import com.cwp.jinja_hub.ui.testimony_reviews.ReviewViewModel
import com.cwp.jinja_hub.ui.testimony_reviews.fragments.comments.LatestCommentsActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase

class Popular : Fragment(), OnLikeStatusChangedListener {

    private var _binding: FragmentPopularBinding? = null
    private val binding get() = _binding!!
    private val popularViewModel: ReviewViewModel by viewModels()
    private lateinit var messageViewModel: MessageViewModel
    private val userViewModel: ProfessionalSignupViewModel by viewModels()

    lateinit var fUser: FirebaseUser
    lateinit var name: String
    lateinit var profileImage: String

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

        fUser = FirebaseAuth.getInstance().currentUser!!

        val messageRepository = MessageRepository(FirebaseDatabase.getInstance())
        messageViewModel = ViewModelProvider(this,
            MessageViewModel.MessageViewModelFactory(messageRepository))[MessageViewModel::class.java]

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
            popularRepository = ReviewRepository(),
            onReportClicked = { review ->
               showReportDialog(review)
            },
            likeStatusListener = this
        )
        binding.recyclerView.isNestedScrollingEnabled = false
        binding.recyclerView.adapter = adapter
    }

    private fun sendLikeNotification(reviewId: String, isLiked: Boolean) {

    }

    private fun showReportDialog(review: ReviewModel) {
        val reportDialog = ReportBottomSheet()
        val bundle = Bundle().apply {
            putString("type", "testimonial")
            putString("title", "Report Testimonial")
            putString("id", review.posterId)
            putString("subTitle", "Let us know what's wrong with this testimonial. We take every report seriously")
            putStringArrayList("spinnerList", arrayListOf(
                "Select report category",
                "False or Misleading Testimonial",
                "Inappropriate Language",
                "Hate Speech or Discrimination",
                "Spam or Promotional Content",
                "Harassment or Bullying",
                "Privacy Violation",
                "Plagiarized or Stolen Content",
                "Defamatory or Harmful Statement",
                "Irrelevant or Off-Topic Content",
                "Violation of Community Guidelines"
            ))
            putString("productId", review.reviewId)
        }
        reportDialog.arguments = bundle
        reportDialog.show(requireActivity().supportFragmentManager, "ReportDialog")
    }

    private fun loadReportFragment(review: ReviewModel, title: String, spinnerList: List<String>, fragment: Fragment){
        val bundle = Bundle()

        bundle.putString("type", "testimonial")
        bundle.putString("title", title)
        bundle.putString("id", review.posterId)
        bundle.putString("subTitle", "Let us know what's wrong with this testimonial. We take every report seriously")
        bundle.putStringArrayList("spinnerList", spinnerList.toCollection(ArrayList()))
        bundle.putString("testimonyId", review.reviewId)


        fragment.arguments = bundle

        requireParentFragment().childFragmentManager.beginTransaction()
            .replace(R.id.popular_testimony_container, fragment)
            .addToBackStack(null)
            .commit()


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
        intent.putExtra("posterId", review.posterId)
        requireActivity().startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onLikeStatusChanged(reviewId: String, isLiked: Boolean, posterId: String) {

        if (fUser.uid == posterId) {
            return
        }

        userViewModel.getUserProfile(fUser.uid){
            if (it != null){
                name = it.fullName
                profileImage = it.profileImage
            }

        }
        if (isLiked) { // ✅ If Liked, Trigger Notification
            userViewModel.getUserProfile(posterId) { user ->
                user?.let {
                    messageViewModel.triggerNotification(
                        user.fcmToken,
                        mapOf(
                            "title" to "",
                            "body" to "$name liked your testimony",
                            "reviewId" to reviewId,
                            "type" to "testimonyLike",
                            "imageUrl" to profileImage
                        )
                    )
                }
            }
        }
    }
}