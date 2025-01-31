package com.cwp.jinja_hub.ui.chat

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cwp.jinja_hub.R
import com.cwp.jinja_hub.adapters.ChatListAdapter
import com.cwp.jinja_hub.model.User
import com.cwp.jinja_hub.repository.ChatRepository
import com.cwp.jinja_hub.ui.message.MessageActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase

class ChatsFragment : Fragment() {

    private lateinit var chatViewModel: ChatViewModel
    private lateinit var chatAdapter: ChatListAdapter
    private lateinit var fUser: FirebaseUser

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_chat, container, false)

        fUser = FirebaseAuth.getInstance().currentUser!!

        val chatRepository = ChatRepository(FirebaseDatabase.getInstance())
        chatViewModel = ViewModelProvider(
            this,
            ChatViewModel.ChatViewModelFactory(chatRepository, fUser)
        )[ChatViewModel::class.java]

        val recyclerView: RecyclerView = view.findViewById(R.id.chatsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())


        chatAdapter = ChatListAdapter(
            chats = emptyList(),
            onChatClicked = { chatItem ->
                // Handle chat click

                // Navigate to MessageActivity
                val intent = Intent(requireContext(), MessageActivity::class.java)
                intent.putExtra("receiverId", chatItem.userId)
                startActivity(intent)
            },
            chatRepository = chatRepository
        )
        recyclerView.adapter = chatAdapter

        observeViewModel()

        return view
    }

    private fun observeViewModel() {
        chatViewModel.chats.observe(viewLifecycleOwner) { chatList ->
            if (chatList != null) {
                chatAdapter.updateChatList(chatList)
            }
        }

        chatViewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
        }




    }
}
