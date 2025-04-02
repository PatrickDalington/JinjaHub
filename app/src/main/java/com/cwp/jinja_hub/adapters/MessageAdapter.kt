package com.cwp.jinja_hub.adapters

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.cwp.jinja_hub.R
import com.cwp.jinja_hub.databinding.MessageItemLeftBinding
import com.cwp.jinja_hub.databinding.MessageItemRightBinding
import com.cwp.jinja_hub.model.Message
import com.cwp.jinja_hub.ui.message.MessageActivity
import com.cwp.jinja_hub.ui.multi_image_viewer.ViewAllImagesActivity
import com.cwp.jinja_hub.ui.single_image_viewer.SingleImageViewer
import com.google.firebase.auth.FirebaseAuth

class MessageAdapter(
    var imageUrl: String,
    val context: MessageActivity,
    private val onLongClickMessage: (Message, position: Int, which: String) -> Unit,
) : ListAdapter<Message, RecyclerView.ViewHolder>(MessageDiffCallback()) {

    companion object {
        const val LEFT_VIEW_TYPE = 1
        const val RIGHT_VIEW_TYPE = 2
    }

    fun updateReceiverProfileImage(profileImage: String) {
        imageUrl = profileImage
        notifyDataSetChanged() // Refresh profile image on left messages
    }

    // ViewHolder for left-aligned messages (Receiver)
    inner class LeftMessageViewHolder(private val binding: MessageItemLeftBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(message: Message) {
            binding.profileImage.load(imageUrl)

            bindMessageContent(
                message = message,
                textView = binding.textMessage,
                imageView = binding.leftImageChat,
                mediaUrls = message.mediaUrl, // Use mediaUrls
                cardImage = binding.cardImageL
            )

            binding.root.setOnLongClickListener {
                onLongClickMessage(message, adapterPosition, "left")
                true
            }

            binding.textSeen.visibility = View.GONE
        }
    }

    // ViewHolder for right-aligned messages (Sender)
    inner class RightMessageViewHolder(private val binding: MessageItemRightBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(message: Message) {
            bindMessageContent(
                message = message,
                textView = binding.textMessage,
                imageView = binding.rightImageChat,
                mediaUrls = message.mediaUrl, // Use mediaUrls
                cardImage = binding.imageCard
            )

            binding.root.setOnLongClickListener {
                onLongClickMessage(message, adapterPosition, "right")
                true
            }

            updateSeenStatus(position = adapterPosition, message, binding.textSeen)
        }
    }

    private fun bindMessageContent(
        message: Message,
        textView: TextView,
        imageView: ImageView,
        mediaUrls: List<String>, // Change to List<String>
        cardImage: CardView
    ) {
        if (mediaUrls.isNotEmpty()) {
            textView.visibility = View.GONE
            cardImage.visibility = View.VISIBLE

            if (mediaUrls.size == 1) {
                // Single image, display as before
                imageView.visibility = View.VISIBLE
                val imageUrl = mediaUrls[0]
                imageView.load(imageUrl)
                imageView.setOnClickListener {
                    val fragment = SingleImageViewer()
                    val bundle = Bundle().apply {
                        putString("image_url", imageUrl)
                    }
                    fragment.arguments = bundle
                    context.supportFragmentManager.beginTransaction()
                        .replace(R.id.message_container, fragment)
                        .addToBackStack(null).commit()
                }
            } else {
                // Multiple images, display in a grid
                imageView.visibility = View.GONE // Hide single image view
                cardImage.removeAllViews() // Clear existing views

                val gridLayout = GridLayoutManager(context, 2) // 2 columns
                val recyclerView = RecyclerView(context)
                recyclerView.layoutManager = gridLayout
                val gridAdapter = ImageGridAdapter(mediaUrls, context, mediaUrls)
                recyclerView.adapter = gridAdapter

                cardImage.addView(recyclerView)
            }
        } else {
            textView.visibility = View.VISIBLE
            cardImage.visibility = View.GONE
            textView.text = message.message
        }
    }

    fun updateSeenStatus(position: Int, message: Message, textSeen: TextView) {
        val isLastMessage = position == currentList.size - 1 // Check if it's the last message

        if (message.senderId == FirebaseAuth.getInstance().currentUser?.uid) {
            if (isLastMessage) {
                textSeen.text = if (message.isSeen) "Seen" else "Sent"
                textSeen.visibility = View.VISIBLE
            } else {
                textSeen.visibility = View.GONE
            }
        } else {
            textSeen.visibility = View.GONE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return if (viewType == LEFT_VIEW_TYPE) {
            val binding = MessageItemLeftBinding.inflate(layoutInflater, parent, false)
            LeftMessageViewHolder(binding)
        } else {
            val binding = MessageItemRightBinding.inflate(layoutInflater, parent, false)
            RightMessageViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = getItem(position)
        if (holder is LeftMessageViewHolder) holder.bind(message)
        else if (holder is RightMessageViewHolder) holder.bind(message)
    }

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position).senderId == FirebaseAuth.getInstance().currentUser?.uid) {
            RIGHT_VIEW_TYPE
        } else {
            LEFT_VIEW_TYPE
        }
    }
}

// **DiffUtil Callback for Optimized List Updates**
class MessageDiffCallback : DiffUtil.ItemCallback<Message>() {
    override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean {
        return oldItem.messageId == newItem.messageId
    }

    override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
        return oldItem == newItem
    }
}

class ImageGridAdapter(private val imageUrls: List<String>, private val context: Context, private val mediaUrls: List<String>) :
    RecyclerView.Adapter<ImageGridAdapter.ImageViewHolder>() {

    class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.gridImageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.grid_image_item, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.imageView.load(imageUrls[position])

        val layoutParams = holder.imageView.layoutParams

        when (imageUrls.size) {
            2 -> {
                layoutParams.width = 400 // Set width to 200 pixels
                layoutParams.height = 550 // Set height to 200 pixels
                holder.imageView.layoutParams = layoutParams
                holder.imageView.layoutParams = layoutParams
            }
            3 -> {
                if (position == 2) {
                    layoutParams.width = 600
                    layoutParams.height = 275 // Set height to 200 pixels
                    holder.imageView.layoutParams = layoutParams
                }
            }
            // Add more cases as needed for other image counts
        }

        holder.imageView.layoutParams = layoutParams
        holder.imageView.setOnClickListener {
            Intent(context, ViewAllImagesActivity::class.java).also {
                it.putStringArrayListExtra("extra_image_urls", ArrayList(mediaUrls))
                it.putExtra("clicked_position", position)
                context.startActivity(it)
            }
        }
    }

    override fun getItemCount(): Int = imageUrls.size
}