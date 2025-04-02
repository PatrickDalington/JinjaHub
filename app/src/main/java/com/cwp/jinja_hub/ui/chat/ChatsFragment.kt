package com.cwp.jinja_hub.ui.chat

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.cwp.jinja_hub.R
import com.cwp.jinja_hub.databinding.FragmentChatBinding
import com.cwp.jinja_hub.adapters.ChatListAdapter
import com.cwp.jinja_hub.repository.ChatRepository
import com.cwp.jinja_hub.repository.MessageRepository
import com.cwp.jinja_hub.ui.message.MessageActivity
import com.google.android.material.snackbar.Snackbar
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
        val window = requireActivity().window
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)

        // Set status bar color
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = ContextCompat.getColor(requireContext(), R.color.black)

        // Make status bar icons light (dark text/icons)
       windowInsetsController.isAppearanceLightStatusBars = false  // Use `false` for light icons

        return binding.root
    }




    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fUser = FirebaseAuth.getInstance().currentUser!!

        val chatRepository = ChatRepository(FirebaseDatabase.getInstance())
        val messageRepository = MessageRepository(FirebaseDatabase.getInstance())
        chatViewModel = ViewModelProvider(
            this,
            ChatViewModel.ChatViewModelFactory(chatRepository, fUser, messageRepository)
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
            messageRepository = MessageRepository(FirebaseDatabase.getInstance()),
            onLongClickChat = { chatItem, position ->
                clearUserChats(chatItem.userId, position)
            }
        )
        binding.chatsRecyclerView.adapter = chatAdapter
    }

    private fun clearUserChats(receiverId: String, pos: Int){
        val alertDialog = AlertDialog.Builder(requireActivity())
        alertDialog.setTitle("Delete chats")
        alertDialog.setMessage("Proceeding will permanently delete the chat history for both you and the other user. This action cannot be undone.")
        alertDialog.setPositiveButton("Proceed") { dialog, _ ->
            dialog.dismiss()
            chatViewModel.clearUserFromChatList(receiverId) {
                if (it) {
                    chatViewModel.clearChatWithUser(receiverId){ success ->
                        if(success){
                            //observeMessages()
                            chatAdapter.notifyItemChanged(pos)
                            Snackbar.make(binding.root, "Chat deleted successfully", Snackbar.LENGTH_SHORT).show()
                        }else{
                            Snackbar.make(binding.root, "Error deleting chat", Snackbar.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Error clearing chat",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

        }
        alertDialog.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }
        alertDialog.create().show()
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
