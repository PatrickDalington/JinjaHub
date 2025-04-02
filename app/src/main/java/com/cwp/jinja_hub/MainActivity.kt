package com.cwp.jinja_hub

import android.animation.ObjectAnimator
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.cwp.jinja_hub.com.cwp.jinja_hub.listeners.ReselectedListener
import com.cwp.jinja_hub.com.cwp.jinja_hub.services.MyFirebaseMessagingService
import com.cwp.jinja_hub.com.cwp.jinja_hub.ui.testimony_reviews.fragments.comments.NewsCommentsActivity
import com.cwp.jinja_hub.databinding.ActivityMainBinding
import com.cwp.jinja_hub.repository.ProfessionalSignupRepository
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
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Random

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private val currentUser: FirebaseUser = FirebaseAuth.getInstance().currentUser!!

    private var updateDialogShown = false
    private var notificationPermissionGranted = false

    private val requestNotificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                // Permission granted, proceed

            } else {

                startDelayedShow()
            }
        }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavigation()
        refreshFCMToken()
        checkNotificationPermission()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)





        binding.on.setOnClickListener {
            if (!notificationPermissionGranted)  {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", packageName, null)
                }
                startActivity(intent)
            }
        }
    }



    private fun checkNotificationPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = "android.permission.POST_NOTIFICATIONS"
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                requestNotificationPermissionLauncher.launch(permission)
            }
        }
    }


    private fun startDelayedShow() {
        val random = Random()
        val delayMillis = (random.nextInt(2) + 10) * 1000L // Random delay between 10 to 11 seconds

        Handler(Looper.getMainLooper()).postDelayed({
            binding.viewHolder.visibility = View.VISIBLE
            window.statusBarColor = resources.getColor(R.color.primary, theme)
            slideInTopLayoutHolder()
            startCountdownAndAnimate()
        }, delayMillis)
    }



    private fun startCountdownAndAnimate() {
        binding.viewHolder.visibility = View.VISIBLE

        val countDownTimer = object : CountDownTimer(10000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = millisUntilFinished / 1000
                binding.timer.text = String.format("00:%02d", seconds)
            }

            override fun onFinish() {
                binding.timer.text = "00:00"
                animateViewHolderSlideUp()
            }
        }

        countDownTimer.start()
    }

    private fun animateViewHolderSlideUp() {
        val slideUpAnimation = ObjectAnimator.ofFloat(binding.viewHolder, "translationY", 0f, -binding.viewHolder.height.toFloat())
        slideUpAnimation.duration = 500 // Animation duration in milliseconds
        slideUpAnimation.start().apply {
            window.statusBarColor = resources.getColor(R.color.white, theme)
            binding.viewHolder.visibility = View.GONE
        }
    }

    private fun slideInTopLayoutHolder() {
        // Measure the view after setting visibility to visible
        binding.viewHolder.measure(
            View.MeasureSpec.makeMeasureSpec(binding.viewHolder.width, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )

        // Calculate the starting position (above the view)
        val startY = -binding.viewHolder.measuredHeight.toFloat()

        // Set the initial position
        binding.viewHolder.translationY = startY

        // Create the animation
        val animator = ObjectAnimator.ofFloat(
            binding.viewHolder,
            "translationY",
            startY,
            0f // End position (original position)
        )

        animator.duration = 500 // Adjust duration as needed
        animator.interpolator = AccelerateDecelerateInterpolator()

        // Start the animation
        animator.start()
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

    @OptIn(DelicateCoroutinesApi::class)
    private fun checkIncomingNotificationIntent() {
        // post delay for notification 4s
        val notificationType = intent.getStringExtra("type")

        GlobalScope.launch {
            delay(4000)
            if (notificationType == "newsLike" || notificationType == "newsComment") {
                val newsId = intent.getStringExtra("newsId")
                if (newsId != null) {
                    startActivity(Intent(this@MainActivity, NewsCommentsActivity::class.java).apply {
                        putExtra("News_ID", newsId)
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
                    })
                    intent = Intent()
                }
            }
            if (notificationType == "testimonyLike" || notificationType == "testimonyComment") {
                val reviewId = intent.getStringExtra("reviewId")
                if (reviewId != null) {
                    startActivity(Intent(this@MainActivity, LatestCommentsActivity::class.java).apply {
                        putExtra("REVIEW_ID", reviewId)
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
                    })
                    intent = Intent()
                }
            }
        }




        intent.getStringExtra("REVIEW_ID")?.let { reviewId ->
            startActivity(Intent(this, LatestCommentsActivity::class.java).apply {
                putExtra("REVIEW_ID", reviewId)
                flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            intent = Intent()
        }

    intent.getStringExtra("News_ID")?.let { newsId ->
        startActivity(Intent(this, NewsCommentsActivity::class.java).apply {
            putExtra("News_ID", newsId)
            flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        intent = Intent()
    }
}

}