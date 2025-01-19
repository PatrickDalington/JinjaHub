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
import com.cwp.jinja_hub.model.User
import com.cwp.jinja_hub.repository.ChatRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class ChatListAdapter(
    private var chats: List<User>,
    private val onChatClicked: (User) -> Unit,
    private var chatRepository: ChatRepository
) : RecyclerView.Adapter<ChatListAdapter.ChatViewHolder>() {

    inner class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profileImage: ImageView = itemView.findViewById(R.id.profileImage)
        val userName: TextView = itemView.findViewById(R.id.chatUserName)
        val unreadCount: TextView = itemView.findViewById(R.id.chatUnreadCount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.chat_item, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val chat = chats[position]

        holder.userName.text = chat.fullName

        // Load profile image
        holder.profileImage.load(chat.profileImage)

        // Getting unread count
        chatRepository.getUnreadCount(chat.userId) { count ->
            holder.unreadCount.visibility = if (count > 0) View.VISIBLE else View.GONE
            holder.unreadCount.text = count.toString()
        }




        // Handle chat click
        holder.itemView.setOnClickListener {
            onChatClicked(chat)
        }
    }

    override fun getItemCount(): Int = chats.size

    fun updateChatList(newChatList: List<User>) {
        val diffResult = DiffUtil.calculateDiff(ChatDiffCallback(chats, newChatList))
        chats = newChatList
        diffResult.dispatchUpdatesTo(this)
    }


    class ChatDiffCallback(
        private val oldList: List<User>,
        private val newList: List<User>
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
