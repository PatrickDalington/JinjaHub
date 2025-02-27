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

    private var comingFrom: String? = null

    private var receiverId: String? = null
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

        receiverId = intent.getStringExtra("receiverId")
        comingFrom = intent.getStringExtra("comingFrom")
        Toast.makeText(this, "Coming from: $comingFrom", Toast.LENGTH_SHORT).show()
        firebaseUser = FirebaseAuth.getInstance().currentUser

        if (firebaseUser == null || receiverId.isNullOrEmpty()) {
            showToast("Unable to retrieve user or receiver information")
            finish()
            return
        }

        Log.d("MessageActivity", "Opening chat with receiverId: $receiverId")
        currentChatId = receiverId

        setupViewModelAndAdapter()
        loadReceiverInfo()
        observeMessages()
        fetchUserProfile()

        binding.sendMessage.setOnClickListener { sendMessage() }
        binding.sendImage.setOnClickListener { imagePickerLauncher.launch("image/*") }
        binding.back.setOnClickListener { navigateToChatFragment() }


        messageViewModel.markMessageAsSeen(receiverId!!) // Ensure messages are marked as seen
        messageViewModel.getChats(receiverId!!)
    }

    override fun onDestroy() {
        super.onDestroy()
        currentChatId = null
    }

    /** Sets up the ViewModel and initializes the adapter */
    private fun setupViewModelAndAdapter() {
        val repository = MessageRepository(firebaseDatabase)
        messageViewModel = ViewModelProvider(
            this,
            MessageViewModel.MessageViewModelFactory(repository)
        )[MessageViewModel::class.java]

        adapter = MessageAdapter("", this) // ListAdapter handles internal list updates

        binding.recyclerview.apply {
            layoutManager = LinearLayoutManager(this@MessageActivity).apply {
                stackFromEnd = true // Ensures messages scroll from the bottom
            }
            adapter = this@MessageActivity.adapter
        }

        observeMessages()
    }

    /** Observes message updates and updates the RecyclerView */
    private fun observeMessages() {
        messageViewModel.messages.observe(this) { messages ->
            adapter.submitList(messages)
            binding.recyclerview.postDelayed({
                if (adapter.itemCount > 0) {
                    binding.recyclerview.smoothScrollToPosition(adapter.itemCount - 1)
                }
            }, 100) // Delay to ensure smooth UI transition
        }
    }

    /** Fetches the receiver's name and profile image */
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

    /** Fetches the user's profile information */
    private fun fetchUserProfile() {
        firebaseUser?.let {
            userViewModel.getUserProfile(it.uid) { user ->
                fullName = user?.fullName ?: ""
            }
        }
    }

    /** Sends a text message */
    private fun sendMessage() {
        val text = binding.editTextMessage.text.toString().trim()
        if (text.isEmpty()) return

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
                sendBothNotifications(text, message.mediaUrl)
            } else {
                showToast("Failed to send message")
            }
        }
        binding.editTextMessage.text?.clear()
    }

    private fun sendBothNotifications(content: String, mediaUrl: String) {
        receiverId?.let { recId ->
            Log.d("MessageActivity", "sendBothNotifications called: comingFrom = $comingFrom, receiverId = $recId")

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

            // Check if coming from "market_place" and fetch user profile
            userViewModel.getUserProfile(recId) { user ->
                if (user == null || user.fcmToken.isNullOrEmpty()) {
                    Log.e("MessageActivity", "Failed to retrieve user or FCM token for receiverId: $recId")
                    return@getUserProfile
                }

                Log.d("MessageActivity", "User FCM Token: ${user.fcmToken}")

                val notificationData = if (comingFrom == "market_place") {
                    mapOf(
                        "title" to "You have a potential buyer",
                        "body" to "$fullName: $content",
                        "chat" to firebaseUser!!.uid,
                        "type" to "message",
                        "mediaUrl" to mediaUrl
                    )
                } else {
                    mapOf(
                        "title" to fullName,
                        "body" to content,
                        "chat" to firebaseUser!!.uid,
                        "type" to "message",
                        "mediaUrl" to mediaUrl
                    )
                }

                Log.d("MessageActivity", "Triggering FCM Notification: $notificationData")

                messageViewModel.triggerNotification(user.fcmToken, notificationData)
            }
        }
    }


    /** Handles image selection and sends an image message */
    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { imageUri: Uri? ->
        imageUri?.let { uri ->
            showToast("Sending image...")
            messageViewModel.sendImageMessage(firebaseUser!!.uid, receiverId!!, uri) { success ->
                if (success) sendNotification("Sent an image")
            }
        }
    }

    /** Sends a push notification */
    private fun sendNotification(content: String) {
        receiverId?.let { recId ->
            val notification = NotificationModel(
                posterId = firebaseUser!!.uid,
                content = "$fullName: $content",
                isRead = false,
                timestamp = System.currentTimeMillis(),
                key = firebaseUser!!.uid,
                type = "message"
            )
            SendRegularNotification().sendNotification(recId, this, notification)
        }
    }

    /** Navigates back to the chat fragment */
    private fun navigateToChatFragment() {
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("openChatFragment", true)
        }
        startActivity(intent)
        finish()
    }

    /** Helper function to show toast messages */
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
