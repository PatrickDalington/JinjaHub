package com.cwp.jinja_hub.ui.testimony_reviews.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.cwp.jinja_hub.com.cwp.jinja_hub.adapters.NewsAdapter
import com.cwp.jinja_hub.com.cwp.jinja_hub.model.NewsModel
import com.cwp.jinja_hub.com.cwp.jinja_hub.repository.NewsRepository
import com.cwp.jinja_hub.com.cwp.jinja_hub.ui.message.MessageViewModel
import com.cwp.jinja_hub.com.cwp.jinja_hub.ui.testimony_reviews.NewsViewModel
import com.cwp.jinja_hub.com.cwp.jinja_hub.ui.testimony_reviews.fragments.comments.NewsCommentsActivity
import com.cwp.jinja_hub.databinding.FragmentLatestBinding
import com.cwp.jinja_hub.helpers.SendRegularNotification
import com.cwp.jinja_hub.model.NotificationModel
import com.cwp.jinja_hub.repository.MessageRepository
import com.cwp.jinja_hub.ui.message.MessageActivity
import com.cwp.jinja_hub.ui.professionals_registration.ProfessionalSignupViewModel
import com.cwp.jinja_hub.ui.testimony_reviews.fragments.comments.LatestCommentsActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase


class LatestFragment : Fragment() {

    private var _binding: FragmentLatestBinding? = null
    private val binding get() = _binding!!

    private val newsViewModel: NewsViewModel by viewModels()
    private lateinit var messageViewModel: MessageViewModel
    private val userViewModel: ProfessionalSignupViewModel by viewModels()

    lateinit var fUser: FirebaseUser
    lateinit var name: String

    private var newsId: String? = null

    private lateinit var adapter: NewsAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLatestBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fUser = FirebaseAuth.getInstance().currentUser!!

        val messageRepository = MessageRepository(FirebaseDatabase.getInstance())
        messageViewModel = ViewModelProvider(this,
            MessageViewModel.MessageViewModelFactory(messageRepository))[MessageViewModel::class.java]

        setupRecyclerView()

        // Observe LiveData only once
        newsViewModel.popularNews.observe(viewLifecycleOwner) { news ->
            updateRecyclerView(news)
            //binding.swipeRefreshLayout.isRefreshing = false
        }



        // Show loader and fetch data
        binding.progressBar.visibility = View.VISIBLE
        newsViewModel.fetchPopularNews()

        binding.smartRefreshLayout.setOnRefreshListener{
            newsViewModel.fetchPopularNews() // Just refresh data, don't reattach observer
        }

    }

    private fun setupRecyclerView() {
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = NewsAdapter(
            news = mutableListOf(),
            onCommentClickListener = { news -> handleCommentClick(news) },
            onDescriptionClickListener = { news -> handleDescriptionClicked(news) },
            onNameClickListener = { news -> gotoMessageActivity(news) },
            newsRepository = NewsRepository(),
            messageViewModel = MessageViewModel(
                MessageRepository(
                    FirebaseDatabase.getInstance()
                )
            ),
            likeStatusListener = this
        )
        binding.recyclerView.isNestedScrollingEnabled = false
        binding.recyclerView.adapter = adapter
    }

    private fun sendLikeNotification(reviewId: String, isLiked: Boolean) {

    }

    private fun updateRecyclerView(reviews: MutableList<NewsModel>) {
        adapter.refreshReviews(reviews) // Update existing adapter, don't recreate it
        binding.progressBar.visibility = View.GONE
        binding.recyclerView.visibility = View.VISIBLE
    }

    private fun handleCommentClick(review: NewsModel) {
        goToLatestCommentsActivity(review)
    }

    private fun handleDescriptionClicked(review: NewsModel) {
        goToLatestCommentsActivity(review)
    }

    private fun gotoMessageActivity(review: NewsModel) {
        handleProfileClick(review)
    }

    private fun handleProfileClick(review: NewsModel) {
        if (review.posterId != FirebaseAuth.getInstance().currentUser?.uid) {
            Intent(requireActivity(), MessageActivity::class.java).apply {
                putExtra("receiverId", review.posterId)
                requireActivity().startActivity(this)
            }
        } else {
            Toast.makeText(requireActivity(), "You cannot message yourself", Toast.LENGTH_SHORT).show()
        }
    }

    private fun goToLatestCommentsActivity(news: NewsModel) {
        val intent = Intent(requireActivity(), NewsCommentsActivity::class.java)
        intent.putExtra("News_ID", news.newsId)
        requireActivity().startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun onLikeStatusChanged(newsId: String, isLiked: Boolean, posterId: String) {

        userViewModel.getUserProfile(fUser.uid){
            if (it != null){
                name = it.fullName
            }

        }
        if (isLiked) { // ✅ If Liked, Trigger Notification
            userViewModel.getUserProfile(posterId) { user ->
                user?.let {
                    user.fcmToken
                    mapOf(
                        "title" to "You have a potential buyer",
                        "body" to "${user.fullName} liked a news",
                        "newsId" to newsId,
                        "type" to "news"
                    )
                }
            }
        }
    }

    companion object {

        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            LatestFragment().apply {

            }
    }
}