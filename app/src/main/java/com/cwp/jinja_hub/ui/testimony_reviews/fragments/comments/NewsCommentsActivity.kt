package com.cwp.jinja_hub.com.cwp.jinja_hub.ui.testimony_reviews.fragments.comments

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import com.cwp.jinja_hub.R
import com.cwp.jinja_hub.adapters.LatestCommentsAdapter
import com.cwp.jinja_hub.com.cwp.jinja_hub.repository.NewsCommentRepository
import com.cwp.jinja_hub.com.cwp.jinja_hub.ui.message.MessageViewModel
import com.cwp.jinja_hub.com.cwp.jinja_hub.ui.testimony_reviews.NewsViewModel
import com.cwp.jinja_hub.databinding.NewsCommentActivityBinding
import com.cwp.jinja_hub.model.LatestCommentModel
import com.cwp.jinja_hub.repository.MessageRepository
import com.cwp.jinja_hub.ui.multi_image_viewer.ViewAllImagesActivity
import com.cwp.jinja_hub.ui.multi_image_viewer.ViewAllImagesActivity.Companion.EXTRA_IMAGE_URLS
import com.cwp.jinja_hub.ui.professionals_registration.ProfessionalSignupViewModel
import com.cwp.jinja_hub.utils.NumberFormater
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

class NewsCommentsActivity : AppCompatActivity() {

    private lateinit var binding: NewsCommentActivityBinding
    private val repository = NewsCommentRepository()
    private val viewModel: NewsCommentViewModel by viewModels {
        NewsCommentViewModel.NewsCommentViewModelFactory(repository)
    }
    private val newsViewModel: NewsViewModel by viewModels()
    private lateinit var name: String
    private lateinit var messageViewModel: MessageViewModel
    private val userViewModel: ProfessionalSignupViewModel by viewModels()

    private lateinit var adapter: LatestCommentsAdapter
    private lateinit var newsId: String
    private lateinit var whichComment: String

    private lateinit var numOfaComments: String

    private lateinit var imageUrls: List<String>
    private lateinit var posterId: String

    private var openedFromNotification = false

    private val fUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser
    private var posterName = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = NewsCommentActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get review ID from Intent
        newsId = intent.getStringExtra("News_ID") ?: ""
        //val posterId = intent.getStringExtra("id")
        //val imageUrls = intent.getStringArrayListExtra(EXTRA_IMAGE_URLS)

        val messageRepository = MessageRepository(FirebaseDatabase.getInstance())
        messageViewModel = ViewModelProvider(this,
            MessageViewModel.MessageViewModelFactory(messageRepository))[MessageViewModel::class.java]



        // ✅ Check if launched from notification
        openedFromNotification = intent.getBooleanExtra("FROM_NEWS_NOTIFICATION", false)


        setupRecyclerView()

        // Observe LiveData from ViewModel
        viewModel.comments.observe(this) { comments ->

            updateRecyclerView(comments, newsId)
        }



        // Fetch comments for the review
        viewModel.fetchComments(newsId)

        // Get all views from the layout
        val userImage = binding.profileImage
        val title = binding.title
        val link = binding.link
        val testimony = binding.content
        val homeImage = binding.homeImage
        val seeImages = binding.seeImages
        val commentCount = binding.commentsCount
        val likes = binding.likes
        val shares = binding.shares


        viewModel.fetchSpecificClickedNews(newsId) { fullName, username, profileImage, news ->
            if (news != null) {
                imageUrls = news.mediaUrl!!
                posterId = news.posterId
                // Set the views with the fetched data
                title.text = news.header
                userImage.load(profileImage)
                link.text = news.vidLink
                if (news.mediaUrl.isNullOrEmpty()) {
                    homeImage.load(R.drawable.no_img)
                    seeImages.visibility = View.GONE
                } else if (news.mediaUrl?.size!! == 1) {
                    homeImage.load(news.mediaUrl!!.first())
                } else {
                    homeImage.load(news.mediaUrl!!.first())
                    seeImages.visibility = View.VISIBLE
                    seeImages.text = "View ${news.mediaUrl?.size.toString()} more images"
                }
                testimony.text = news.content
            }
        }

