package com.cwp.jinja_hub.ui.message

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import com.cwp.jinja_hub.MainActivity
import com.cwp.jinja_hub.R
import com.cwp.jinja_hub.adapters.MessageAdapter
import com.cwp.jinja_hub.alert_dialogs.ReportDialog
import com.cwp.jinja_hub.com.cwp.jinja_hub.ui.message.MessageViewModel
import com.cwp.jinja_hub.databinding.ActivityMessageBinding
import com.cwp.jinja_hub.helpers.SendRegularNotification
import com.cwp.jinja_hub.model.Message
import com.cwp.jinja_hub.model.NotificationModel
import com.cwp.jinja_hub.repository.MessageRepository
import com.cwp.jinja_hub.ui.professionals_registration.ProfessionalSignupViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat

class MessageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMessageBinding
    private lateinit var messageViewModel: MessageViewModel
    private lateinit var adapter: MessageAdapter
    private val userViewModel: ProfessionalSignupViewModel by viewModels()

    private var comingFrom: String? = null

    private var receiverId: String? = null
    private var firebaseUser: FirebaseUser? = null
    private var fullName = ""
    private var myProfileImage = ""
    private var receiverName = ""
    private val firebaseDatabase = FirebaseDatabase.getInstance()

    private var scrollingTracker = "None"

    companion object {
        var currentChatId: String? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //enableEdgeToEdge()
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = resources.getColor(R.color.message_status_bar, theme) //or ContextCompat.getColor(this, R.color.your_dynamic_color)
        binding = ActivityMessageBinding.inflate(layoutInflater)
        setContentView(binding.root)



        // phone call
        binding.phoneCallIcon.setOnClickListener{
            Toast.makeText(this, "Voice calling coming soon", Toast.LENGTH_SHORT).show()
        }
        binding.videoCallIcon.setOnClickListener{
            Toast.makeText(this, "Video calling coming soon", Toast.LENGTH_SHORT).show()
        }


        // video call

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

        setupViewModelAndAdapter()
        loadReceiverInfo()
        observeMessages()
        fetchUserProfile()

        binding.sendMessage.setOnClickListener { sendMessage() }
        binding.sendImage.setOnClickListener {
            imagePickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
        binding.back.setOnClickListener { navigateToChatFragment() }


        messageViewModel.markMessageAsSeen(receiverId!!) // Ensure messages are marked as seen
        messageViewModel.getChats(receiverId!!)

        // set default toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)


        // Is loading
        messageViewModel.isLoading.observe(this) { isLoading ->
            if (isLoading) {
                binding.progressBar.visibility = View.VISIBLE
            } else {
                binding.progressBar.visibility = View.GONE
            }
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.message_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_clear_chats -> {
                clearChats(receiverId!!)
                true
            }
            R.id.action_report -> {
                loadReportFragment(
                    "Report $receiverName",
                    listOf(
                        "Select report category",
                        "Harassment & Bullying",
                        "Hate Speech",
                        "Spam & Scams",
                        "Impersonation",
                        "Inappropriate Content",
                        "Privacy Violation",
                        "Threats & Violence",
                        "Misinformation & Fake News",
                        "Unwanted Contact",
                        "Self-Harm & Suicide Concerns"
                    ),
                    fragment = ReportDialog()
                )
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun loadReportFragment(title: String, spinnerList: List<String>, fragment: Fragment){
        val bundle = Bundle()

        bundle.putString("type", "message")
        bundle.putString("title", title)
        bundle.putString("subTitle", "Let us know what's wrong. We take every report seriously")
        bundle.putStringArrayList("spinnerList", spinnerList.toCollection(ArrayList()))
        bundle.putString("id", receiverId)


        fragment.arguments = bundle

        supportFragmentManager.beginTransaction()
            .replace(R.id.message_container, fragment)
            .addToBackStack(null)
            .commit()
    }
    private fun clearChats(receiverId: String){
        val alertDialog = AlertDialog.Builder(this)
        alertDialog.setTitle("Delete chats")
        alertDialog.setMessage("Proceeding will permanently delete the chat history for both you and the other user. This action cannot be undone.")
        alertDialog.setPositiveButton("Proceed") { dialog, _ ->
            dialog.dismiss()
            messageViewModel.clearChatWithUser(receiverId){
                if(it){
                    observeMessages()
                    Toast.makeText(this, "Chats deleted successfully", Toast.LENGTH_SHORT).show()
                }
            }

        }
        alertDialog.create().show()

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

        adapter = MessageAdapter(
            "",
            this, onLongClickMessage = {message, position, which ->
                if (which == "right" && message.senderId == firebaseUser?.uid) {

                    val alertDialog = AlertDialog.Builder(this)
                    alertDialog.setTitle("Chat Info")
                    alertDialog.setMessage(SimpleDateFormat.getInstance().format(message.timestamp))
                    alertDialog.setPositiveButton("Delete") { dialog, _ ->
                        messageViewModel.deleteMessage(message.messageId){
                            //adapter.notifyItemRemoved(adapter.currentList.indexOf(message))
                            adapter.notifyItemRangeChanged(adapter.currentList.indexOf(message), adapter.currentList.size)
                        }
                        dialog.dismiss()
                    }
                    alertDialog.setNegativeButton("Cancel") { dialog, _ ->
                        dialog.dismiss()
                    }
                    alertDialog.create().show()

                }else{
                    val alertDialog = AlertDialog.Builder(this)
                    alertDialog.setTitle("Chat Info")
                    alertDialog.setMessage(SimpleDateFormat.getInstance().format(message.timestamp))
                    alertDialog.setPositiveButton("Ok") { dialog, _ ->
                        dialog.dismiss()
                    }
                    alertDialog.create().show()
                }
            })

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
            if (messages.isEmpty())
                binding.noMessage.visibility = View.VISIBLE
            else
                binding.noMessage.visibility = View.GONE

            adapter.submitList(messages)
            if (scrollingTracker == "New Message") {
                binding.recyclerview.postDelayed({
                    if (adapter.itemCount > 0) {
                        binding.recyclerview.smoothScrollToPosition(adapter.itemCount - 1)
                    }
                }, 100) // Delay to ensure smooth UI transition }
            }
        }
    }

    /** Fetches the receiver's name and profile image */
    private fun loadReceiverInfo() {
        receiverId?.let {
            messageViewModel.fetchReceiverInfo(it)
            messageViewModel.receiverInfo.observe(this) { (name, profileImage) ->
                receiverName = name
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
                myProfileImage = user?.profileImage ?: ""
            }
        }
    }

    /** Sends a text message */
    private fun sendMessage() {
        scrollingTracker = "New Message"
        val text = binding.editTextMessage.text.toString().trim()
        if (text.isEmpty()) return

        val message = Message(
            senderId = firebaseUser!!.uid,
            receiverId = receiverId!!,
            message = text,
            timestamp = System.currentTimeMillis(),
            isSeen = false,
            mediaUrl = emptyList(),
            status = "sent",
            messageType = "text"
        )


        messageViewModel.sendMessageToUser(firebaseUser!!.uid, receiverId!!, message) { success ->
            if (success) {
                sendBothNotifications(
                    text,
                    message.mediaUrl
                )
            } else {
                showToast("Failed to send message")
            }
        }
        binding.editTextMessage.text?.clear()
    }

    private fun sendBothNotifications(content: String, mediaUrl: List<String>) {
        val comingIn = if (mediaUrl.isNotEmpty()) "image" else "text"

        receiverId?.let { recId ->
            val notification = NotificationModel(
                posterId = firebaseUser!!.uid,
                content = "$fullName: $content",
                isRead = false,
                timestamp = System.currentTimeMillis(),
                key = firebaseUser!!.uid,
                type = "message"
            )

           // val sendNotification = SendRegularNotification()
           // sendNotification.sendNotification(recId, this, notification)

            userViewModel.getUserProfile(recId) { user ->
                if (user == null || user.fcmToken.isNullOrEmpty()) {
                    Log.e("MessageActivity", "Failed to retrieve user or FCM token for receiverId: $recId")
                    return@getUserProfile
                }

                Log.d("MessageActivity", "User FCM Token: ${user.fcmToken}")

                if (comingFrom == "market_place") {
                    val notificationData = mapOf(
                        "title" to "You have a buyer",
                        "body" to "$fullName is interested",
                        "chat" to firebaseUser!!.uid,
                        "type" to "message",
                        "imageUrl" to myProfileImage
                    )

                    messageViewModel.triggerNotification(user.fcmToken, notificationData)
                }


                val notificationData = if (comingIn == "image") {
                    mapOf(
                        "title" to fullName,
                        "body" to "Sent a photo",
                        "chat" to firebaseUser!!.uid,
                        "type" to "message",
                        "imageUrl" to myProfileImage,
                        "imageUrls" to mediaUrl.joinToString(",") // send the urls as a comma seperated string.
                    )
                } else {
                    mapOf(
                        "title" to fullName,
                        "body" to content,
                        "chat" to firebaseUser!!.uid,
                        "type" to "message",
                        "imageUrl" to myProfileImage
                    )
                }

                Log.d("MessageActivity", "Triggering FCM Notification: $notificationData")

                messageViewModel.triggerNotification(user.fcmToken, notificationData)
            }
        }
    }

    /** Handles image selection and sends an image message */
    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(4)) { uris ->
        if (uris.isNotEmpty()) {
            showImagePreviewBottomSheet(uris)
        } else {
            Toast.makeText(this, "No images selected", Toast.LENGTH_SHORT).show()
        }
    }


    private fun showImagePreviewBottomSheet(imageUris: List<Uri>) {
        val bottomSheetFragment = ImagePreviewBottomSheetFragment.newInstance(imageUris)
        bottomSheetFragment.onImagesSendClicked = { confirmedUris ->
            sendMultipleImages(confirmedUris)
        }
        bottomSheetFragment.show(supportFragmentManager, "ImagePreviewBottomSheet")
    }

    private fun sendMultipleImages(imageUris: List<Uri>) {
        showToast("Sending images...")
        messageViewModel.sendImageMessage(firebaseUser!!.uid, receiverId!!, imageUris) { success ->
            if (success) sendNotification("Sent images")
            else showToast("Failed to send images")
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
