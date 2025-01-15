package com.cwp.jinja_hub.ui.chat

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.cwp.jinja_hub.adapters.ChatListAdapter
import com.cwp.jinja_hub.databinding.FragmentChatBinding
import com.cwp.jinja_hub.model.ChatItem
import com.cwp.jinja_hub.repository.ChatRepository
import com.cwp.jinja_hub.ui.message.MessageActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class ChatsFragment : Fragment() {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: ChatListAdapter
    private lateinit var chatRepository: ChatRepository

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        chatRepository = ChatRepository(FirebaseDatabase.getInstance())
        setupRecyclerView()

        // Load dummy or real chat items
        loadChatItems()
    }

    private fun setupRecyclerView() {
        binding.chatsRecyclerView.layoutManager = LinearLayoutManager(requireActivity())
        adapter = ChatListAdapter(listOf()) { chat ->
            // Open MessageActivity with chat details
            val intent = Intent(requireActivity(), MessageActivity::class.java)
            intent.putExtra("chatId", chat.chatId)
            intent.putExtra("userId", FirebaseAuth.getInstance().currentUser?.uid ?: chat.userId)
            startActivity(intent)
        }
        binding.chatsRecyclerView.adapter = adapter
    }

    private fun loadChatItems() {
        val dummyChats = chatRepository.getDummyChats() // Simulated dummy data
        adapter.updateChatList(dummyChats)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