        // Fetch number of comments
        newsViewModel.fetchNumberOfReviewComments(newsId) { numberOfComments ->
            numOfaComments = numberOfComments.toString()
            commentCount.text = numOfaComments
        }

        // Fetch number of likes
       newsViewModel.fetchNumberOfLikes(newsId) { numberOfLikes ->
            likes.text = NumberFormater().formatNumber(numberOfLikes.toString())
        }


        newsViewModel.checkIfUserLikedNews(newsId) { isLiked ->
            // Update the heart icon based on the user's like status
            binding.heart.setImageResource(
                if (isLiked) R.drawable.spec_heart_on else R.drawable.heart
            )
        }

        // Like review
        binding.heart.setOnClickListener {
            whichComment = ""
            newsViewModel.likeReview(newsId, fUser?.uid ?: "") { liked ->
                if (liked == "like") {
                    binding.heart.setImageResource(R.drawable.spec_heart_on)

                    showNotification(
                        whichComment,
                        "New Like 👍🏾",
                        "$posterName liked the news",
                        fUser!!,
                        newsId,
                        "like",
                        posterId)

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
            whichComment = ""
            val commentText = binding.editTextComment.text.toString()
            if (!TextUtils.isEmpty(commentText)) {
                viewModel.addComment(newsId, commentText){
                    if (it){
                        if (posterId != fUser!!.uid){
                            whichComment = "single"
                            showNotification(
                                whichComment,
                                "New comment",
                                commentText,
                                fUser,
                                newsId,
                                "newsLike",
                                posterId)
                        }else{
                            whichComment = "multiple"
                            val commentStyle = listOf(
                                "A new comment just came in—stay updated.",
                                "A fresh perspective has been shared—join the conversation.",
                                "New discussion alert—see what’s happening now.",
                                "Your post is gaining traction—check the latest response.",
                                "Someone just joined the conversation—read their thoughts.",
                                "A new update has arrived—don’t miss out.",
                                "A thought-provoking comment was just posted.",
                                "New insights have been shared—explore the conversation.",
                                "A key update just dropped—stay informed.",
                                "Breaking discussion happening now—take a look.",
                                "A new response is waiting for you—join in.",
                                "A trending discussion is unfolding—see what’s being said.",
                                "Someone just added to the conversation—read their take.",
                                "A new comment might change your perspective.",
                                "Stay ahead—see the latest thoughts shared.",
                                "Engagement is rising—join the discussion now.",
                                "A fresh take has just been posted—check it out.",
                                "A crucial update has been shared—see what’s new.",
                                "A major conversation shift just happened—be part of it.",
                                "New voices are adding to the discussion—read now."
                            ).random()
                            // fetch all sender ids and send them notification

                            viewModel.fetchAllSenderId{ ids ->
                                CoroutineScope(Dispatchers.IO).launch {
                                    for (id in ids) {
                                        withContext(Dispatchers.Main) {
                                            showNotification(
                                                whichComment,
                                                commentStyle,
                                                commentText,
                                                fUser,
                                                newsId,
                                                "newsComment",
                                                id)
                                        }
                                        delay(500) // ✅ Adds a delay of 500ms before sending the next notification
                                    }
                                }
                            }
                        }
                    }else
                        Toast.makeText(this, "Comment failed to add", Toast.LENGTH_SHORT).show()
                }
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
            newsViewModel.addNumberOfShares(newsId)
        }

        // Get number of shares
        newsViewModel.getNumberOfShares(newsId) { numberOfShares ->
            shares.text = NumberFormater().formatNumber(numberOfShares.toString())
        }

        // set toolbar back button
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            handleBackNavigation()
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish() // ✅ Just go back, no new screen
            }
        })


    }

    private fun showNotification(whichComment: String, title: String, body: String, fUser: FirebaseUser, reviewId: String, type: String, posterId: String) {
        fUser.let { it1 ->
            userViewModel.getUserProfile(it1.uid){
                if (it != null){
                    posterName = it.fullName
                }

            }
        }
        if (type == "like") {
            // ✅ If Liked, Trigger Notification
            userViewModel.getUserProfile(posterId) { user ->
                user?.let {
                    messageViewModel.triggerNotification(
                        user.fcmToken,
                        mapOf(
                            "title" to "",
                            "body" to "${user.fullName} liked your comment",
                            "reviewId" to reviewId,
                            "type" to type
                        )
                    )
                }
            }
        }
        else if (type == "comment"){
            if (whichComment == "single"){
                userViewModel.getUserProfile(posterId) { user ->
                    user?.let {
                        messageViewModel.triggerNotification(
                            user.fcmToken,
                            mapOf(
                                "title" to "$title from ${user.fullName} ",
                                "body" to body,
                                "reviewId" to reviewId,
                                "type" to type
                            )
                        )
                    }
                }
            }else{
                userViewModel.getUserProfile(posterId) { user ->
                    user?.let {
                        messageViewModel.triggerNotification(
                            user.fcmToken,
                            mapOf(
                                "title" to "${user.fullName} joined the conversation",
                                "body" to body,
                                "reviewId" to reviewId,
                                "type" to type
                            )
                        )
                    }
                }
            }

        }

    }


    // **Navigation**
    private fun handleBackNavigation() {
        onBackPressedDispatcher.onBackPressed()
    }

    private fun setupRecyclerView() {

        binding.recyclerViewComments.layoutManager = LinearLayoutManager(this)
        adapter = LatestCommentsAdapter(
             "",
            comments = emptyList(),
            onCommentClicked = { comment, pos ->
                showCommentDialog(comment, newsId, pos, fUser!!)

            },
            onDeleteCommentClicked = {i,p->}

        )
        binding.recyclerViewComments.adapter = adapter
    }

    private fun updateRecyclerView(comments: List<LatestCommentModel>, reviewId: String) {
        //observe comment so as to get the image
        adapter = LatestCommentsAdapter(
          "",
            comments,
           onCommentClicked = { comment, pos ->
                // onLongClicked -> User long click the comment
                showCommentDialog(comment, reviewId, pos, fUser!!)
            },
            onDeleteCommentClicked = {i,p->}
        )
        binding.recyclerViewComments.adapter = adapter
    }


    private fun showCommentDialog(comment: LatestCommentModel, reviewId: String, position: Int, fUser: FirebaseUser) {
        val formattedDate = SimpleDateFormat("hh:mm a, dd MMM yyyy", Locale.getDefault()).format(
            Date(comment.timestamp)
        )

        val builder = AlertDialog.Builder(this)
            .setTitle("Comment Detail")
            .setMessage("Date & Time: $formattedDate")
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }

        // If the current user is the sender, allow them to delete the comment
        if (comment.senderId == fUser.uid) {
            builder.setNegativeButton("Delete") { dialog, _ ->
                viewModel.deleteComment(comment, reviewId) { isDeleted ->
                    if (isDeleted) {
                        runOnUiThread {
                            // ✅ Remove comment from adapter before closing dialog
                            val updatedComments = adapter.comments.toMutableList()
                            updatedComments.remove(comment)
                            adapter.updateComments(updatedComments, position)  // Update RecyclerView manually

                            Toast.makeText(this, "Comment deleted", Toast.LENGTH_SHORT).show()
                            dialog.dismiss()  // ✅ Dismiss Dialog Immediately
                        }
                    } else {
                        Toast.makeText(this, "Failed to delete comment", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        builder.create().show()
    }


    private fun goToViewAllImagesActivity(imageUrls: List<String>?, posterId: String?){
        val intent = Intent(this, ViewAllImagesActivity::class.java)
        intent.putStringArrayListExtra(
            EXTRA_IMAGE_URLS,
            imageUrls?.let { ArrayList(it) })
        intent.putExtra("id", posterId)
        startActivity(intent)
    }
}
