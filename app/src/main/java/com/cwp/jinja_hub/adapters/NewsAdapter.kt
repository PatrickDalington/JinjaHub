package com.cwp.jinja_hub.com.cwp.jinja_hub.adapters

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.cwp.jinja_hub.R
import com.cwp.jinja_hub.com.cwp.jinja_hub.model.NewsModel
import com.cwp.jinja_hub.com.cwp.jinja_hub.repository.NewsRepository
import com.cwp.jinja_hub.com.cwp.jinja_hub.ui.message.MessageViewModel
import com.cwp.jinja_hub.ui.testimony_reviews.fragments.LatestFragment
import com.cwp.jinja_hub.utils.NumberFormater
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale

class NewsAdapter(
    private var news: MutableList<NewsModel>,
    private val onCommentClickListener: (NewsModel) -> Unit,
    private val onDescriptionClickListener: (NewsModel) -> Unit,
    private val onNameClickListener: (NewsModel) -> Unit,
    private val newsRepository: NewsRepository,
    private val messageViewModel: MessageViewModel,
    private val likeStatusListener: LatestFragment
) : RecyclerView.Adapter<NewsAdapter.ReviewViewHolder>() {

    class ReviewViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val header: TextView = view.findViewById(R.id.header)
        val content: TextView = view.findViewById(R.id.content)
        val time: TextView = view.findViewById(R.id.time)
        val homeImage: ImageView = view.findViewById(R.id.home_image)
        val likeHeart: ImageView = view.findViewById(R.id.heart_like)
        val likeCount: TextView = view.findViewById(R.id.likes)
        val commentIcon: LinearLayout = view.findViewById(R.id.comments)
        val commentCount: TextView = view.findViewById(R.id.comment_count)
        val share: LinearLayout = view.findViewById(R.id.share)
        val numOfShares: TextView = view.findViewById(R.id.num_of_shares)
        val heartLayout: LinearLayout = view.findViewById(R.id.heart_layout)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.news_item, parent, false)
        return ReviewViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        val new = news[position]
        val fUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser



        // Bind user-specific data
        holder.header.text = new.header
        holder.time.text = formatTimestamp(new.timestamp)
        holder.content.text = new.content



        // Load number of comments
        new.newsId.let {
            newsRepository.fetchNumberOfNewsComments(it) { numberOfComments ->
                holder.commentCount.text = NumberFormater().formatNumber(numberOfComments.toString())
            }
        }

        // Load review image (if any)
        if (!new.mediaUrl.isNullOrEmpty()) {
            holder.homeImage.load(new.mediaUrl!!.first())
        } else {
            holder.homeImage.setImageResource(R.drawable.no_image)
        }

        // Set initial number of comments
        new.newsId.let {
            newsRepository.fetchNumberOfLikes(it) { numberOfLikes ->
                holder.likeCount.text = NumberFormater().formatNumber(numberOfLikes.toString())
            }
        }

        // Handle likes
        // Check if the current user has liked the review
        newsRepository.checkIfUserLikedNews(new.newsId) { isLiked ->
            // Update the heart icon based on the user's like status
            holder.likeHeart.setImageResource(
                if (isLiked) R.drawable.spec_heart_on else R.drawable.heart
            )
        }


        // Get number of shares
        new.newsId.let {
            newsRepository.getNumberOfShares(it) { numberOfShares ->
                holder.numOfShares.text = NumberFormater().formatNumber(numberOfShares.toString())
            }
        }


        // Like/Unlike functionality
        holder.heartLayout.setOnClickListener {
            if (fUser == null) {
                Toast.makeText(holder.itemView.context, "Unable to like review. Please log in.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            newsRepository.likeNews(new.newsId, fUser.uid) { isLiked ->
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        withContext(Dispatchers.Main) {
                            if (isLiked == "like") {
                                holder.likeHeart.setImageResource(R.drawable.spec_heart_on)
                                likeStatusListener.onLikeStatusChanged(new.newsId, true, new.posterId)
                            } else {
                                holder.likeHeart.setImageResource(R.drawable.heart)
                                likeStatusListener.onLikeStatusChanged(new.newsId, false, new.posterId)
                            }

                            // ✅ Update like count efficiently
                            newsRepository.fetchNumberOfLikes(new.newsId) { numberOfLikes ->
                                holder.likeCount.text = NumberFormater().formatNumber(numberOfLikes.toString())
                            }
                        }
                    } catch (error: Exception) {
                        Log.e("LikeNotification", "Failed to fetch user profile: ${error.message}")
                    }
                }
            }
        }

        holder.share.setOnClickListener {
            // Add number of shares to firebase database
            new.newsId.let { newsId ->
                newsRepository.createDynamicLink(newsId) { dynamicLink ->
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, dynamicLink)
                    }
                    holder.itemView.context.startActivity(Intent.createChooser(intent, "Share Link"))
                    newsRepository.addNumberOfShares(newsId)
                }
            }
        }

        // Click listeners
        holder.commentIcon.setOnClickListener { onCommentClickListener(new) }
        //holder.description.setOnClickListener { onDescriptionClickListener(review) }
        ///holder.homeImage.setOnClickListener { onHomeImageClickListener(review) }
        holder.header.setOnClickListener{onNameClickListener(new)}


        holder.itemView.setOnClickListener{
            onDescriptionClickListener(new)
        }

    }

    override fun getItemCount(): Int = news.size

    /**
     * Formats a timestamp into a user-friendly string.
     */
    private fun formatTimestamp(timestamp: Long?): String {
        if (timestamp == null) return "N/A"
        val formatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return formatter.format(timestamp)
    }

    /**
     * Refresh the list of reviews and notify the adapter.
     */
    fun refreshReviews(newNews: List<NewsModel>) {
        val diffCallback = ReviewDiffCallback(news, newNews)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        news.clear()
        news.addAll(newNews)
        diffResult.dispatchUpdatesTo(this)
    }

    class ReviewDiffCallback(
        private val oldList: List<NewsModel>,
        private val newList: List<NewsModel>
    ) : DiffUtil.Callback() {

        override fun getOldListSize() = oldList.size
        override fun getNewListSize() = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
            oldList[oldItemPosition].newsId == newList[newItemPosition].newsId

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
            oldList[oldItemPosition] == newList[newItemPosition]
    }

}
