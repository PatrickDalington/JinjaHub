package com.cwp.jinja_hub

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.cwp.jinja_hub.com.cwp.jinja_hub.listeners.ReselectedListener
import com.cwp.jinja_hub.com.cwp.jinja_hub.services.MyFirebaseMessagingService
import com.cwp.jinja_hub.databinding.ActivityMainBinding
import com.cwp.jinja_hub.repository.ProfessionalSignupRepository
import com.cwp.jinja_hub.ui.message.MessageActivity
import com.cwp.jinja_hub.ui.testimony_reviews.fragments.comments.LatestCommentsActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private val currentUser: FirebaseUser = FirebaseAuth.getInstance().currentUser!!

    private var updateDialogShown = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavigation()
        refreshFCMToken()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment
        navController = navHostFragment.navController
        val navView: BottomNavigationView = binding.navView
        navView.setupWithNavController(navController)

        navView.setOnItemReselectedListener { menuItem ->
            (supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main) as? NavHostFragment)
                ?.childFragmentManager?.primaryNavigationFragment?.let { currentFragment ->
                    if (currentFragment is ReselectedListener) {
                        currentFragment.onTabReselected()
                    } else {
                        Log.d("MainActivity", "Current fragment does not implement ReselectedListener")
                    }
                }
        }
    }

    fun selectTabAtPosition(position: Int) {
        val menuItemId = when (position) {
            0 -> R.id.navigation_home
            1 -> R.id.navigation_market_place
            2 -> R.id.navigation_activity
            3 -> R.id.navigation_chat
            else -> R.id.navigation_home
        }
        binding.navView.selectedItemId = menuItemId
    }

    private fun checkForAppUpdate() {

        val currentVersionCode = resources.getString(R.string.app_version).toIntOrNull() ?: 0
        Toast.makeText(this, "Current Version: $currentVersionCode", Toast.LENGTH_SHORT).show()
        val databaseRef = FirebaseDatabase.getInstance().getReference("latestVersionCode/version")

        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val latestVersionCode = snapshot.getValue(Int::class.java) ?: 0
                if (latestVersionCode > currentVersionCode) {
                    showUpdateDialog()
                }
                Log.d("UpdateCheck", "Current: $currentVersionCode, Latest: $latestVersionCode")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("UpdateCheck", "Failed to read latest version: ${error.message}")
            }
        })
    }

    private fun showUpdateDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.update_available_title))
            .setMessage(getString(R.string.update_available_message))
            .setCancelable(false)
            .setPositiveButton(getString(R.string.update_now)) { _, _ -> openPlayStore() }
            .setNegativeButton(getString(R.string.later)) { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun openPlayStore() {
        val appPackageName = packageName
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$appPackageName")))
        } catch (e: android.content.ActivityNotFoundException) {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")))
        }
    }

    override fun onStart() {
        super.onStart()
        if (!updateDialogShown) { // Check the flag
            checkForAppUpdate()
            updateDialogShown = true // Set the flag after checking
        }
        setUserLoggedInBefore()
        openChatFragment()
        checkIncomingNotificationIntent()
        handleDynamicLink()
    }

    private fun handleDynamicLink() {
        FirebaseDynamicLinks.getInstance().getDynamicLink(intent)
            .addOnSuccessListener(this) { pendingDynamicLinkData ->
                pendingDynamicLinkData?.link?.lastPathSegment?.let { reviewId ->
                    startActivity(Intent(this, LatestCommentsActivity::class.java).apply {
                        putExtra("REVIEW_ID", reviewId)
                    })
                }
            }
            .addOnFailureListener(this) { e -> Log.w("DynamicLinks", "getDynamicLink:onFailure", e) }
    }

    private fun refreshFCMToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                ProfessionalSignupRepository().updateUserProfile(currentUser.uid, mapOf("fcmToken" to token)) { success ->
                    if (success) Log.d("FCM", "Token updated successfully") else Log.e("FCM", "Token update failed")
                    MyFirebaseMessagingService().onNewToken(token)
                    Log.d("FCM", "Refreshed token: $token")
                }
            } else {
                Log.w("FCM", "Fetching FCM registration token failed", task.exception)
            }
        }
    }

    private fun setUserLoggedInBefore() {
        getSharedPreferences("user_log_preferences", MODE_PRIVATE).edit().putBoolean("is_logged_in", true).apply()
    }

    private fun openChatFragment() {
        if (intent.getBooleanExtra("openChatFragment", false)) {
            selectTabAtPosition(3)
        }
    }

    private fun checkIncomingNotificationIntent() {
        intent.getStringExtra("receiverId")?.let { receiverId ->
            startActivity(Intent(this, MessageActivity::class.java).apply {
                putExtra("receiverId", receiverId)
                flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            intent = Intent()
        }

        intent.getStringExtra("REVIEW_ID")?.let { reviewId ->
            startActivity(Intent(this, LatestCommentsActivity::class.java).apply {
                putExtra("REVIEW_ID", reviewId)
                flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            intent = Intent()
        }
    }
}