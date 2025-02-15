package com.cwp.jinja_hub.ui.testimony_reviews.fragments.comments

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import com.cwp.jinja_hub.R
import com.cwp.jinja_hub.adapters.LatestCommentsAdapter
import com.cwp.jinja_hub.databinding.ActivityLatestFragmentCommentsBinding
import com.cwp.jinja_hub.model.LatestCommentModel
import com.cwp.jinja_hub.repository.CommentRepository
import com.cwp.jinja_hub.ui.multi_image_viewer.ViewAllImagesActivity
import com.cwp.jinja_hub.ui.multi_image_viewer.ViewAllImagesActivity.Companion.EXTRA_IMAGE_URLS
import com.cwp.jinja_hub.ui.testimony_reviews.ReviewViewModel
import com.cwp.jinja_hub.utils.NumberFormater
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import java.text.DateFormat
import kotlin.math.abs

class LatestCommentsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLatestFragmentCommentsBinding
    private val repository = CommentRepository()
    private val viewModel: LatestCommentViewModel by viewModels {
        LatestCommentViewModel.LatestCommentViewModelFactory(repository)
    }
    private val reviewViewModel: ReviewViewModel by viewModels()

    private lateinit var adapter: LatestCommentsAdapter
    private lateinit var reviewId: String

    private lateinit var numOfaComments: String

    private val fUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLatestFragmentCommentsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get review ID from Intent
        reviewId = intent.getStringExtra("REVIEW_ID") ?: ""
        val posterId = intent.getStringExtra("id")
        val imageUrls = intent.getStringArrayListExtra(EXTRA_IMAGE_URLS)




        setupRecyclerView()

        // Observe LiveData from ViewModel
        viewModel.comments.observe(this) { comments ->

            updateRecyclerView(comments)
        }



        // Fetch comments for the review
        viewModel.fetchComments(reviewId)

        // Get all views from the layout
        val userImage = binding.profileImage
        val name = binding.name
        val usernameTime = binding.usernameTime
        val testimony = binding.testimony
        val homeImage = binding.homeImage
        val seeImages = binding.seeImages
        val commentCount = binding.commentsCount
        val likes = binding.likes
        val shares = binding.shares


        viewModel.fetchSpecificClickedReviews(reviewId) { fullName, username, profileImage, review ->
            name.text = fullName
            userImage.load(profileImage)
            usernameTime.text = "@$username . ${DateFormat.getInstance().format(review.timestamp)}"
            if (review.mediaUrl.isNullOrEmpty()) {
                homeImage.load(R.drawable.no_image)
                seeImages.visibility = View.GONE
            }else if (review.mediaUrl?.size!! == 1){
                homeImage.load(review.mediaUrl!!.first())
            }else{
                homeImage.load(review.mediaUrl!!.first())
                seeImages.visibility = View.VISIBLE
                seeImages.text = "View ${review.mediaUrl?.size.toString()} more images"
            }
            testimony.text = review.description
        }

        // Fetch number of comments
        reviewViewModel.fetchNumberOfReviewComments(reviewId) { numberOfComments ->
            numOfaComments = numberOfComments.toString()
            commentCount.text = numOfaComments
        }

        // Fetch number of likes
        reviewViewModel.fetchNumberOfLikes(reviewId) { numberOfLikes ->
            likes.text = NumberFormater().formatNumber(numberOfLikes.toString())
        }


        reviewViewModel.checkIfUserLikedReview(reviewId) { isLiked ->
            // Update the heart icon based on the user's like status
            binding.heart.setImageResource(
                if (isLiked) R.drawable.spec_heart_on else R.drawable.heart
            )
        }

        // Like review
        binding.heart.setOnClickListener {
            reviewViewModel.likeReview(reviewId, fUser?.uid ?: "") { liked ->
                if (liked == "like") {
                    binding.heart.setImageResource(R.drawable.spec_heart_on)
                } else {
                    binding.heart.setImageResource(R.drawable.heart)
                }
            }
        }

        // click on see images takes you to ViewAllImagesActivity
        binding.seeImages.setOnClickListener {
           goToViewAllImagesActivity(imageUrls, posterId)
        }

        // Click the main image to go to ViewAllImagesActivity as well
        binding.homeImage.setOnClickListener{
            goToViewAllImagesActivity(imageUrls, posterId)
        }


        // Add a comment
        binding.buttonAddComment.setOnClickListener {
            val commentText = binding.editTextComment.text.toString()
            if (!TextUtils.isEmpty(commentText)) {
                viewModel.addComment(reviewId, commentText)
                binding.editTextComment.text?.clear()
            } else {
                Toast.makeText(this, "Comment cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }

        binding.collapsingToolbar.title = "Comments"

        // make the title of collapsingToolbar appear and disappear on scroll
        binding.appBar.addOnOffsetChangedListener{ appBarLayout, verticalOffset ->

            if (abs(verticalOffset) >= appBarLayout.totalScrollRange) {
                // Collapsed: Show the title
                if (numOfaComments > 1.toString())
                    binding.collapsingToolbar.title = "$numOfaComments Comments"
                else if (numOfaComments == 0.toString())
                    binding.collapsingToolbar.title = "No comment yet"
                else
                    binding.collapsingToolbar.title = "$numOfaComments Comment"
            } else {
                // Expanded: Hide the title
                binding.collapsingToolbar.title = ""
            }
        }

        // Add number of share when share icon is clicked
        binding.shares.setOnClickListener {
            reviewViewModel.addNumberOfShares(reviewId)
        }

        // Get number of shares
        reviewViewModel.getNumberOfShares(reviewId) { numberOfShares ->
            shares.text = NumberFormater().formatNumber(numberOfShares.toString())
        }

        // set toolbar back button
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

    }

    private fun setupRecyclerView() {
        binding.recyclerViewComments.layoutManager = LinearLayoutManager(this)
        adapter = LatestCommentsAdapter(comments = emptyList())
        binding.recyclerViewComments.adapter = adapter
    }

    private fun updateRecyclerView(comments: List<LatestCommentModel>) {
        //observe comment so as to get the image
        adapter = LatestCommentsAdapter(comments)
        binding.recyclerViewComments.adapter = adapter
    }

    private fun goToViewAllImagesActivity(imageUrls: List<String>?, posterId: String?){
        val intent = android.content.Intent(this, ViewAllImagesActivity::class.java)
        intent.putStringArrayListExtra(
            EXTRA_IMAGE_URLS,
            imageUrls?.let { ArrayList(it) })
        intent.putExtra("id", posterId)
        startActivity(intent)
    }
}
