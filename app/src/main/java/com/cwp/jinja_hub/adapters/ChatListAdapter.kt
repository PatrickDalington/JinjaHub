package com.cwp.jinja_hub.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.cwp.jinja_hub.R
import com.cwp.jinja_hub.model.ChatItem
import com.cwp.jinja_hub.model.NormalUser
import com.cwp.jinja_hub.repository.ChatRepository
import com.cwp.jinja_hub.repository.MessageRepository
import com.google.firebase.auth.FirebaseAuth
import java.text.DateFormat

class ChatListAdapter(
    private var chats: List<NormalUser>,
    private val onChatClicked: (NormalUser) -> Unit,
    private var chatRepository: ChatRepository,
    private var messageRepository: MessageRepository,
    private var onLongClickChat: (NormalUser, position: Int) -> Unit
) : RecyclerView.Adapter<ChatListAdapter.ChatViewHolder>() {

    inner class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profileImage: ImageView = itemView.findViewById(R.id.profileImage)
        val userName: TextView = itemView.findViewById(R.id.chatUserName)
        val unreadCount: TextView = itemView.findViewById(R.id.chatUnreadCount)
        val lastMessage: TextView = itemView.findViewById(R.id.chatLastMessage)
        val time: TextView = itemView.findViewById(R.id.chatTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.chat_item, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val chat = chats[position]

        holder.userName.text = chat.fullName
        holder.profileImage.load(chat.profileImage)

        // Load Last Message & Timestamp
        messageRepository.getChats(chat.userId) { messages ->
            messages.maxByOrNull { it.timestamp }?.let { latestMessage ->
                holder.time.text = DateFormat.getTimeInstance(DateFormat.SHORT).format(latestMessage.timestamp)
                holder.lastMessage.text = latestMessage.message
            }
        }

        // Unread Messages Count
        chatRepository.getUnreadCount(chat.userId) { count ->
            holder.unreadCount.text = count.toString()
            holder.unreadCount.visibility = if (count > 0) View.VISIBLE else View.GONE
        }

        holder.itemView.setOnClickListener {
            onChatClicked(chat)
        }

        holder.itemView.setOnLongClickListener{
            onLongClickChat(chat, position)
            true
        }

    }

    override fun getItemCount(): Int = chats.size

    fun updateChatList(newChatList: List<NormalUser>) {
        val uniqueChats = newChatList.distinctBy { it.userId }
        val diffResult = DiffUtil.calculateDiff(ChatDiffCallback(chats, uniqueChats))
        chats = uniqueChats
        diffResult.dispatchUpdatesTo(this)
    }

    // ChatDiffCallback class added to compare old and new chat lists efficiently
    class ChatDiffCallback(
        private val oldList: List<NormalUser>,
        private val newList: List<NormalUser>
    ) : DiffUtil.Callback() {
        override fun getOldListSize() = oldList.size
        override fun getNewListSize() = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].userId == newList[newItemPosition].userId
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }
}
