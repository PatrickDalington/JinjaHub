package com.cwp.jinja_hub.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.cwp.jinja_hub.R
import com.cwp.jinja_hub.model.ChatItem

class ChatListAdapter(
    private var chats: List<ChatItem>,
    private val onChatClicked: (ChatItem) -> Unit
) : RecyclerView.Adapter<ChatListAdapter.ChatViewHolder>() {

    inner class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profileImage: ImageView = itemView.findViewById(R.id.profileImage)
        val userName: TextView = itemView.findViewById(R.id.chatUserName)
        val lastMessage: TextView = itemView.findViewById(R.id.chatLastMessage)
        val time: TextView = itemView.findViewById(R.id.chatTimestamp)
        val unreadCount: TextView = itemView.findViewById(R.id.chatUnreadCount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.chat_item, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val chat = chats[position]

        holder.userName.text = chat.userName
        holder.lastMessage.text = chat.lastMessage
        holder.time.text = chat.time

        // Show unread count if > 0
        if (chat.unreadCount > 0) {
            holder.unreadCount.visibility = View.VISIBLE
            holder.unreadCount.text = chat.unreadCount.toString()
        } else {
            holder.unreadCount.visibility = View.GONE
        }

        // Load profile image
        Glide.with(holder.profileImage.context)
            .load(chat.profileImageUrl)
            .placeholder(R.drawable.profile_image)
            .into(holder.profileImage)

        // Handle chat click
        holder.itemView.setOnClickListener {
            onChatClicked(chat)
        }
    }

    fun updateChatList(newChatList: List<ChatItem>) {
        chats = newChatList
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = chats.size
}
