package com.cwp.jinja_hub.ui.message

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import com.cwp.jinja_hub.MainActivity
import com.cwp.jinja_hub.adapters.MessageAdapter
import com.cwp.jinja_hub.databinding.ActivityMessageBinding
import com.cwp.jinja_hub.helpers.SendRegularNotification
import com.cwp.jinja_hub.model.Message
import com.cwp.jinja_hub.model.NotificationModel
import com.cwp.jinja_hub.repository.MessageRepository
import com.cwp.jinja_hub.ui.professionals_registration.ProfessionalSignupViewModel
import com.cwp.jinja_hub.com.cwp.jinja_hub.ui.message.MessageViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase

class MessageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMessageBinding
    private lateinit var messageViewModel: MessageViewModel
    private lateinit var adapter: MessageAdapter
    private val userViewModel: ProfessionalSignupViewModel by viewModels()

    private var receiverId: String? = null
    private var comingFrom: String? = null
    private var firebaseUser: FirebaseUser? = null
    private var fullName = ""
    private val firebaseDatabase = FirebaseDatabase.getInstance()

    companion object {
        var currentChatId: String? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMessageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Handle permissions
        requestPermissions()

        // Get receiverId from intent
        receiverId = intent.getStringExtra("receiverId")
        comingFrom = intent.getStringExtra("comingFrom")
        firebaseUser = FirebaseAuth.getInstance().currentUser

        if (firebaseUser == null || receiverId.isNullOrEmpty()) {
            showToast("Unable to retrieve user or receiver information")
            finish()
            return
        }

        Log.d("MessageActivity", "Opening chat with receiverId: $receiverId")
        currentChatId = receiverId

        // Initialize ViewModel & Adapter
        setupViewModelAndAdapter()
        loadReceiverInfo()
        observeMessages()

        // Fetch user profile for notifications
        fetchUserProfile()

        // Handle message sending (text & image)
        binding.sendMessage.setOnClickListener { sendMessage() }
        binding.sendImage.setOnClickListener { imagePickerLauncher.launch("image/*") }

        // Mark messages as seen
        messageViewModel.markMessageAsSeen(receiverId!!)

        // Handle back button
        binding.back.setOnClickListener { navigateToChatFragment() }

        // fetch chats
        messageViewModel.getChats(receiverId!!)
    }

    override fun onDestroy() {
        super.onDestroy()
        currentChatId = null // Reset chat when user leaves
    }

    // **Permission Handling**
    private fun requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
        } else if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) showToast("Permission denied. Cannot access images.")
    }

    // **Setup ViewModel & Adapter**
    private fun setupViewModelAndAdapter() {
        val repository = MessageRepository(firebaseDatabase)
        messageViewModel = ViewModelProvider(this, MessageViewModel.MessageViewModelFactory(repository))[MessageViewModel::class.java]

        adapter = MessageAdapter(mutableListOf(), "", this)
        val layoutManager = LinearLayoutManager(this)
        layoutManager.stackFromEnd = true  // Starts list from the bottom
        layoutManager.reverseLayout = false // Normal order, but starts from end
        binding.recyclerview.layoutManager = layoutManager

        binding.recyclerview.adapter = adapter

    }

    private fun observeMessages() {
        messageViewModel.messages.observe(this) { messages ->
            Log.d("MessageActivity", "Updating UI with ${messages.size} messages")
            adapter.submitList(messages)
            adapter.notifyItemChanged(adapter.itemCount - 1)

            // Automatically scroll to the latest message
            if (messages.isNotEmpty() && !binding.recyclerview.canScrollVertically(1)) {
                binding.recyclerview.smoothScrollToPosition(messages.size - 1)
            }
        }
    }


    // **Fetch Receiver Info**
    private fun loadReceiverInfo() {
        receiverId?.let {
            messageViewModel.fetchReceiverInfo(it)
            messageViewModel.receiverInfo.observe(this) { (name, profileImage) ->
                binding.receiverName.text = name
                binding.profileImage.load(profileImage)
                adapter.updateReceiverProfileImage(profileImage)
            }
        }
    }

    // **Fetch User Profile for Notifications**
    private fun fetchUserProfile() {
        firebaseUser?.let {
            userViewModel.getUserProfile(it.uid) { user ->
                if (user != null) fullName = user.fullName
            }
        }
    }

    // **Send Message (Text & Image)**
    private fun sendMessage() {
        val text = binding.editTextMessage.text.toString().trim()
        if (text.isEmpty()) {
            showToast("Message cannot be empty")
            return
        }

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

        messageViewModel.sendMessageToUser(firebaseUser!!.uid, receiverId!!, message) { success ->
            if (success) {
                sendBothNotifications(text, "")
            } else {
                showToast("Failed to send message")
            }
        }
        binding.editTextMessage.text?.clear()
    }

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { imageUri: Uri? ->
        imageUri?.let { uri ->
            showToast("Sending image...")
            messageViewModel.sendImageMessage(firebaseUser!!.uid, receiverId!!, uri) { success ->
                if (success) {
                    sendBothNotifications("Sent an image", uri.toString())
                } else {
                    showToast("Failed to send image")
                }
            }
        } ?: showToast("No image selected")
    }

    // **Send Regular & Triggered Notifications**
    private fun sendBothNotifications(content: String, mediaUrl: String) {
        receiverId?.let { recId ->

            val notification = NotificationModel(
                posterId = firebaseUser!!.uid,
                content = "$fullName: $content",
                isRead = false,
                timestamp = System.currentTimeMillis(),
                key = firebaseUser!!.uid,
                type = "message"
            )

            // Regular Notification
            val sendNotification = SendRegularNotification()
            sendNotification.sendNotification(recId, this, notification)

            // FCM Notification
            if (comingFrom == "market_place"){
                userViewModel.getUserProfile(recId) { user ->
                    user?.let {
                        messageViewModel.triggerNotification(
                            user.fcmToken,
                            mapOf(
                                "title" to "You have a potential buyer",
                                "body" to "$fullName: $content",
                                "chat" to firebaseUser!!.uid,
                                "type" to "message",
                                "mediaUrl" to mediaUrl
                            )
                        )
                    }
                }
            }else{
                userViewModel.getUserProfile(recId) { user ->
                    user?.let {
                        messageViewModel.triggerNotification(
                            user.fcmToken,
                            mapOf(
                                "title" to fullName,
                                "body" to content,
                                "chat" to firebaseUser!!.uid,
                                "type" to "message",
                                "mediaUrl" to mediaUrl
                            )
                        )
                    }
                }
            }
        }
    }

    // **Navigation**
    private fun navigateToChatFragment() {
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("openChatFragment", true)
        }
        startActivity(intent)
        finish()
    }

    // **Helper Functions**
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }


}
