package com.cwp.jinja_hub.adapters

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.cwp.jinja_hub.R
import com.cwp.jinja_hub.databinding.MessageItemLeftBinding
import com.cwp.jinja_hub.databinding.MessageItemRightBinding
import com.cwp.jinja_hub.model.Message
import com.cwp.jinja_hub.ui.message.MessageActivity
import com.cwp.jinja_hub.ui.single_image_viewer.SingleImageViewer
import com.google.firebase.auth.FirebaseAuth

class MessageAdapter(
    private var imageUrl: String,
    private val context: MessageActivity
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
                mediaUrl = message.mediaUrl,
                cardImage = binding.cardImageL
            )

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
                mediaUrl = message.mediaUrl,
                cardImage = binding.imageCard
            )

            updateSeenStatus(position = adapterPosition,message, binding.textSeen)
        }
    }

    private fun bindMessageContent(
        message: Message,
        textView: TextView,
        imageView: ImageView,
        mediaUrl: String,
        cardImage: CardView
    ) {
        if (mediaUrl.isNotEmpty()) {
            textView.visibility = View.GONE
            cardImage.visibility = View.VISIBLE
            imageView.load(mediaUrl)
        } else {
            textView.visibility = View.VISIBLE
            cardImage.visibility = View.GONE
            textView.text = message.message
        }

        imageView.setOnClickListener {
            val fragment = SingleImageViewer()
            val bundle = Bundle().apply {
                putString("image_url", mediaUrl)
            }
            fragment.arguments = bundle
            context.supportFragmentManager.beginTransaction()
                .replace(R.id.message_container, fragment)
                .addToBackStack(null).commit()
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
