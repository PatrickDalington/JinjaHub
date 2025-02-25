package com.cwp.jinja_hub.adapters

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.DiffUtil
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
    private val messages: MutableList<Message>, // Changed to mutable list for better handling
    private var imageUrl: String,
    private val context: MessageActivity
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val LEFT_VIEW_TYPE = 1
        const val RIGHT_VIEW_TYPE = 2
    }

    fun updateReceiverProfileImage(profileImage: String) {
        imageUrl = profileImage
        notifyDataSetChanged() // Notify adapter to update the profile image in left messages
    }


    // ViewHolder for left-aligned message
    inner class LeftMessageViewHolder(private val binding: MessageItemLeftBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(message: Message) {
            // Load profile image of the receiver (left user)
            binding.profileImage.load(imageUrl)

            // Bind message content (text or image)
            bindMessageContent(
                message = message,
                textView = binding.textMessage,
                imageView = binding.leftImageChat,
                mediaUrl = message.mediaUrl,
                cardImage = binding.cardImageL
            )

            // Hide textSeen for left side (Receiver)
            binding.textSeen.visibility = View.GONE
        }
    }

    // ViewHolder for right-aligned message
    inner class RightMessageViewHolder(private val binding: MessageItemRightBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(message: Message) {


            // Bind message content (text or image)
            bindMessageContent(
                message = message,
                textView = binding.textMessage,
                imageView = binding.rightImageChat,
                mediaUrl = message.mediaUrl,
                cardImage = binding.imageCard
            )

            // Handle seen/sent status
            updateSeenStatus(adapterPosition, message, binding.textSeen)
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

        imageView.setOnClickListener{
           // Open single image viewer fragment
            val fragment = SingleImageViewer()
            val bundle = Bundle()
            bundle.putString("image_url", mediaUrl)
            fragment.arguments = bundle
            context.supportFragmentManager.beginTransaction()
                .replace(R.id.message_container, fragment)
                .addToBackStack(null).commit()
        }
    }

    private fun updateSeenStatus(adapterPosition: Int, message: Message, textSeen: TextView) {
        if (message.senderId == FirebaseAuth.getInstance().currentUser?.uid && adapterPosition == messages.size - 1) {
            textSeen.text = if (message.isSeen) "Seen" else "Sent"
            textSeen.visibility = View.VISIBLE

            val params = textSeen.layoutParams as ConstraintLayout.LayoutParams
            // Adjust margins for images
            if (message.mediaUrl.isNotEmpty()) {
                params.topToBottom = R.id.image_card // Constrain to the image
            } else {
                params.topToBottom = R.id.text_message // Constrain to the text
            }
                textSeen.layoutParams = params
            } else {

            textSeen.visibility = View.GONE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)

        return if (viewType == LEFT_VIEW_TYPE) {
            // Inflate and return ViewHolder for left message layout
            val binding = MessageItemLeftBinding.inflate(layoutInflater, parent, false)
            LeftMessageViewHolder(binding)
        } else {
            // Inflate and return ViewHolder for right message layout
            val binding = MessageItemRightBinding.inflate(layoutInflater, parent, false)
            RightMessageViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]

        if (holder is LeftMessageViewHolder) {
            holder.bind(message)
        } else if (holder is RightMessageViewHolder) {
            holder.bind(message)
        }
    }

    override fun getItemCount(): Int {
        return messages.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].senderId == FirebaseAuth.getInstance().currentUser?.uid) {
            RIGHT_VIEW_TYPE
        } else {
            LEFT_VIEW_TYPE
        }
    }

    fun submitList(newMessages: List<Message>?) {
        if (newMessages != null) {
            val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
                override fun getOldListSize(): Int = messages.size
                override fun getNewListSize(): Int = newMessages.size

                override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                    return messages[oldItemPosition].messageId == newMessages[newItemPosition].messageId
                }

                override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                    return messages[oldItemPosition] == newMessages[newItemPosition]
                }
            })

            messages.clear()
            messages.addAll(newMessages)
            diffResult.dispatchUpdatesTo(this)
        }
    }
}
