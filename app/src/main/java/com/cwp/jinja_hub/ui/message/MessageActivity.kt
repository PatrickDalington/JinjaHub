package com.cwp.jinja_hub.ui.message

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.cwp.jinja_hub.R
import com.cwp.jinja_hub.adapters.MessageAdapter
import com.cwp.jinja_hub.databinding.ActivityMessageBinding
import com.cwp.jinja_hub.databinding.FragmentMessageBinding
import com.cwp.jinja_hub.model.Message
import com.cwp.jinja_hub.repository.MessageRepository
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.FirebaseDatabase
import java.util.UUID


class MessageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMessageBinding
    private lateinit var messageViewModel: MessageViewModel
    private lateinit var adapter: MessageAdapter
    private val chatId = "exampleChatId" // Replace dynamically
    private val userId = "exampleUserId" // Replace dynamically

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMessageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // get intent extra coming from ChatFragment
        val chatId = intent.getStringExtra("chatId")
        val userId = intent.getStringExtra("userId")

        val repository = MessageRepository(FirebaseDatabase.getInstance())
        messageViewModel = ViewModelProvider(this, MessageViewModel.MessageViewModelFactory(repository))[MessageViewModel::class.java]

        adapter = userId?.let { MessageAdapter(it) }!!
        binding.recyclerview.adapter = adapter
        binding.recyclerview.layoutManager = LinearLayoutManager(this)

        messageViewModel.messages.observe(this) { messages ->
            adapter.submitList(messages)
            binding.recyclerview.scrollToPosition(messages.size - 1)
        }

        if (chatId != null) {
            messageViewModel.loadMessages(chatId)
        }

        binding.buttonSend.setOnClickListener {
            val text = binding.editTextMessage.text.toString().trim()
            if (text.isNotEmpty()) {
                val message = Message(
                    messageId = UUID.randomUUID().toString(),
                    senderId = userId,
                    text = text,
                    timestamp = System.currentTimeMillis(),
                    read = false
                )
                if (chatId != null) {
                    messageViewModel.sendMessage(chatId, message)
                }
                binding.editTextMessage.text.clear()
            }
        }
    }
}
