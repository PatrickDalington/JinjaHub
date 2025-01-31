package com.cwp.jinja_hub.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.cwp.jinja_hub.R
import com.cwp.jinja_hub.model.ReviewModel
import com.cwp.jinja_hub.repository.ReviewRepository
import com.cwp.jinja_hub.utils.NumberFormater
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import de.hdodenhof.circleimageview.CircleImageView
import java.text.SimpleDateFormat
import java.util.Locale

class PopularReviewAdapter(
    private var reviews: MutableList<ReviewModel>,
    private val onCommentClickListener: (ReviewModel) -> Unit,
    private val onProfileClickListener: (ReviewModel) -> Unit,
    private val onDescriptionClickListener: (ReviewModel) -> Unit,
    private val onHomeImageClickListener: (ReviewModel) -> Unit,
    private val popularRepository: ReviewRepository
) : RecyclerView.Adapter<PopularReviewAdapter.ReviewViewHolder>() {

    class ReviewViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.name)
        val username: TextView = view.findViewById(R.id.username_time)
        val description: TextView = view.findViewById(R.id.testimony)
        val profileImage: CircleImageView = view.findViewById(R.id.profile_image)
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
        val view = LayoutInflater.from(parent.context).inflate(R.layout.review_item, parent, false)
        return ReviewViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        val review = reviews[position]
        val fUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser

        // Bind user-specific data
        holder.name.text = review.posterName
        holder.username.text = "@${review.posterUsername} . ${formatTimestamp(review.timestamp)}"
        holder.description.text = review.description

        // Load profile image
        holder.profileImage.load(review.posterProfileImage) {
            placeholder(R.drawable.no_image)
        }


        // Load number of comments
        review.reviewId?.let {
            popularRepository.fetchNumberOfReviewComments(it) { numberOfComments ->
                holder.commentCount.text = NumberFormater().formatNumber(numberOfComments.toString())
            }
        }

        // Load review image (if any)
        if (!review.mediaUrl.isNullOrEmpty()) {
            holder.homeImage.load(review.mediaUrl!!.first())
        } else {
            holder.homeImage.setImageResource(R.drawable.no_image)
        }

        // Set initial number of comments
        review.reviewId?.let {
            popularRepository.fetchNumberOfLikes(it) { numberOfLikes ->
                holder.likeCount.text = NumberFormater().formatNumber(numberOfLikes.toString())
            }
        }

        // Handle likes
        // Check if the current user has liked the review
        popularRepository.checkIfUserLikedReview(review.reviewId ?: "") { isLiked ->
            // Update the heart icon based on the user's like status
            holder.likeHeart.setImageResource(
                if (isLiked) R.drawable.spec_heart_on else R.drawable.heart
            )
        }


        // Get number of shares
        review.reviewId?.let {
            popularRepository.getNumberOfShares(it) { numberOfShares ->
                holder.numOfShares.text = NumberFormater().formatNumber(numberOfShares.toString())
            }
        }


        // Like/Unlike functionality
        holder.heartLayout.setOnClickListener {
            if (fUser != null && review.reviewId != null) {
                popularRepository.likeReview(review.reviewId!!, fUser.uid) { isLiked ->
                    if (isLiked == "like") {
                        holder.likeHeart.setImageResource(R.drawable.spec_heart_on)
                    } else {
                        holder.likeHeart.setImageResource(R.drawable.heart)
                    }

                    // Update like count
                    popularRepository.fetchNumberOfLikes(review.reviewId!!) { numberOfLikes ->
                        holder.likeCount.text = NumberFormater().formatNumber(numberOfLikes.toString())
                    }
                }
            } else {
                Toast.makeText(holder.itemView.context, "Unable to like review. Please log in.", Toast.LENGTH_SHORT).show()
            }
        }

        holder.share.setOnClickListener {
            // Add number of shares to firebase database
            review.reviewId?.let { reviewId ->
                popularRepository.createDynamicLink(reviewId) { dynamicLink ->
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, dynamicLink)
                    }
                    holder.itemView.context.startActivity(Intent.createChooser(intent, "Share Link"))
                    popularRepository.addNumberOfShares(reviewId)
                }
            }
        }

        // Click listeners
        holder.commentIcon.setOnClickListener { onCommentClickListener(review) }
        holder.profileImage.setOnClickListener { onProfileClickListener(review) }
        holder.description.setOnClickListener { onDescriptionClickListener(review) }
        holder.homeImage.setOnClickListener { onHomeImageClickListener(review) }

    }

    override fun getItemCount(): Int = reviews.size

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
    fun refreshReviews(newReviews: List<ReviewModel>) {
        val diffCallback = ReviewDiffCallback(reviews, newReviews)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        reviews.clear()
        reviews.addAll(newReviews)
        diffResult.dispatchUpdatesTo(this)
    }

    class ReviewDiffCallback(
        private val oldList: List<ReviewModel>,
        private val newList: List<ReviewModel>
    ) : DiffUtil.Callback() {

        override fun getOldListSize() = oldList.size
        override fun getNewListSize() = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
            oldList[oldItemPosition].reviewId == newList[newItemPosition].reviewId

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
            oldList[oldItemPosition] == newList[newItemPosition]
    }

}
