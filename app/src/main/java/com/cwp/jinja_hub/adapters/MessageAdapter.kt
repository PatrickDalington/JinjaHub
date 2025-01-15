package com.cwp.jinja_hub.adapters

import android.icu.text.ListFormatter.Width
import android.text.format.DateFormat
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.cwp.jinja_hub.R
import com.cwp.jinja_hub.databinding.ChatItemBinding
import com.cwp.jinja_hub.databinding.ItemMessageBinding
import com.cwp.jinja_hub.model.Message

class MessageAdapter(private val currentUserId: String) : ListAdapter<Message, MessageAdapter.MessageViewHolder>(DIFF_CALLBACK) {


    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Message>() {
            override fun areItemsTheSame(oldItem: Message, newItem: Message) = oldItem.messageId == newItem.messageId
            override fun areContentsTheSame(oldItem: Message, newItem: Message) = oldItem == newItem
        }
    }

    inner class MessageViewHolder(private val binding: ItemMessageBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(message: Message) {
            binding.text.text = message.text
            binding.timestampText.text = DateFormat.format("hh:mm a", message.timestamp)

            val isSentByCurrentUser = message.senderId == currentUserId

            // Adjust gravity for the message container
            val params = binding.messageContainer.layoutParams as LinearLayout.LayoutParams
            params.gravity = if (isSentByCurrentUser) Gravity.END else Gravity.START
            //params.width = if (isSentByCurrentUser) ViewGroup.LayoutParams.MATCH_PARENT else 800
            binding.messageContainer.layoutParams = params



            // Set background drawable
            binding.messageContainer.background = ContextCompat.getDrawable(
                binding.root.context,
                if (isSentByCurrentUser) R.drawable.bubble_sent else R.drawable.bubble_received
            )



            // Set text color
            binding.text.setTextColor(
                ContextCompat.getColor(
                    binding.root.context,
                    if (isSentByCurrentUser) R.color.white else R.color.black
                )
            )
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val binding = ItemMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MessageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}