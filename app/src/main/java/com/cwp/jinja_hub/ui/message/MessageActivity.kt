package com.cwp.jinja_hub.ui.message

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import com.cwp.jinja_hub.adapters.MessageAdapter
import com.cwp.jinja_hub.databinding.ActivityMessageBinding
import com.cwp.jinja_hub.model.Message
import com.cwp.jinja_hub.repository.MessageRepository
import com.cwp.jinja_hub.viewmodel.MessageViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase

class MessageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMessageBinding
    private lateinit var messageViewModel: MessageViewModel
    private lateinit var adapter: MessageAdapter

    private var receiverId: String? = null
    private var firebaseUser: FirebaseUser? = null
    private val firebaseDatabase = FirebaseDatabase.getInstance()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            Toast.makeText(this, "Permission denied. Cannot access images.", Toast.LENGTH_SHORT).show()
        }
    }

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { imageUri: Uri? ->
        imageUri?.let { uri ->
            Toast.makeText(this, "Sending image...", Toast.LENGTH_SHORT).show()
            messageViewModel.sendImageMessage(
                senderId = firebaseUser!!.uid,
                receiverId = receiverId!!,
                imageUri = uri
            )
        } ?: Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMessageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Handle permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
        } else if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        receiverId = intent.getStringExtra("receiverId")
        firebaseUser = FirebaseAuth.getInstance().currentUser

        if (firebaseUser == null || receiverId == null) {
            Toast.makeText(this, "Unable to retrieve user or receiver information", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Initialize ViewModel and Adapter
        val repository = MessageRepository(firebaseDatabase)
        messageViewModel = ViewModelProvider(
            this, MessageViewModel.MessageViewModelFactory(repository)
        )[MessageViewModel::class.java]

        adapter = MessageAdapter(mutableListOf(), "")
        binding.recyclerview.adapter = adapter
        binding.recyclerview.layoutManager = LinearLayoutManager(this)

        // Observe receiver info (name, profile image)
        receiverId?.let {
            messageViewModel.fetchReceiverInfo(it)
            messageViewModel.receiverInfo.observe(this) { (name, profileImage) ->
                binding.receiverName.text = name
                binding.profileImage.load(profileImage)
                adapter.updateReceiverProfileImage(profileImage)
            }
        }

        // Observe messages and update the UI
        messageViewModel.messages.observe(this) { messages ->
            adapter.submitList(messages)

            // Automatically scroll to the bottom if new messages are added
            if (binding.recyclerview.canScrollVertically(1).not()) {
                binding.recyclerview.smoothScrollToPosition(messages.size - 1)
            }
        }

        // Load messages from ViewModel
        receiverId?.let { messageViewModel.getChats(receiverId!!) }

        // Send message button click
        binding.sendMessage.setOnClickListener {
            val text = binding.editTextMessage.text.toString().trim()
            if (text.isNotEmpty()) {
                val message = Message(
                    senderId = firebaseUser!!.uid,
                    receiverId = receiverId!!,
                    message = text,
                    timestamp = System.currentTimeMillis(),
                    isSeen = false,
                    mediaUrl = "",
                    status = "sent",
                    messageType = "text"
                )
                messageViewModel.sendMessageToUser(firebaseUser!!.uid, receiverId!!, message)
                binding.editTextMessage.text?.clear()
            } else {
                Toast.makeText(this, "Message cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }


        receiverId?.let { messageViewModel.markMessageAsSeen(it) }


        // Handle image sending
        binding.sendImage.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }
    }

    override fun onPause() {
        super.onPause()
        // Remove listeners when activity is paused
       // messageViewModel.seenListener.removeObservers(this)
    }
}
