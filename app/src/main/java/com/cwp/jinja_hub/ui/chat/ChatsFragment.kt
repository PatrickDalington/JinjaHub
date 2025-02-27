package com.cwp.jinja_hub.ui.chat

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.cwp.jinja_hub.databinding.FragmentChatBinding
import com.cwp.jinja_hub.adapters.ChatListAdapter
import com.cwp.jinja_hub.repository.ChatRepository
import com.cwp.jinja_hub.repository.MessageRepository
import com.cwp.jinja_hub.ui.message.MessageActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase

class ChatsFragment : Fragment() {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!

    private lateinit var chatViewModel: ChatViewModel
    private lateinit var chatAdapter: ChatListAdapter
    private lateinit var fUser: FirebaseUser

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fUser = FirebaseAuth.getInstance().currentUser!!

        val chatRepository = ChatRepository(FirebaseDatabase.getInstance())
        chatViewModel = ViewModelProvider(
            this,
            ChatViewModel.ChatViewModelFactory(chatRepository, fUser)
        )[ChatViewModel::class.java]

        setupRecyclerView()
        observeViewModel()

        binding.refreshLayout.setOnRefreshListener {
            chatViewModel.refreshChats()
            binding.refreshLayout.isRefreshing = false
        }
    }

    private fun setupRecyclerView() {
        binding.chatsRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        chatAdapter = ChatListAdapter(
            chats = emptyList(),
            onChatClicked = { chatItem ->
                val intent = Intent(requireContext(), MessageActivity::class.java)
                intent.putExtra("receiverId", chatItem.userId)
                startActivity(intent)
            },
            chatRepository = ChatRepository(FirebaseDatabase.getInstance()),
            messageRepository = MessageRepository(FirebaseDatabase.getInstance())
        )
        binding.chatsRecyclerView.adapter = chatAdapter
    }

    private fun observeViewModel() {
        chatViewModel.chats.observe(viewLifecycleOwner) { chatList ->
            _binding?.let {
                chatAdapter.updateChatList(chatList.distinctBy { it.userId }) // Prevent duplicates
                binding.emptyStateView.visibility = if (chatList.isEmpty()) View.VISIBLE else View.GONE
            }
        }

        chatViewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            _binding?.let {
                if (errorMessage == "No chats available")
                    binding.emptyStateView.visibility = View.VISIBLE
                else
                    binding.emptyStateView.visibility = View.GONE
            }
        }

        chatViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            _binding?.let {
                binding.progressBarHolder.visibility = if (isLoading) View.VISIBLE else View.GONE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Prevent memory leaks
    }
}
